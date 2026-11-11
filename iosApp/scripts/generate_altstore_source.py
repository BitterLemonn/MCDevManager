"""Generate AltStore Classic / SideStore metadata from an IPA and a JSON template.

Run on macOS (codesign is used to read embedded entitlements). No dependencies.
This tool never changes the IPA, signs it, uploads files or publishes a release.
"""

import argparse
from copy import deepcopy
from datetime import datetime, timezone
import json
from pathlib import Path, PurePosixPath
import plistlib
import re
import stat
import subprocess
import tempfile
from urllib.parse import quote, urlsplit
import zipfile


def numeric_version(value):
    if not isinstance(value, str) or not re.fullmatch(r"\d+(?:\.\d+){0,2}", value):
        raise ValueError("Expected a numeric bundle version/build number")
    parts = tuple(map(int, value.split(".")))
    return parts + (0,) * (3 - len(parts))


def version_key(entry):
    return numeric_version(entry["version"]), numeric_version(entry["buildVersion"])


def signed_permissions(bundle):
    result = subprocess.run(
        ["codesign", "--display", "--entitlements", ":-", str(bundle)],
        capture_output=True, check=False,
    )
    if result.returncode:
        if b"code object is not signed at all" in result.stderr:
            return {}  # The upstream workflow intentionally produces an unsigned IPA.
        raise ValueError("Cannot inspect embedded entitlements: "
                         + result.stderr.decode("utf-8", errors="replace"))
    return plistlib.loads(result.stdout) if result.stdout.strip() else {}


def inspect_ipa(path, permissions_reader=signed_permissions):
    with tempfile.TemporaryDirectory() as temporary, zipfile.ZipFile(path) as archive:
        entries = archive.infolist()
        if sum(entry.file_size for entry in entries) > 1024 ** 3:
            raise ValueError("IPA exceeds 1 GiB uncompressed")
        for entry in entries:
            name = PurePosixPath(entry.filename)
            if (name.is_absolute() or ".." in name.parts or "\\" in entry.filename
                    or ":" in entry.filename or stat.S_ISLNK(entry.external_attr >> 16)):
                raise ValueError("Unsafe IPA archive path")
        roots = [entry.filename for entry in entries
                 if re.fullmatch(r"Payload/[^/]+\.app/Info\.plist", entry.filename)]
        if len(roots) != 1:
            raise ValueError("Expected exactly one main app in IPA")
        archive.extractall(temporary)
        main = Path(temporary) / PurePosixPath(roots[0]).parent
        info = plistlib.loads((main / "Info.plist").read_bytes())
        metadata = {"bundleIdentifier": info["CFBundleIdentifier"],
                    "version": info["CFBundleShortVersionString"],
                    "buildVersion": info["CFBundleVersion"],
                    "minOSVersion": info["MinimumOSVersion"], "size": path.stat().st_size}
        version_key(metadata)
        numeric_version(metadata["minOSVersion"])
        permissions = set()
        privacy = {}
        for bundle in [main, *sorted(main.rglob("*.appex"))]:
            bundle_info = plistlib.loads((bundle / "Info.plist").read_bytes())
            permissions.update(permissions_reader(bundle))
            for key, value in bundle_info.items():
                if key.startswith("NS") and "UsageDescription" in key:
                    if not isinstance(value, str) or not value.strip():
                        raise ValueError(f"Empty privacy description: {key}")
                    privacy.setdefault(key, value)
        metadata["appPermissions"] = {"entitlements": sorted(permissions), "privacy": privacy}
        return metadata


def download_url(pattern, filename, tag):
    if "{tag}" in pattern and not tag:
        raise ValueError("The template uses {tag}; provide --release-tag")
    url = pattern.replace("{filename}", quote(filename, safe=""))
    url = url.replace("{tag}", quote(tag or "", safe=""))
    parsed = urlsplit(url)
    if ("{" in url or "}" in url or parsed.scheme != "https" or not parsed.netloc
            or parsed.username or parsed.password or parsed.fragment
            or any(character.isspace() for character in url)):
        raise ValueError("downloadURL must be a public HTTPS IPA URL without credentials")
    return url


def generate(template, metadata, filename, tag=None, date=None, previous=None):
    source = deepcopy(template)
    if len(source.get("apps", [])) != 1:
        raise ValueError("Template must describe one app")
    app = source["apps"][0]
    if app["bundleIdentifier"] != metadata["bundleIdentifier"]:
        raise ValueError("Template bundleIdentifier does not match the actual IPA")
    if len(app.get("versions", [])) != 1:
        raise ValueError("Template needs one version with downloadURL")
    entry = app["versions"][0]
    url = download_url(entry["downloadURL"], filename, tag)
    entry.update({key: metadata[key] for key in
                  ("version", "buildVersion", "minOSVersion", "size")})
    version_key(entry)
    entry["downloadURL"] = url
    entry["date"] = date or datetime.now(timezone.utc).isoformat(timespec="seconds")
    if datetime.fromisoformat(entry["date"].replace("Z", "+00:00")).tzinfo is None:
        raise ValueError("date must include a time zone")
    permissions = deepcopy(metadata["appPermissions"])
    history = []
    if previous:
        old_apps = previous.get("apps", [])
        if len(old_apps) != 1 or old_apps[0]["bundleIdentifier"] != app["bundleIdentifier"]:
            raise ValueError("Previous source describes a different application")
        old_app = old_apps[0]
        history = deepcopy(old_app["versions"])
        if history and version_key(entry) < max(map(version_key, history)):
            raise ValueError("Refusing to replace a newer source version")
        for old in history:
            if version_key(old) == version_key(entry):
                if old["downloadURL"] != url or old["size"] != entry["size"]:
                    raise ValueError("Existing version/build points to a different package")
                entry["date"] = old["date"]
        history = [old for old in history if version_key(old) != version_key(entry)]
        old_permissions = old_app["appPermissions"]
        permissions["entitlements"] = sorted(set(permissions["entitlements"])
                                             | set(old_permissions["entitlements"]))
        permissions["privacy"] = {**old_permissions["privacy"], **permissions["privacy"]}
    app["versions"] = [entry, *history]
    app["appPermissions"] = permissions
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ipa", type=Path, required=True)
    parser.add_argument("--template", type=Path, default=Path("altstore/source.template.json"))
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--release-tag")
    parser.add_argument("--date")
    parser.add_argument("--previous", type=Path)
    args = parser.parse_args()
    template = json.loads(args.template.read_text(encoding="utf-8"))
    previous = json.loads(args.previous.read_text(encoding="utf-8")) if args.previous else None
    source = generate(template, inspect_ipa(args.ipa), args.ipa.name,
                      args.release_tag, args.date, previous)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(source, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    entry = source["apps"][0]["versions"][0]
    print(f"Generated source for {entry['version']} ({entry['buildVersion']}), {entry['size']} bytes")


if __name__ == "__main__":
    main()
