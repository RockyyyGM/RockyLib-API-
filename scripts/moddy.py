PK     ȥ�Z               moddy/PK     ȥ�Z               moddy/commands/PK     ���ZU��A�  �     moddy/main.py#!/usr/bin/env python3
"""Moddy - Multipurpose helper for the multiloader template.

This tool bundles various project setup and maintenance commands used with
multiloader-template projects. Each command is implemented in its own module
under ``moddy.commands`` for easier maintenance.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from .commands import (
    cmd_add_service,
    cmd_open_libs,
    cmd_open,
    cmd_docs,
    cmd_set_minecraft_version,
    cmd_setup,
    cmd_update,
    cmd_ping,
    cmd_changelog,
    cmd_help,
    cmd_version,
    check_for_update,
)


COMMANDS = {
    "add-service": cmd_add_service,
    "open-libs": cmd_open_libs,
    "open": cmd_open,
    "docs": cmd_docs,
    "set-minecraft-version": cmd_set_minecraft_version,
    "setup": cmd_setup,
    "update": cmd_update,
    "ping": cmd_ping,
    "changelog": cmd_changelog,
    "help": cmd_help,
    "version": cmd_version,
}


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Moddy - helper for the multiloader template",
        formatter_class=argparse.RawTextHelpFormatter,
    )
    parser.add_argument(
        "-y",
        "--yes",
        action="store_true",
        help="Assume yes for all confirmation prompts",
    )
    parser.add_argument("command", nargs="?", help="Command to run")
    parser.add_argument("args", nargs=argparse.REMAINDER)
    return parser


def main(argv=None) -> None:
    parser = build_parser()
    ns, _ = parser.parse_known_args(argv or sys.argv[1:])

    import moddy

    moddy.AUTO_YES = ns.yes
    command = ns.command or "help"

    if command not in COMMANDS:
        parser.print_help()
        return

    func = COMMANDS[command]
    subparser = argparse.ArgumentParser(prog=f"{Path(sys.argv[0]).name} {command}")

    if func is cmd_add_service:
        subparser.add_argument("name", help="Service interface name")
    elif func is cmd_open_libs:
        subparser.add_argument(
            "loader", choices=["fabric", "forge", "neoforge"], help="Loader to open the output for"
        )
    elif func is cmd_open:
        subparser.add_argument(
            "site",
            choices=["curseforge", "modrinth", "github"],
            help="Site to open",
        )
    elif func is cmd_docs:
        subparser.add_argument(
            "target",
            choices=["fabric", "neoforge", "forge", "parchment", "linkie"],
            help="Documentation site to open",
        )
    elif func is cmd_set_minecraft_version:
        subparser.add_argument("version", help="Minecraft version, e.g. 1.21.5")
    elif func is cmd_update:
        subparser.add_argument(
            "version",
            nargs="?",
            help="Moddy version to install (defaults to latest)",
        )

    args = subparser.parse_args(ns.args)
    if func is cmd_help:
        func(args, parser, COMMANDS)
    else:
        func(args)
    if command not in ["update", "ping"]:
        check_for_update()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print()
        sys.exit(1)
PK     �{�ZC�ˈ/  /     moddy/utils.pyfrom pathlib import Path
import platform
import subprocess
import os
import re
import urllib.request


def parse_group(props_path: Path) -> str:
    """Return the 'group' property from a gradle.properties file."""
    text = props_path.read_text(encoding="utf-8")
    m = re.search(r"^group=(.+)$", text, re.MULTILINE)
    if not m:
        raise RuntimeError("Could not determine group property")
    return m.group(1).strip()


def open_dir(path: Path) -> None:
    """Open *path* using the default file manager."""
    system = platform.system()
    if system == "Windows":
        os.startfile(path)  # type: ignore[attr-defined]
    elif system == "Darwin":
        subprocess.run(["open", str(path)], check=False)
    else:
        subprocess.run(["xdg-open", str(path)], check=False)


def open_url(url: str) -> None:
    """Open *url* in the default browser."""
    system = platform.system()
    if system == "Windows":
        os.startfile(url)  # type: ignore[attr-defined]
    elif system == "Darwin":
        subprocess.run(["open", url], check=False)
    else:
        subprocess.run(["xdg-open", url], check=False)


def fetch_url_bytes(url: str, headers=None) -> bytes:
    """Return the raw bytes from *url*."""
    req = urllib.request.Request(url, headers=headers or {})
    with urllib.request.urlopen(req) as resp:
        return resp.read()


def fetch_url_text(url: str, headers=None) -> str:
    """Return the decoded text from *url* assuming UTF-8."""
    return fetch_url_bytes(url, headers).decode("utf-8")
PK     ȥ�Zl.��W  W     moddy/__init__.pyAUTO_YES = False
MODDY_VERSION = "0.15.0"
VERSION_REGISTRY_URL = (
    "https://raw.githubusercontent.com/iamkaf/modresources/main/moddy/versions.json"
)
RAW_BASE_URL = "https://raw.githubusercontent.com/iamkaf/modresources/main"

__all__ = [
    "AUTO_YES",
    "MODDY_VERSION",
    "VERSION_REGISTRY_URL",
    "RAW_BASE_URL",
]
PK     �u�Z���q  q     moddy/commands/add_service.pyfrom __future__ import annotations

import argparse
import re
from pathlib import Path

from ..utils import parse_group
from .. import AUTO_YES


def cmd_add_service(args: argparse.Namespace) -> None:
    """Create a service interface and loader specific implementations."""
    name = args.name.strip()
    if not re.match(r"^[A-Za-z_][A-Za-z0-9_]*$", name):
        raise SystemExit("Illegal characters in service name")

    interface_name = f"I{name}"
    group = parse_group(Path("gradle.properties"))
    pkg_path = group.replace(".", "/")
    service_fqn = f"{group}.platform.services.{interface_name}"

    interface_path = Path("common/src/main/java") / pkg_path / "platform" / "services" / f"{interface_name}.java"
    fabric_impl_path = Path("fabric/src/main/java") / pkg_path / "platform" / f"Fabric{name}.java"
    forge_impl_path = Path("forge/src/main/java") / pkg_path / "platform" / f"Forge{name}.java"
    neo_impl_path = Path("neoforge/src/main/java") / pkg_path / "platform" / f"NeoForge{name}.java"

    fabric_meta = Path("fabric/src/main/resources/META-INF/services") / service_fqn
    forge_meta = Path("forge/src/main/resources/META-INF/services") / service_fqn
    neo_meta = Path("neoforge/src/main/resources/META-INF/services") / service_fqn

    files = {
        interface_path: f"package {group}.platform.services;\n\npublic interface {interface_name} {{\n}}\n",
        fabric_impl_path: f"package {group}.platform;\n\nimport {service_fqn};\n\npublic class Fabric{name} implements {interface_name} {{\n}}\n",
        forge_impl_path: f"package {group}.platform;\n\nimport {service_fqn};\n\npublic class Forge{name} implements {interface_name} {{\n}}\n",
        neo_impl_path: f"package {group}.platform;\n\nimport {service_fqn};\n\npublic class NeoForge{name} implements {interface_name} {{\n}}\n",
        fabric_meta: f"{group}.platform.Fabric{name}\n",
        forge_meta: f"{group}.platform.Forge{name}\n",
        neo_meta: f"{group}.platform.NeoForge{name}\n",
    }

    print(f"This will create a new service called '{interface_name}'.")
    print("The following files will be created:\n")
    for path, content in files.items():
        print(f"--- {path}")
        print(content.rstrip())
        print()

    existing = [str(p) for p in files if p.exists()]
    if existing:
        print("The following files already exist and will not be overwritten:")
        for p in existing:
            print(f"  {p}")
        return

    if not AUTO_YES and input("Proceed? [y/N] ").lower() != "y":
        print("Aborted")
        return

    for path, content in files.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        print(f"Created {path}")

    services_path = Path("common/src/main/java") / pkg_path / "platform" / "Services.java"
    if services_path.exists():
        text = services_path.read_text(encoding="utf-8")
        import_line = f"import {service_fqn};"
        if import_line not in text:
            m = re.search(r"(package[^\n]+;\n)", text)
            if m:
                insert_pos = m.end()
                text = text[:insert_pos] + import_line + "\n" + text[insert_pos:]
            else:
                text = import_line + "\n" + text
        field_name = name[:1].lower() + name[1:]
        field_line = f"    public static {interface_name} {field_name} = load({interface_name}.class);"
        if field_line not in text:
            idx = text.rfind("}")
            if idx != -1:
                text = text[:idx].rstrip() + "\n" + field_line + "\n}" + text[idx+1:]
            else:
                text += "\n" + field_line + "\n"
        services_path.write_text(text, encoding="utf-8")
        print(f"Updated {services_path}")
    else:
        print(f"Services class not found at {services_path}; skipping update.")
PK     �u�Z��L��  �     moddy/commands/changelog.pyfrom __future__ import annotations

import argparse
import json

from ..utils import fetch_url_text
from .. import VERSION_REGISTRY_URL


def cmd_changelog(args: argparse.Namespace) -> None:
    """Print the changelog for the last 3 Moddy versions."""
    try:
        registry = json.loads(fetch_url_text(VERSION_REGISTRY_URL))
    except Exception as e:
        print(f"Failed to fetch changelog: {e}")
        return

    for entry in registry[:3]:
        version = entry.get("version", "unknown")
        print(f"Version {version}:")
        notes = entry.get("notes", [])
        for note in notes:
            print(f" - {note}")
        print()
PK     �u�Z��&�5  5     moddy/commands/docs.pyfrom __future__ import annotations

import argparse

from ..utils import open_url


DOC_URLS = {
    "fabric": "https://docs.fabricmc.net/develop/",
    "neoforge": "https://docs.neoforged.net/docs/gettingstarted/",
    "forge": "https://docs.minecraftforge.net/en/1.21.x/",
    "parchment": "https://parchmentmc.org/docs/getting-started",
    "linkie": "https://linkie.shedaniel.dev/mappings",
}


def cmd_docs(args: argparse.Namespace) -> None:
    """Open the selected documentation page."""
    url = DOC_URLS[args.target]
    open_url(url)
PK     w�Zƃ�MM  M     moddy/commands/meta.pyfrom __future__ import annotations

import argparse
import json

from ..utils import fetch_url_text
from .. import MODDY_VERSION, VERSION_REGISTRY_URL


def cmd_help(args: argparse.Namespace, parser, commands) -> None:
    """Show available commands."""
    parser.print_help()
    print("\nCommands:")
    for name, func in commands.items():
        if func is cmd_help:
            continue
        doc = (func.__doc__ or "").strip().splitlines()[0]
        print(f"  {name:<22} {doc}")


def cmd_version(args: argparse.Namespace) -> None:
    """Print Moddy's version."""
    print(MODDY_VERSION)


def check_for_update() -> None:
    try:
        registry = json.loads(fetch_url_text(VERSION_REGISTRY_URL))
        latest_version = registry[0].get("version")
    except Exception:
        return
    if latest_version and latest_version != MODDY_VERSION:
        YELLOW = "\033[33m"
        RESET = "\033[0m"
        print(f"{YELLOW}A new Moddy version ({MODDY_VERSION} -> {latest_version}) is available.{RESET}")
        print("Run 'moddy update' to update.")
PK     �u�Zl�۶  �     moddy/commands/open.pyfrom __future__ import annotations

import argparse
import re
from pathlib import Path

from ..utils import open_url


def cmd_open(args: argparse.Namespace) -> None:
    """Open the mod page on the selected site."""
    props_path = Path("gradle.properties")
    mod_id = "examplemod"
    author = "yourname"
    if props_path.is_file():
        text = props_path.read_text(encoding="utf-8")
        m = re.search(r"^mod_id=(.+)$", text, re.MULTILINE)
        if m:
            mod_id = m.group(1).strip()
        m = re.search(r"^mod_author=(.+)$", text, re.MULTILINE)
        if m:
            author = m.group(1).strip()

    site = args.site
    if site == "curseforge":
        url = f"https://www.curseforge.com/minecraft/mc-mods/{mod_id}"
    elif site == "modrinth":
        url = f"https://modrinth.com/mod/{mod_id}"
    else:  # github
        url = f"https://github.com/{author}/{mod_id}"
    open_url(url)
PK     �u�ZUk�V�  �     moddy/commands/open_libs.pyfrom __future__ import annotations

import argparse
from pathlib import Path

from ..utils import open_dir
from .. import AUTO_YES


def cmd_open_libs(args: argparse.Namespace) -> None:
    """Open the build/libs folder for the chosen loader."""
    libs = Path(args.loader) / "build" / "libs"
    if not libs.is_dir():
        print(f"No libs folder found at {libs}. Run a build first.")
        return
    open_dir(libs)
PK     �u�Z�-�   �      moddy/commands/ping.pyfrom __future__ import annotations

import argparse


def cmd_ping(args: argparse.Namespace) -> None:
    """Print pong to verify Moddy works."""
    print("pong")

PK     ���Z��X��+  �+      moddy/commands/setup_template.pyfrom __future__ import annotations

import argparse
import os
import re
import shutil
import struct
import zlib
from pathlib import Path

from .. import AUTO_YES

RESET = "\033[0m"
GREEN = "\033[32m"
CYAN = "\033[36m"
YELLOW = "\033[33m"

ROOT_IGNORE = {
    ".github",
    ".gradle",
    ".idea",
    "build",
    "buildSrc",
    "gradle",
}

SUBPROJECT_IGNORE = {
    ".gradle",
    "build",
    "runs",
}

OLD_PACKAGE = "com.example.modtemplate"
OLD_MOD_ID = "examplemod"
OLD_MOD_NAME = "Example Mod"
OLD_AUTHOR = "yourname"


def _default_version() -> str:
    try:
        text = Path("gradle.properties").read_text(encoding="utf-8")
        m = re.search(r"^version=(.*)$", text, re.MULTILINE)
        if m:
            return m.group(1).strip()
    except Exception:
        pass
    return "1.0.0"


def _to_camel(value: str) -> str:
    parts = re.split(r"[_\-\s]+", value)
    return "".join(p[:1].upper() + p[1:] if p else "" for p in parts)


def _create_icon(char: str, filename: str) -> None:
    font = {
        'A': [0b00111000, 0b01000100, 0b10000010, 0b10000010, 0b11111110, 0b10000010, 0b10000010, 0b00000000],
        'B': [0b11111100, 0b10000010, 0b10000010, 0b11111100, 0b10000010, 0b10000010, 0b11111100, 0b00000000],
        'C': [0b01111110, 0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b01111110, 0b00000000],
        'D': [0b11111100, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b11111100, 0b00000000],
        'E': [0b11111110, 0b10000000, 0b10000000, 0b11111100, 0b10000000, 0b10000000, 0b11111110, 0b00000000],
        'F': [0b11111110, 0b10000000, 0b10000000, 0b11111100, 0b10000000, 0b10000000, 0b10000000, 0b00000000],
        'G': [0b01111110, 0b10000000, 0b10000000, 0b10001110, 0b10000010, 0b10000010, 0b01111110, 0b00000000],
        'H': [0b10000010, 0b10000010, 0b10000010, 0b11111110, 0b10000010, 0b10000010, 0b10000010, 0b00000000],
        'I': [0b01111100, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b01111100, 0b00000000],
        'J': [0b00111110, 0b00000010, 0b00000010, 0b00000010, 0b10000010, 0b10000010, 0b01111100, 0b00000000],
        'K': [0b10000010, 0b10000100, 0b10001000, 0b10110000, 0b11001000, 0b10000100, 0b10000010, 0b00000000],
        'L': [0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b11111110, 0b00000000],
        'M': [0b10000010, 0b11000110, 0b10101010, 0b10010010, 0b10000010, 0b10000010, 0b10000010, 0b00000000],
        'N': [0b10000010, 0b11000010, 0b10100010, 0b10010010, 0b10001010, 0b10000110, 0b10000010, 0b00000000],
        'O': [0b01111100, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b01111100, 0b00000000],
        'P': [0b11111100, 0b10000010, 0b10000010, 0b11111100, 0b10000000, 0b10000000, 0b10000000, 0b00000000],
        'Q': [0b01111100, 0b10000010, 0b10000010, 0b10000010, 0b10001010, 0b10000100, 0b01111010, 0b00000000],
        'R': [0b11111100, 0b10000010, 0b10000010, 0b11111100, 0b10001000, 0b10000100, 0b10000010, 0b00000000],
        'S': [0b01111100, 0b10000010, 0b10000000, 0b01111100, 0b00000010, 0b10000010, 0b01111100, 0b00000000],
        'T': [0b11111110, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b00000000],
        'U': [0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b10000010, 0b01111100, 0b00000000],
        'V': [0b10000010, 0b10000010, 0b10000010, 0b01000100, 0b01000100, 0b00101000, 0b00010000, 0b00000000],
        'W': [0b10000010, 0b10000010, 0b10000010, 0b10010010, 0b10101010, 0b11000110, 0b10000010, 0b00000000],
        'X': [0b10000010, 0b01000100, 0b00101000, 0b00010000, 0b00101000, 0b01000100, 0b10000010, 0b00000000],
        'Y': [0b10000010, 0b01000100, 0b00101000, 0b00010000, 0b00010000, 0b00010000, 0b00010000, 0b00000000],
        'Z': [0b11111110, 0b00000010, 0b00000100, 0b00001000, 0b00010000, 0b00100000, 0b11111110, 0b00000000],
    }
    w = h = 512
    bg = (66, 135, 245)
    s = w // 16
    ox = (w - 8 * s) // 2
    oy = (h - 8 * s) // 2
    pix = bytearray()
    for y in range(h):
        for x in range(w):
            if char.upper() in font and ox <= x < ox + 8 * s and oy <= y < oy + 8 * s:
                row = font[char.upper()][(y - oy) // s]
                if row & (1 << (7 - (x - ox) // s)):
                    pix += b"\xff\xff\xff\xff"
                    continue
            pix += bytes([bg[0], bg[1], bg[2], 255])
    def chunk(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    raw = b''.join(b'\x00' + pix[i*w*4:(i+1)*w*4] for i in range(h))
    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0))
    data += chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')
    Path(filename).write_bytes(data)


def _replace_template_in_comments(text: str, mod_name: str) -> str:
    comment_regex = re.compile(r"(#.*?$|//.*?$|/\*.*?\*/)", re.MULTILINE | re.DOTALL)

    def repl(match: re.Match) -> str:
        comment = match.group(0)
        return re.sub(r"\b(template|example)\b", mod_name, comment, flags=re.IGNORECASE)

    return comment_regex.sub(repl, text)


def cmd_setup(args: argparse.Namespace) -> None:
    base_package = input(f"Base package [{OLD_PACKAGE}]: ") or OLD_PACKAGE
    mod_id = input(f"Mod id [{OLD_MOD_ID}]: ") or OLD_MOD_ID
    mod_name = input(f"Mod name [{OLD_MOD_NAME}]: ") or OLD_MOD_NAME
    author = input(f"Author [{OLD_AUTHOR}]: ") or OLD_AUTHOR
    version = input(f"Initial version [{_default_version()}]: ") or _default_version()

    class_prefix = _to_camel(mod_name)
    icon_path = Path("common/src/main/resources/icon.png")

    replacements = {
        OLD_PACKAGE: base_package,
        OLD_MOD_ID: mod_id,
        OLD_MOD_NAME: mod_name,
        OLD_AUTHOR: author,
        "TemplateMod": f"{class_prefix}Mod",
        "TemplateFabric": f"{class_prefix}Fabric",
        "TemplateForge": f"{class_prefix}Forge",
        "TemplateNeoForge": f"{class_prefix}NeoForge",
        "TemplateDatagen": f"{class_prefix}Datagen",
    }

    print(
        "This will update package names, identifiers and the changelog in this project."
    )
    if not AUTO_YES and input("Proceed? [y/N] ").lower() != "y":
        print("Aborted")
        return

    print(f"{CYAN}Updating files...{RESET}")
    pkg_roots = [
        Path("common/src/main/java"),
        Path("fabric/src/main/java"),
        Path("forge/src/main/java"),
        Path("neoforge/src/main/java"),
    ]

    old_pkg_path = OLD_PACKAGE.replace(".", "/")
    new_pkg_path = base_package.replace(".", "/")

    for src in pkg_roots:
        if not src.exists():
            continue

        for root, dirs, files in os.walk(src):
            for name in files:
                path = Path(root) / name
                if path.suffix != ".java":
                    continue
                text = path.read_text(encoding="utf-8")
                for old, new in replacements.items():
                    text = text.replace(old, new)
                text = _replace_template_in_comments(text, mod_name)
                path.write_text(text, encoding="utf-8")

        old_pkg_dir = src / old_pkg_path
        if old_pkg_dir.exists():
            new_pkg_dir = src / new_pkg_path
            new_pkg_dir.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(old_pkg_dir), str(new_pkg_dir))
            print(f"{GREEN}Moved{RESET} {old_pkg_dir} -> {new_pkg_dir}")
            parent = old_pkg_dir.parent
            while parent != src and parent.is_dir() and not any(parent.iterdir()):
                parent.rmdir()
                parent = parent.parent

    print(f"{CYAN}Renaming files...{RESET}")
    for path in Path('.').rglob('*'):
        parts = path.parts
        if '.git' in parts:
            continue
        if parts and parts[0] in ROOT_IGNORE:
            continue
        if len(parts) > 1 and parts[1] in SUBPROJECT_IGNORE:
            continue
        if path.is_file():
            new_name = path.name
            if OLD_MOD_ID in new_name:
                new_name = new_name.replace(OLD_MOD_ID, mod_id)
            if OLD_PACKAGE in new_name:
                new_name = new_name.replace(OLD_PACKAGE, base_package)
            if "Template" in new_name:
                new_name = new_name.replace("Template", class_prefix)
            if new_name != path.name:
                new_path = path.with_name(new_name)
                path.rename(new_path)
                print(f"{GREEN}Renamed{RESET} {path} -> {new_path}")
                path = new_path

            # update contents for json, toml and service descriptor files
            if (
                path.suffix in {".json", ".toml", ".mcmeta", ".properties"}
                or "META-INF/services" in path.as_posix()
            ):
                try:
                    text = path.read_text(encoding="utf-8")
                except Exception:
                    text = None
                if text is not None:
                    orig = text
                    for old, new in replacements.items():
                        text = text.replace(old, new)
                    text = _replace_template_in_comments(text, mod_name)
                    if text != orig:
                        path.write_text(text, encoding="utf-8")
                        print(f"{GREEN}Updated{RESET} {path}")

    generated_cache = Path("fabric/src/main/generated")
    if generated_cache.exists():
        shutil.rmtree(generated_cache)
        print(f"{GREEN}Removed{RESET} {generated_cache}")

    props_path = Path("gradle.properties")
    text = props_path.read_text(encoding="utf-8")
    text = re.sub(r"(?m)^version=.*$", f"version={version}", text)
    text = re.sub(r"(?m)^group=.*$", f"group={base_package}", text)
    text = re.sub(r"(?m)^archives_base_name=.*$", f"archives_base_name={mod_id}", text)
    props_path.write_text(text, encoding="utf-8")
    print(f"{GREEN}Updated gradle.properties{RESET}")

    chg_path = Path("changelog.md")
    chg_lines = chg_path.read_text(encoding="utf-8").splitlines()
    entry = [f"## {version}", "", "Initial Implementation", ""]
    try:
        def_idx = chg_lines.index("## 1.0.0")
        end_idx = def_idx + 1
        while end_idx < len(chg_lines) and not chg_lines[end_idx].startswith("## "):
            end_idx += 1
        del chg_lines[def_idx:end_idx]
    except ValueError:
        pass
    try:
        idx = chg_lines.index("## Types of changes")
    except ValueError:
        idx = len(chg_lines)
    chg_lines[idx:idx] = entry
    chg_path.write_text("\n".join(chg_lines) + "\n", encoding="utf-8")
    print(f"{GREEN}Updated changelog{RESET}")

    if not icon_path.exists():
        _create_icon(mod_name[0], icon_path)
        print(f"{GREEN}Created {icon_path}{RESET}")
    else:
        print(f"{CYAN}Skipped icon generation{RESET}")

    print(f"{GREEN}Template initialized.{RESET}")
PK     �u�Zv���3!  3!  '   moddy/commands/set_minecraft_version.pyfrom __future__ import annotations

import argparse
import json
import re
import urllib.parse
import xml.etree.ElementTree as ET
from pathlib import Path

from ..utils import fetch_url_text
from .. import AUTO_YES


def _get_artifact_latest(meta_url: str, mc_version: str):
    try:
        xml_text = fetch_url_text(meta_url)
    except Exception:
        return None
    versions = re.findall(r"<version>([^<]+)</version>", xml_text)
    prefix = mc_version + "-"
    candidates = [v for v in versions if v.startswith(prefix)]
    if not candidates:
        return None
    stable = [v for v in candidates if "-rc" not in v and "-pre" not in v]
    versions = stable if stable else candidates
    versions.sort()
    return versions[-1]


def _get_neoform_version(mc: str):
    url = (
        "https://maven.neoforged.net/releases/net/neoforged/neoform/maven-metadata.xml"
    )
    return _get_artifact_latest(url, mc)


def _get_neoforge_version(mc: str):
    meta_url = (
        "https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml"
    )
    try:
        xml_text = fetch_url_text(meta_url)
    except Exception:
        return None
    root = ET.fromstring(xml_text)
    mc_prefix = ".".join(mc.split(".")[1:3])
    versions = [
        v.text for v in root.findall("./versioning/versions/version") if v.text.startswith(mc_prefix)
    ]
    if not versions:
        return None
    stable = [v for v in versions if "-beta" not in v and "-rc" not in v]
    versions = stable if stable else versions
    return versions[-1]


def _fetch_parchment_version(mc: str):
    url = f"https://maven.parchmentmc.org/org/parchmentmc/data/parchment-{mc}/maven-metadata.xml"
    try:
        xml_text = fetch_url_text(url)
        root = ET.fromstring(xml_text)
        versions = [v.text for v in root.findall("./versioning/versions/version")]
        versions = [
            v for v in versions if not re.search(r"bleeding|nightly|snapshot", v, re.IGNORECASE)
        ]
        return versions[-1] if versions else None
    except Exception:
        return None


def _get_parchment_version(mc: str):
    current = mc
    while current:
        version = _fetch_parchment_version(current)
        if version:
            return version
        parts = current.split(".")
        if len(parts) < 3 or not parts[2].isdigit():
            break
        patch = int(parts[2]) - 1
        if patch < 0:
            break
        parts[2] = str(patch)
        current = ".".join(parts)
    return None


def _get_fabric_loader_version(mc: str):
    url = f"https://meta.fabricmc.net/v2/versions/loader/{mc}"
    try:
        data = json.loads(fetch_url_text(url))
        stable = [e["loader"]["version"] for e in data if e["loader"].get("stable")]
        if stable:
            def ver_key(v):
                return [int(x) if x.isdigit() else x for x in re.split(r"[.-]", v)]
            return sorted(stable, key=ver_key)[-1]
    except Exception:
        pass
    return None


def _get_fabric_api_version(mc: str):
    query = urllib.parse.quote(f'["{mc}"]', safe="")
    url = f"https://api.modrinth.com/v2/project/fabric-api/version?game_versions={query}"
    try:
        versions = json.loads(fetch_url_text(url))
        latest = max(versions, key=lambda v: v["date_published"])
        return latest["version_number"]
    except Exception:
        return None


def _get_mod_menu_version(mc: str):
    query = urllib.parse.quote(f'["{mc}"]', safe="")
    url = f"https://api.modrinth.com/v2/project/modmenu/version?game_versions={query}"
    try:
        versions = json.loads(fetch_url_text(url))
        latest = max(versions, key=lambda v: v["date_published"])
        return latest["version_number"]
    except Exception:
        return None


def _get_amber_version(mc: str):
    query = urllib.parse.quote(f'["{mc}"]', safe="")
    url = f"https://api.modrinth.com/v2/project/amber/version?game_versions={query}"
    try:
        versions = json.loads(fetch_url_text(url))
        latest = max(versions, key=lambda v: v["date_published"])
        return latest["version_number"]
    except Exception:
        return None


def _get_forge_version(mc: str):
    url = f"https://files.minecraftforge.net/net/minecraftforge/forge/index_{mc}.html"
    try:
        html = fetch_url_text(url, headers={"User-Agent": "Mozilla/5.0"})
    except Exception:
        return None
    for label in ("Recommended", "Latest"):
        m = re.search(label + r":\s*([0-9.]+)", html)
        if m:
            return m.group(1)
    return None


def _collect_versions(mc: str, include_amber: bool = False) -> dict:
    parchment_version = _get_parchment_version(mc)
    parchment_mc = mc
    if parchment_version:
        m = re.match(r"([0-9]+(?:\.[0-9]+){1,2})-", parchment_version)
        if m:
            parchment_mc = m.group(1)

    versions = {
        "neoform_version": _get_neoform_version(mc),
        "neoforge_version": _get_neoforge_version(mc),
        "parchment_minecraft": parchment_mc,
        "parchment_version": parchment_version,
        "fabric_loader_version": _get_fabric_loader_version(mc),
        "fabric_version": _get_fabric_api_version(mc),
        "mod_menu_version": _get_mod_menu_version(mc),
        "forge_version": _get_forge_version(mc),
    }
    if include_amber:
        versions["amber_version"] = _get_amber_version(mc)
    return versions


def _apply_versions(props_path: Path, mc: str, versions: dict) -> None:
    text = props_path.read_text(encoding="utf-8")
    next_minor = mc.split(".")
    if len(next_minor) >= 2:
        try:
            minor = int(next_minor[1])
            next_minor[1] = str(minor + 1)
            next_minor = ".".join(next_minor[:2])
        except Exception:
            next_minor = mc
    else:
        next_minor = mc
    replacements = {
        "minecraft_version": mc,
        "minecraft_version_range": f"[{mc}, {next_minor})",
        "neo_form_version": versions.get("neoform_version"),
        "parchment_minecraft": versions.get("parchment_minecraft") or mc,
        "parchment_version": versions.get("parchment_version"),
        "fabric_loader_version": versions.get("fabric_loader_version"),
        "fabric_version": versions.get("fabric_version"),
        "mod_menu_version": versions.get("mod_menu_version"),
        "forge_version": versions.get("forge_version"),
        "neoforge_version": versions.get("neoforge_version"),
        "amber_version": versions.get("amber_version"),
        "game_versions": mc,
    }
    for key, value in replacements.items():
        if not value:
            continue
        pattern = rf"(?m)^{re.escape(key)}=.*$"
        if re.search(pattern, text):
            text = re.sub(pattern, f"{key}={value}", text)
        else:
            if not text.endswith("\n"):
                text += "\n"
            text += f"{key}={value}\n"
    props_path.write_text(text, encoding="utf-8")


def cmd_set_minecraft_version(args: argparse.Namespace) -> None:
    mc = args.version
    print(f"Fetching dependency versions for Minecraft {mc}.")
    if not AUTO_YES and input("Proceed? [y/N] ").lower() != "y":
        print("Aborted")
        return

    props_text = Path("gradle.properties").read_text(encoding="utf-8")
    versions = _collect_versions(mc, include_amber="amber_version" in props_text)
    print("Fetched versions:")
    for k, v in versions.items():
        print(f"  {k}: {v}")
    missing = [k for k, v in versions.items() if v is None]
    if missing:
        print("\nFailed to determine:", ", ".join(missing))
        print("You can look them up manually at:")
        print("  https://projects.neoforged.net/neoforged/neoform")
        print("  https://projects.neoforged.net/neoforged/neoforge")
        print("  https://fabricmc.net/develop/")
        print("  https://files.minecraftforge.net/net/minecraftforge/forge/")
        print("  https://parchmentmc.org/docs/getting-started#choose-a-version")
    answer = "y" if AUTO_YES else input("\nApply these versions to gradle.properties? [y/N] ")
    if answer.lower().startswith("y"):
        _apply_versions(Path("gradle.properties"), mc, versions)
        print("Updated gradle.properties")
    else:
        print("No changes made")
PK     ���ZD�1b  b     moddy/commands/update.pyfrom __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

import io
import zipfile
import hashlib

from ..utils import fetch_url_text, fetch_url_bytes
from .. import AUTO_YES, MODDY_VERSION, VERSION_REGISTRY_URL, RAW_BASE_URL


def cmd_update(args: argparse.Namespace) -> None:
    """Download a specific Moddy version and replace this file."""
    print(
        "! ! ! WARNING ! ! !: Executing this command will fetch Python "
        "code from the internet and run it on your computer."
    )
    print(
        "Only continue if you trust the source. The update is pulled from the "
        "official Moddy repository: https://github.com/iamkaf/modresources"
    )
    print(
        "You can inspect the downloaded file to learn how the update works "
        "before proceeding."
    )
    try:
        registry = json.loads(fetch_url_text(VERSION_REGISTRY_URL))
    except Exception as e:
        print(f"Failed to check for updates: {e}")
        return

    target_version = getattr(args, "version", None)
    entry = None
    if target_version:
        for item in registry:
            if item.get("version") == target_version:
                entry = item
                break
        if not entry:
            print(f"Version {target_version} not found in registry")
            return
    else:
        entry = registry[0]

    update_url = RAW_BASE_URL + entry.get("source", "")

    print(f"Registry: {VERSION_REGISTRY_URL}")
    print(f"Source: {update_url}")
    if not AUTO_YES and input("Are you sure you want to continue? [y/N] ").lower() != "y":
        print("Aborted")
        return

    try:
        new_data = fetch_url_bytes(update_url)
    except Exception as e:
        print(f"Failed to download update: {e}")
        return

    expected_hash = entry.get("hash") if isinstance(entry, dict) else None
    if expected_hash:
        actual_hash = hashlib.sha256(new_data).hexdigest()
        if actual_hash != expected_hash:
            print(
                "Update verification failed: hash mismatch."\
                f" Expected {expected_hash}, got {actual_hash}"
            )
            return

    # Verify the downloaded code by running the ping command
    with tempfile.NamedTemporaryFile("wb", delete=False, suffix=".py") as tmp:
        tmp.write(new_data)
        tmp_path = Path(tmp.name)
    try:
        result = subprocess.run(
            [sys.executable, str(tmp_path), "ping"],
            capture_output=True,
            text=True,
            check=True,
        )
    except subprocess.CalledProcessError:
        print("Update verification failed. The downloaded file did not run correctly.")
        print("If this issue persists, please report it at https://github.com/iamkaf/modresources/issues")
        try:
            tmp_path.unlink()
        except OSError:
            pass
        return
    if not result.stdout.startswith("pong"):
        print("Update verification failed. Unexpected ping output.")
        print("If this issue persists, please report it at https://github.com/iamkaf/modresources/issues")
        try:
            tmp_path.unlink()
        except OSError:
            pass
        return
    try:
        tmp_path.unlink()
    except OSError:
        pass

    if new_data.startswith(b"PK"):
        try:
            with zipfile.ZipFile(io.BytesIO(new_data)) as zf:
                init_text = zf.read("moddy/__init__.py").decode("utf-8")
        except Exception:
            init_text = ""
    else:
        init_text = new_data.decode("utf-8", errors="ignore")

    m = re.search(r"MODDY_VERSION\s*=\s*['\"]([^'\"]+)['\"]", init_text)
    new_version = m.group(1) if m else "unknown"
    if new_version == MODDY_VERSION:
        print("Moddy is already up to date.")
        return

    # Determine the path of the script that launched Moddy. When running from
    # the distributed "moddy.py" zipapp, ``__file__`` points to a location
    # inside the archive like ``moddy.py/moddy/commands/update.py`` which does
    # not exist on disk. ``sys.argv[0]`` however contains the actual path to the
    # executable zip file. When running from sources ``sys.argv[0]`` is the main
    # module path which is also valid to overwrite. Fallback to ``__file__`` in
    # the unlikely event ``sys.argv[0]`` isn't a file.

    script_path = Path(sys.argv[0]).resolve()
    if not script_path.exists():
        script_path = Path(__file__).resolve()
    backup = script_path.with_suffix(".bak")
    try:
        shutil.copy2(script_path, backup)
        script_path.write_bytes(new_data)
    except Exception as e:
        print(f"Update failed: {e}")
        return
    print(f"Updated Moddy from {MODDY_VERSION} to {new_version}")
    print(f"A backup of the previous version was saved to {backup}")
    notes = entry.get("notes", []) if isinstance(entry, dict) else []
    if notes:
        print(f"Changelog for {new_version}:")
        for n in notes:
            print(f" - {n}")
PK     �u�ZF:�  �     moddy/commands/__init__.pyfrom .add_service import cmd_add_service
from .open_libs import cmd_open_libs
from .open import cmd_open
from .docs import cmd_docs
from .set_minecraft_version import cmd_set_minecraft_version
from .setup_template import cmd_setup
from .update import cmd_update
from .ping import cmd_ping
from .changelog import cmd_changelog
from .meta import cmd_help, cmd_version, check_for_update

__all__ = [
    "cmd_add_service",
    "cmd_open_libs",
    "cmd_open",
    "cmd_docs",
    "cmd_set_minecraft_version",
    "cmd_setup",
    "cmd_update",
    "cmd_ping",
    "cmd_changelog",
    "cmd_help",
    "cmd_version",
    "check_for_update",
]
PK     ȥ�Z"-{�<   <      __main__.py# -*- coding: utf-8 -*-
import moddy.main
moddy.main.main()
PK      ȥ�Z                      �A    moddy/PK      ȥ�Z                      �A$   moddy/commands/PK      ���ZU��A�  �             ��Q   moddy/main.pyPK      �{�ZC�ˈ/  /             ��H  moddy/utils.pyPK      ȥ�Zl.��W  W             ���  moddy/__init__.pyPK      �u�Z���q  q             ��)  moddy/commands/add_service.pyPK      �u�Z��L��  �             ���$  moddy/commands/changelog.pyPK      �u�Z��&�5  5             ���'  moddy/commands/docs.pyPK      w�Zƃ�MM  M             ��*  moddy/commands/meta.pyPK      �u�Zl�۶  �             ���.  moddy/commands/open.pyPK      �u�ZUk�V�  �             ���2  moddy/commands/open_libs.pyPK      �u�Z�-�   �              ��w4  moddy/commands/ping.pyPK      ���Z��X��+  �+              ��Y5  moddy/commands/setup_template.pyPK      �u�Zv���3!  3!  '           ��ga  moddy/commands/set_minecraft_version.pyPK      ���ZD�1b  b             ��߂  moddy/commands/update.pyPK      �u�ZF:�  �             ��w�  moddy/commands/__init__.pyPK      ȥ�Z"-{�<   <              �G�  __main__.pyPK      ~  ��    