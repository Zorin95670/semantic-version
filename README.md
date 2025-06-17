# semantic-version

**semantic-version** is a lightweight Maven plugin that automates changelog generation and project versioning based on
semantic versioning principles.

## Features

This plugin provides two main commands:

### 🔧 `changelog`

Generates or updates a `CHANGELOG.md` file using Git history.

- Supports a `dryRun` option to preview the next changelog without writing to disk.

### 🚀 `release`

Performs the following operations:

- Determines the next semantic version based on commit history.
- Updates the version in the `pom.xml`.
- Generates or updates the `CHANGELOG.md`.
- Commits the changes.
- Creates a Git tag for the new release.

After running the `release` goal, make sure to **push the commit and the tag** manually:

```bash
git push origin main
git push origin --tags
```

### ✅ `check-commits`

Validates commit messages against [Conventional Commits](https://www.conventionalcommits.org/) and custom rules:

| Section        | Supported Prefixes                         |
| -------------- | ------------------------------------------ |
| **Added**      | `feat`                                     |
| **Fixed**      | `fix`                                      |
| **Changed**    | `perf`, `refactor`, `style`, `ci`, `build` |
| **Removed**    | `removed`                                  |
| **Security**   | `security`                                 |
| **Deprecated** | `deprecated`                               |

* Logs a warning for any invalid commit prefix.
* Can be configured to fail the build if any invalid commit is found using the `failOnWarning` parameter:

## Usage

Add the plugin to your project or run it using the CLI:

```bash
mvn io.github.zorin95670:semantic-version:check-commit
mvn io.github.zorin95670:semantic-version:check-commit -DfailOnWarning=true
mvn io.github.zorin95670:semantic-version:changelog
mvn io.github.zorin95670:semantic-version:changelog -DdryRun=true
mvn io.github.zorin95670:semantic-version:release
```

## Version Bumping Rules

The plugin determines the next semantic version based on commit messages using the following logic:

### 🔼 Major version bump

The major version is incremented if:

- The commit contains `BREAKING CHANGE` or `BREAKING-CHANGE` in the body or footer.
- The commit header includes a `!` after the type, e.g.:

```
feat!: introduce breaking change
fix!: drop support for old behavior
```

### 🔼 Minor version bump

The minor version is incremented if:

- The commit type is `feat` (feature addition) without a breaking change.

Example:

```
feat: add changelog dry-run option
```

### 🔼 Patch version bump

The patch version is incremented if:

- The commit type is `fix`.

Example:

```
fix: correct issue with release tag
```

### No version bump

Commits with other types (e.g. `chore`, `style`, `refactor`, `ci`) will not trigger a version change.

Only the highest-priority rule applies per release.  
For example, if both a `feat` and a `BREAKING CHANGE` are found, the major version will be incremented.

## Commit Types

The plugin categorizes commits into changelog sections based on conventional commit prefixes.  
Each commit must start with a valid type to be included correctly.

| Section        | Supported Prefixes                         |
|----------------|--------------------------------------------|
| **Added**      | `feat`                                     |
| **Fixed**      | `fix`                                      |
| **Changed**    | `perf`, `refactor`, `style`, `ci`, `build` |
| **Removed**    | `removed`                                  |
| **Security**   | `security`                                 |
| **Deprecated** | `deprecated`                               |

Example of a properly formatted commit:

```
feat: add support for dry-run option
```

Invalid or unrecognized prefixes will be ignored in the changelog.
