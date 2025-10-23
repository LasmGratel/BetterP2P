# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.5.2] - 2025-10-23

### Fixed

- Better Memory Card now selects P2P tunnel and type correctly (#30)

## [1.5.1] - 2025-10-18

### Added

- NeoForge Support by heisluft
- Update to 1.21.1

### Changed
 - Migrate from Artifactory loom to NeoGradle
 - Update AE2 dependency
 - Update Gradle
 - Update KotlinForForge
 - Migrate Network stack to Stream Codecs
 - Migrate ItemNBT to Data Attachments
 - Migrate Event Handling to plain NeoForge
 - Replace Mixin with Event-based initialization
 - Replace redundant network worker with NeoForge Network

### Removed
 - Fabric Support
 - Forge Support
 - Architectury dependency

## [1.5.0] - 2024-12-23

### Added

- MAE2 Support
- Applied Mekanistics Support

### Changed

- Minimum AE2 version requires 15.3.0-beta
- Default Mode is set to "Bind Input"
- While in "Bind Input" mode, unbound P2P tunnels are sorted on the top of the list

### Fixed

- Massive P2P tunnels cause lag. Now only render outlines of <= 200 tunnels and in 50m range of player

## [1.4.3] - 2024-09-19

### Fixed

- Rare issue that crashes client when drawing outline (#19)

## [1.4.2] - 2024-07-23

### Added

- Crowdin integration
- Chinese (Simplified) localization
- Search bar tooltip

### Fixed

- Recipe for Advanced Memory Card

### Removed

- Cleanup unused code


## [1.4.1] - 2024-05-21

### Fixed

- Fixed a issue on Forge side that took over vanilla Block Outline render (#11).

## [1.4.0] - 2024-05-15

### Added

- 1.20.1 Support.

[1.4.2]: https://github.com/LasmGratel/BetterP2P/releases/tag/v1.4.2
[1.4.1]: https://github.com/LasmGratel/BetterP2P/releases/tag/v1.4.1
[1.4.0]: https://github.com/LasmGratel/BetterP2P/releases/tag/v1.4.0
