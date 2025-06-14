# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v0.1.0] - 2025-06-14

### Added

- add command to release
- add command to generate changelog

### Fixed

- configure git remote with token to allow push in GitHub Actions

### Changed

- configure git push with GITHUB_TOKEN for automated release
- extract Maven version from pom.xml to use in GitHub Actions release step
- upgrade actions/cache to v4 to fix deprecation warning
[0.1.0]: https://github.com/Zorin95670/semantic-version/releases/tag/v0.1.0
