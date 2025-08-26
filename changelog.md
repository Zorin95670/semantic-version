# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.3] - 2025-08-26

### Fixed

- add working dir to generate changelog and update valid pom

## [0.4.2] - 2025-08-26

### Fixed

- use basedir instead of project.basedir

## [0.4.1] - 2025-08-25

### Fixed

- bad filter when scope is providing

## [0.4.0] - 2025-07-16

### Added

- add noMerge option on check commit

### Fixed

- bad repository initialization

## [0.3.0] - 2025-07-15

### Added

- filter commit by scope to calculated next version
- add option to choose tag prefix

### Fixed

- set default value on option when it's not use

## [0.2.0] - 2025-06-17

### Added

- add new command to check-commit message

## [0.1.3] - 2025-06-17

### Fixed

- remove 'v' from version when upgrading pom.xml version

## [0.1.2] - 2025-06-14

### Fixed

- bad naming of version in changelog section

## [0.1.1] - 2025-06-14

### Fixed

- bad scm information in pom.xml
- correct template generation for changelog
- create annotated tag after amending commit to ensure tag points to latest commit

## [0.1.0] - 2025-06-14

### Added

- add command to release
- add command to generate changelog

### Fixed

- configure git remote with token to allow push in GitHub Actions

### Changed

- configure git push with GITHUB_TOKEN for automated release
- extract Maven version from pom.xml to use in GitHub Actions release step
- upgrade actions/cache to v4 to fix deprecation warning


[0.4.3]: https://github.com/Zorin95670/semantic-version/compare/v0.4.2...v0.4.3
[0.4.2]: https://github.com/Zorin95670/semantic-version/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/Zorin95670/semantic-version/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/Zorin95670/semantic-version/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Zorin95670/semantic-version/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Zorin95670/semantic-version/compare/v0.1.3...v0.2.0
[0.1.3]: https://github.com/Zorin95670/semantic-version/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/Zorin95670/semantic-version/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/Zorin95670/semantic-version/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/Zorin95670/semantic-version/releases/tag/v0.1.0
