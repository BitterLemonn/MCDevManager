from copy import deepcopy
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import sync_altstore_source as tool


class HistoryTests(unittest.TestCase):
    def setUp(self):
        self.template = {"name": "Test", "apps": [{"name": "Test",
            "bundleIdentifier": "com.example.app", "versions": [{
                "downloadURL": "https://example.com/{tag}/{filename}"}]}], "news": []}
        self.metadata = {"bundleIdentifier": "com.example.app", "version": "1.2.9",
                         "buildVersion": "1", "minOSVersion": "15.0", "size": 3,
                         "appPermissions": {"entitlements": [], "privacy": {}}}
        self.release = {"id": 1, "tag_name": "v1.2.9", "published_at": "2026-10-21T00:00:00Z",
                        "draft": False, "prerelease": False}
        self.asset = {"id": 10, "name": "app.ipa", "state": "uploaded", "size": 3,
                      "browser_download_url": "https://example.com/v1.2.9/app.ipa",
                      "digest": "sha256:abc"}

    def collect(self, releases=None, assets=None, downloader=None, **kwargs):
        return tool.collect("example/repo", releases or [self.release], self.template,
                            assets_reader=lambda *_args, **_kw: assets if assets is not None else [self.asset],
                            downloader=downloader or (lambda *_args: (3, "abc")),
                            inspector=lambda _path: deepcopy(self.metadata), **kwargs)

    def test_paginates_releases_beyond_one_hundred(self):
        urls = []

        def fetch(url, _token):
            urls.append(url)
            return [self.release] * (100 if "page=1" == url.split("&")[-1] else 1)

        self.assertEqual(len(tool.list_releases("example/repo", fetch)), 101)
        self.assertTrue(urls[-1].endswith("page=2"))

    def test_paginates_assets_separately(self):
        def fetch(url, _token):
            return [self.asset] * (100 if url.endswith("page=1") else 1)
        self.assertEqual(len(tool.release_assets("example/repo", self.release, fetch)), 101)

    def test_rejects_invalid_repository_and_token_destination(self):
        with self.assertRaises(ValueError):
            tool.list_releases("../repo?token=bad")
        with self.assertRaises(ValueError):
            tool.read_json("https://example.com/", token="secret")

    def test_excludes_drafts_and_prereleases_by_default(self):
        releases = [{**self.release, "draft": True}, {**self.release, "prerelease": True}]
        candidates, report = self.collect(releases)
        self.assertEqual(candidates, [])
        self.assertEqual(len(report["skipped"]), 2)
        self.assertNotIn("tag", report["skipped"][0])
        candidates, _report = self.collect(releases, include_prereleases=True)
        self.assertEqual(len(candidates), 1)

    def test_reports_releases_without_ipa(self):
        candidates, report = self.collect(assets=[])
        self.assertEqual(candidates, [])
        self.assertEqual(report["skipped"][0]["reason"], "no IPA asset")

    def test_collects_exact_package_metadata(self):
        candidates, report = self.collect()
        source = tool.assemble(self.template, candidates, report)
        entry = source["apps"][0]["versions"][0]
        self.assertEqual((entry["version"], entry["size"]), ("1.2.9", 3))
        self.assertEqual(report["errors"], [])
        self.assertEqual(len(report["included"]), 1)

    def test_rejects_size_and_digest_mismatches(self):
        for result in ((2, "abc"), (3, "different")):
            with self.subTest(result=result):
                candidates, report = self.collect(downloader=lambda *_args: result)
                self.assertEqual(candidates, [])
                self.assertEqual(len(report["errors"]), 1)

    def test_custom_mirror_must_serve_same_package(self):
        self.template["apps"][0]["versions"][0]["downloadURL"] = "https://mirror.example/{tag}/app.ipa"
        calls = []

        def downloader(url, _path):
            calls.append(url)
            return 3, "wrong" if "mirror" in url else "abc"

        candidates, report = self.collect(downloader=downloader)
        self.assertEqual(len(calls), 2)
        self.assertEqual(candidates, [])
        self.assertIn("downloadURL", report["errors"][0]["reason"])

    def test_skips_other_bundle_ids(self):
        self.metadata["bundleIdentifier"] = "com.other.app"
        candidates, report = self.collect()
        self.assertEqual(candidates, [])
        self.assertEqual(report["skipped"][0]["reason"], "different Bundle ID")

    def test_download_failure_is_recorded(self):
        def downloader(*_args):
            raise OSError("network unavailable")
        candidates, report = self.collect(downloader=downloader)
        self.assertEqual(candidates, [])
        self.assertEqual(report["errors"][0]["reason"], "network unavailable")

    def test_numeric_sort_and_permission_union(self):
        candidates, report = self.collect()
        older = deepcopy(candidates[0])
        newer = deepcopy(candidates[0])
        older["source"]["apps"][0]["versions"][0].update({"version": "1.9", "downloadURL": "https://e.com/old.ipa"})
        newer["source"]["apps"][0]["versions"][0].update({"version": "1.10", "downloadURL": "https://e.com/new.ipa"})
        older["source"]["apps"][0]["appPermissions"]["entitlements"] = ["old.permission"]
        newer["source"]["apps"][0]["appPermissions"]["entitlements"] = ["new.permission"]
        source = tool.assemble(self.template, [older, newer], report)
        self.assertEqual([entry["version"] for entry in source["apps"][0]["versions"]], ["1.10", "1.9"])
        self.assertEqual(source["apps"][0]["appPermissions"]["entitlements"], ["new.permission", "old.permission"])

    def test_duplicate_real_versions_keep_newest_release_without_faking_version(self):
        candidates, report = self.collect()
        older = deepcopy(candidates[0])
        older["source"]["apps"][0]["versions"][0]["date"] = "2026-09-01T00:00:00Z"
        older["record"].update({"tag": "v1.2.8", "sha256": "different"})
        source = tool.assemble(self.template, [older, candidates[0]], report)
        self.assertEqual(len(source["apps"][0]["versions"]), 1)
        self.assertEqual(report["included"][0]["tag"], "v1.2.9")
        self.assertEqual(report["skipped"][0]["keptTag"], "v1.2.9")
        self.assertFalse(report["skipped"][0]["sameBytes"])

    def test_rejects_shared_url_across_different_versions(self):
        candidates, report = self.collect()
        newer = deepcopy(candidates[0])
        newer["source"]["apps"][0]["versions"][0]["version"] = "1.3.0"
        tool.assemble(self.template, [*candidates, newer], report)
        self.assertEqual(len(report["errors"]), 1)

    def test_failure_keeps_existing_output_and_writes_report(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            template = root / "template.json"
            template.write_text(json.dumps(self.template), encoding="utf-8")
            output = root / "source.json"
            output.write_text("existing source", encoding="utf-8")
            report = root / "report.json"
            args = ["sync", "--repository", "example/repo", "--template", str(template),
                    "--output", str(output), "--report", str(report)]
            empty_report = {"scannedReleases": 0, "included": [], "skipped": [], "errors": []}
            with patch("sys.argv", args), patch.object(tool, "list_releases", return_value=[]), \
                    patch.object(tool, "collect", return_value=([], empty_report)), self.assertRaises(SystemExit):
                tool.main()
            self.assertEqual(output.read_text(encoding="utf-8"), "existing source")
            self.assertTrue(json.loads(report.read_text(encoding="utf-8"))["errors"])

    def test_local_new_ipa_is_merged_with_history(self):
        candidates, report = self.collect()
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            template = root / "template.json"
            template.write_text(json.dumps(self.template), encoding="utf-8")
            ipa = root / "new.ipa"
            ipa.write_bytes(b"ipa")
            output = root / "source.json"
            args = ["sync", "--repository", "example/repo", "--template", str(template),
                    "--output", str(output), "--report", str(root / "report.json"),
                    "--ipa", str(ipa), "--release-tag", "v1.2.10"]
            metadata = {**self.metadata, "version": "1.2.10"}
            with patch("sys.argv", args), patch.object(tool, "list_releases", return_value=[]), \
                    patch.object(tool, "collect", return_value=(candidates, report)), \
                    patch.object(tool, "inspect_ipa", return_value=metadata):
                tool.main()
            source = json.loads(output.read_text(encoding="utf-8"))
            self.assertEqual([entry["version"] for entry in source["apps"][0]["versions"]], ["1.2.10", "1.2.9"])


if __name__ == "__main__":
    unittest.main()
