"""Rebuild an AltStore / SideStore source from all public GitHub Releases.

Run on macOS. Downloads existing IPAs for inspection; never builds or modifies them.
GITHUB_TOKEN is optional and is sent only to api.github.com, never to IPA hosts.
"""

import argparse
from copy import deepcopy
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import tempfile
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen
import zipfile

from generate_altstore_source import download_url, generate, inspect_ipa, version_key


MAX_IPA_BYTES = 512 * 1024 * 1024


def read_json(url, token=""):
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "MCDevManager-Source"}
    if token:
        if not url.startswith("https://api.github.com/"):
            raise ValueError("GitHub token is restricted to the GitHub API")
        headers["Authorization"] = f"Bearer {token}"
    with urlopen(Request(url, headers=headers), timeout=60) as response:
        return json.load(response)


def list_releases(repository, fetch=read_json, token=""):
    if not re.fullmatch(r"[\w.-]+/[\w.-]+", repository):
        raise ValueError("Expected repository in owner/name format")
    releases = []
    page = 1
    while True:
        batch = fetch(f"https://api.github.com/repos/{repository}/releases?per_page=100&page={page}",
                      token)
        if not isinstance(batch, list):
            raise ValueError("Unexpected releases API response")
        releases.extend(batch)
        if len(batch) < 100:
            return releases
        page += 1


def release_assets(repository, release, fetch=read_json, token=""):
    # Paginate separately: embedded assets can be truncated on large Releases.
    result = []
    page = 1
    release_id = int(release["id"])
    while True:
        batch = fetch(f"https://api.github.com/repos/{repository}/releases/{release_id}"
                      f"/assets?per_page=100&page={page}", token)
        if not isinstance(batch, list):
            raise ValueError("Unexpected assets API response")
        result.extend(batch)
        if len(batch) < 100:
            return result
        page += 1


def download(url, path):
    """Anonymous HTTPS download, bounded in size, with retries for transient failures."""
    download_url(url, "", None)
    for attempt in range(3):
        try:
            request = Request(url, headers={"User-Agent": "MCDevManager-Source"})
            digest = hashlib.sha256()
            size = 0
            with urlopen(request, timeout=60) as response, path.open("wb") as output:
                if not response.geturl().startswith("https://"):
                    raise ValueError("IPA redirected to a non-HTTPS URL")
                while True:
                    chunk = response.read(1024 * 1024)
                    if not chunk:
                        break
                    size += len(chunk)
                    if size > MAX_IPA_BYTES:
                        raise ValueError("IPA exceeds 512 MiB download limit")
                    output.write(chunk)
                    digest.update(chunk)
            return size, digest.hexdigest()
        except (HTTPError, URLError, TimeoutError, ConnectionError):
            if attempt == 2:
                raise
            time.sleep(2 ** attempt)
    raise RuntimeError("Download failed")


def collect(repository, releases, template, include_prereleases=False,
            assets_reader=release_assets, downloader=download, inspector=inspect_ipa, token=""):
    candidates = []
    report = {"repository": repository, "includePrereleases": include_prereleases,
              "scannedReleases": len(releases), "included": [], "skipped": [], "errors": []}
    for release in sorted(releases, key=lambda item: item.get("published_at") or "", reverse=True):
        tag = release["tag_name"]
        if release.get("draft") or not release.get("published_at"):
            # The report becomes public too; never expose private draft labels.
            report["skipped"].append({"reason": "draft or unpublished"})
            continue
        if release.get("prerelease") and not include_prereleases:
            report["skipped"].append({"tag": tag, "reason": "prerelease excluded"})
            continue
        try:
            assets = assets_reader(repository, release, token=token)
            ipas = [asset for asset in assets if asset["name"].lower().endswith(".ipa")]
            if not ipas:
                report["skipped"].append({"tag": tag, "reason": "no IPA asset"})
            for asset in sorted(ipas, key=lambda item: item["id"], reverse=True):
                record = {"tag": tag, "asset": asset["name"], "assetId": asset["id"]}
                try:
                    if asset.get("state") != "uploaded" or not 0 < asset["size"] <= MAX_IPA_BYTES:
                        raise ValueError("Asset is not ready or exceeds the size limit")
                    with tempfile.TemporaryDirectory() as temporary:
                        path = Path(temporary) / "download.ipa"
                        size, digest = downloader(asset["browser_download_url"], path)
                        if size != asset["size"]:
                            raise ValueError("Downloaded size differs from GitHub asset metadata")
                        if asset.get("digest") and asset["digest"] != "sha256:" + digest:
                            raise ValueError("Downloaded SHA-256 differs from GitHub asset metadata")
                        metadata = inspector(path)
                        if metadata["bundleIdentifier"] != template["apps"][0]["bundleIdentifier"]:
                            report["skipped"].append({**record, "reason": "different Bundle ID"})
                            continue
                        source = generate(template, metadata, asset["name"], tag,
                                          release["published_at"])
                        entry = source["apps"][0]["versions"][0]
                        # A custom mirror must serve the same bytes, not a different/latest IPA.
                        if entry["downloadURL"] != asset["browser_download_url"]:
                            mirror_size, mirror_digest = downloader(entry["downloadURL"], path)
                            if (mirror_size, mirror_digest) != (size, digest):
                                raise ValueError("Configured downloadURL does not serve this IPA")
                    record.update({"version": entry["version"], "buildVersion": entry["buildVersion"],
                                   "sha256": digest})
                    candidates.append({"record": record, "source": source})
                except (ValueError, KeyError, OSError, RuntimeError, zipfile.BadZipFile) as error:
                    report["errors"].append({**record, "reason": str(error)})
        except (ValueError, KeyError, OSError) as error:
            report["errors"].append({"tag": tag, "reason": str(error)})
    return candidates, report


def assemble(template, candidates, report):
    """Newest real bundle version first; identical version/build keeps newest release."""
    source = deepcopy(template)
    app = source["apps"][0]
    versions, seen, urls = [], {}, {}
    entitlements, privacy = set(), {}
    candidates = sorted(candidates, key=lambda item: (
        version_key(item["source"]["apps"][0]["versions"][0]),
        item["source"]["apps"][0]["versions"][0]["date"], item["record"]["assetId"]), reverse=True)
    for candidate in candidates:
        candidate_app = candidate["source"]["apps"][0]
        entry = candidate_app["versions"][0]
        record = candidate["record"]
        key = version_key(entry)
        if key in seen:
            report["skipped"].append({**record, "reason": "duplicate real version/build",
                                      "keptTag": seen[key]["tag"],
                                      "sameBytes": record["sha256"] == seen[key]["sha256"]})
            continue
        if entry["downloadURL"] in urls:
            report["errors"].append({**record, "reason": "different versions share one downloadURL"})
            continue
        versions.append(entry)
        seen[key] = record
        urls[entry["downloadURL"]] = key
        report["included"].append(record)
        permissions = candidate_app["appPermissions"]
        entitlements.update(permissions["entitlements"])
        for name, description in permissions["privacy"].items():
            privacy.setdefault(name, description)
    if not versions:
        report["errors"].append({"reason": "No valid IPA versions; existing source must be preserved"})
    app["versions"] = versions
    app["appPermissions"] = {"entitlements": sorted(entitlements), "privacy": privacy}
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--template", type=Path, default=Path("altstore/source.template.json"))
    parser.add_argument("--output", type=Path, default=Path("build/altstore/source.json"))
    parser.add_argument("--report", type=Path, default=Path("build/altstore/report.json"))
    parser.add_argument("--include-prereleases", action="store_true")
    parser.add_argument("--ipa", type=Path, help="Include the new local IPA before its draft is published")
    parser.add_argument("--release-tag", help="Release tag for --ipa")
    args = parser.parse_args()
    if bool(args.ipa) != bool(args.release_tag):
        parser.error("--ipa and --release-tag must be used together")
    template = json.loads(args.template.read_text(encoding="utf-8"))
    token = os.environ.get("GITHUB_TOKEN", "")
    releases = list_releases(args.repository, token=token)
    candidates, report = collect(args.repository, releases, template, args.include_prereleases, token=token)
    if args.ipa:
        metadata = inspect_ipa(args.ipa)
        source = generate(template, metadata, args.ipa.name, args.release_tag,
                          datetime.now(timezone.utc).isoformat(timespec="seconds"))
        digest = hashlib.sha256()
        with args.ipa.open("rb") as package:
            for chunk in iter(lambda: package.read(1024 * 1024), b""):
                digest.update(chunk)
        candidates.append({"source": source, "record": {
            "tag": args.release_tag, "asset": args.ipa.name, "assetId": 0,
            "version": metadata["version"], "buildVersion": metadata["buildVersion"],
            "sha256": digest.hexdigest(), "pendingPublication": True}})
    source = assemble(template, candidates, report)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Scanned {report['scannedReleases']} releases; included {len(report['included'])}, "
          f"skipped {len(report['skipped'])}, errors {len(report['errors'])}")
    if report["errors"]:
        raise SystemExit("Source was not written; inspect report.json. Published source is unchanged.")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(source, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
