# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

See the full changelog at https://github.com/RockyyyFOG/RockyLib

## Unreleased

### Added

- Added support for Forge.
- Added a DeferredRegister system to allow for registering items, blocks, and entities in a more structured way.
- Added a new RockyLibInitializer class to init mods.
- Added a new JSON config system.
- Added events to register commands.
- Added SimpleCommands helper to register commands easily.
- Added a KeybindHelper to register keybinds.
- Added events to render to the HUD.
- Added the `/rockylib doctor` command to diagnose issues with the mod.

### Changed

- TBD

### Deprecated

- AmberMod is now deprecated in favor of AmberInitializer.
- All unversioned API helper classes have been deprecated in preparation for versioned packages.
- JsonFileReader is now deprecated in favor of a new JSON system.

### Removed

- TBD

### Fixed

- Fixed a bug where the mod would not load properly on some platforms.
- FORGE: Fixed a bug preventing interactions with entities.

## 1.26.2

### Added

- Added support for 1.21.6

## 1.0.0-alpha.1

- Initial Release

## Types of changes

- `Added` for new features.
- `Changed` for changes in existing functionality.
- `Deprecated` for soon-to-be removed features.
- `Removed` for now removed features.
- `Fixed` for any bug fixes.
- `Security` in case of vulnerabilities.