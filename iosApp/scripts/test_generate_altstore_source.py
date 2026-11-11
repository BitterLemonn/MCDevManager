from copy import deepcopy
from pathlib import Path
import plistlib
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

import generate_altstore_source as tool


class AltStoreSourceTests(unittest.TestCase):
    def setUp(self):
        self.template = {"name": "Test source", "apps": [{
            "name": "Test", "bundleIdentifier": "com.example.app",
            "versions": [{"downloadURL": "https://example.com/{tag}/{filename}"}],
        }], "news": []}
        self.metadata = {"bundleIdentifier": "com.example.app", "version": "1.2.9",
                         "buildVersion": "10", "minOSVersion": "15.0", "size": 1234,
                         "appPermissions": {"entitlements": [], "privacy": {}}}

    def generate(self, metadata=None, previous=None):
        return tool.generate(self.template, metadata or self.metadata, "App 1.ipa", "v1.2.9",
                             "2026-10-27T00:00:00Z", previous)

    def test_generates_actual_version_and_encoded_download(self):
        source = self.generate()
        entry = source["apps"][0]["versions"][0]
        self.assertEqual(entry["version"], "1.2.9")
        self.assertEqual(entry["buildVersion"], "10")
        self.assertEqual(entry["size"], 1234)
        self.assertEqual(entry["downloadURL"], "https://example.com/v1.2.9/App%201.ipa")
        self.assertNotIn("size", self.template["apps"][0]["versions"][0])

    def test_fork_only_needs_to_change_download_url(self):
        self.template["apps"][0]["versions"][0]["downloadURL"] = "https://fork.example/app.ipa"
        self.assertEqual(self.generate()["apps"][0]["versions"][0]["downloadURL"],
                         "https://fork.example/app.ipa")

    def test_rejects_missing_tag_and_unsafe_url(self):
        with self.assertRaisesRegex(ValueError, "release-tag"):
            tool.download_url("https://example.com/{tag}/app.ipa", "app.ipa", None)
        for pattern in ("http://example.com/app.ipa", "https://user:pass@example.com/a.ipa",
                        "https://example.com/{unknown}", "https://example.com/a.ipa#x"):
            with self.subTest(pattern=pattern), self.assertRaises(ValueError):
                tool.download_url(pattern, "app.ipa", "v1")

    def test_rejects_wrong_bundle_id(self):
        self.metadata["bundleIdentifier"] = "com.other.app"
        with self.assertRaisesRegex(ValueError, "bundleIdentifier"):
            self.generate()

    def test_history_is_preserved_and_rollback_rejected(self):
        previous = self.generate()
        self.metadata["buildVersion"] = "11"
        current = self.generate(previous=previous)
        self.assertEqual([entry["buildVersion"] for entry in current["apps"][0]["versions"]],
                         ["11", "10"])
        self.metadata["buildVersion"] = "9"
        with self.assertRaisesRegex(ValueError, "newer source"):
            self.generate(previous=current)

    def test_same_build_is_idempotent_but_different_size_rejected(self):
        previous = self.generate()
        self.assertEqual(self.generate(previous=previous), previous)
        self.metadata["size"] += 1
        with self.assertRaisesRegex(ValueError, "different package"):
            self.generate(previous=previous)

    def test_real_zip_reads_main_and_extension_permissions(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "test.ipa"
            main = {"CFBundleIdentifier": "com.example.app", "CFBundleShortVersionString": "1.2.9",
                    "CFBundleVersion": "10", "MinimumOSVersion": "15.0"}
            widget = {**main, "CFBundleIdentifier": "com.example.app.widget",
                      "NSLocalNetworkUsageDescription": "Read local data"}
            with zipfile.ZipFile(path, "w") as archive:
                archive.writestr("Payload/Test.app/Info.plist", plistlib.dumps(main))
                archive.writestr("Payload/Test.app/PlugIns/Widget.appex/Info.plist", plistlib.dumps(widget))
            metadata = tool.inspect_ipa(path, lambda _bundle: {"com.apple.security.application-groups": []})
            self.assertEqual(metadata["size"], path.stat().st_size)
            self.assertEqual(metadata["appPermissions"]["entitlements"],
                             ["com.apple.security.application-groups"])
            self.assertEqual(metadata["appPermissions"]["privacy"],
                             {"NSLocalNetworkUsageDescription": "Read local data"})

    def test_rejects_unsafe_archive(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "test.ipa"
            with zipfile.ZipFile(path, "w") as archive:
                archive.writestr("../../escape", "no")
            with self.assertRaisesRegex(ValueError, "Unsafe"):
                tool.inspect_ipa(path)

    def test_unsigned_is_supported_but_unknown_signing_errors_fail(self):
        with patch.object(tool.subprocess, "run") as run:
            run.return_value.returncode = 1
            run.return_value.stderr = b"code object is not signed at all"
            self.assertEqual(tool.signed_permissions(Path("Test.app")), {})
            run.return_value.stderr = b"corrupted signature"
            with self.assertRaisesRegex(ValueError, "Cannot inspect"):
                tool.signed_permissions(Path("Test.app"))

    def test_previous_permissions_remain_declared(self):
        old = deepcopy(self.metadata)
        old["appPermissions"]["entitlements"] = ["previous.permission"]
        previous = self.generate(old)
        self.metadata["buildVersion"] = "11"
        self.assertIn("previous.permission", self.generate(previous=previous)["apps"][0][
            "appPermissions"]["entitlements"])

    @unittest.skipUnless(sys.platform == "darwin", "Requires real macOS codesign")
    def test_real_codesign_unsigned_and_adhoc_permissions(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = root / "Test.app"
            bundle.mkdir()
            info = {"CFBundleIdentifier": "com.example.source.test", "CFBundleExecutable": "Test",
                    "CFBundlePackageType": "APPL", "CFBundleVersion": "1",
                    "CFBundleShortVersionString": "1.0", "MinimumOSVersion": "15.0"}
            (bundle / "Info.plist").write_bytes(plistlib.dumps(info))
            executable = bundle / "Test"
            # Do not copy macOS protected system flags from the fixture binary.
            shutil.copyfile("/usr/bin/true", executable)
            executable.chmod(0o755)
            subprocess.run(["codesign", "--remove-signature", str(executable)], check=True)
            self.assertEqual(tool.signed_permissions(bundle), {})
            permissions = root / "permissions.plist"
            permissions.write_bytes(plistlib.dumps({"com.apple.security.application-groups": ["group.test"]}))
            subprocess.run(["codesign", "--force", "--sign", "-", "--entitlements", str(permissions),
                            str(bundle)], check=True)
            self.assertEqual(tool.signed_permissions(bundle)["com.apple.security.application-groups"],
                             ["group.test"])


if __name__ == "__main__":
    unittest.main()
