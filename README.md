# semantic-version

**semantic-version** is a lightweight Maven plugin that automates changelog generation and project versioning based on
semantic versioning principles.

## Features

This plugin provides two main commands:

---

### 🔧 `changelog`

Generates or updates a `CHANGELOG.md` file using Git history.

* Supports a `dryRun` option to preview the next changelog without writing to disk.
* Supports a scope option to include only commits containing a specific scope.

```bash
mvn io.github.zorin95670:semantic-version:changelog -DtagPrefix=plugin-a@ -Dscope=plugin-a
# Example: 
# chore(plugin-a): Test -> Included
# chore(plugin-b): Test -> Ignored
```

* Supports a `tagPrefix` option to filter Git tags by prefix when generating the changelog (e.g., only include tags like `plugin-a@1.2.0`). Default value is `v`


```bash
mvn io.github.zorin95670:semantic-version:changelog -DtagPrefix=plugin-a@
```

Only tags starting with the specified `tagPrefix` will be considered when generating the changelog.

---

### 🚀 `release`

Performs the following operations:

* Determines the next semantic version based on commit history.
* **Only includes commits since the last tag that starts with the provided `tagPrefix`**.
* Updates the version in the `pom.xml`.
* Generates or updates the `CHANGELOG.md`.
* Commits the changes.
* Creates a Git tag for the new release with the specified prefix.

```bash
mvn io.github.zorin95670:semantic-version:release -DtagPrefix=plugin-a@ -Dscope=plugin-a
```

When `tagPrefix` is specified:

* The next version is determined **only from commits** since the **last tag that starts with that prefix**.
* The generated tag will also include the prefix (e.g., `plugin-a@1.2.0`).
* Default value is `v` 

When `scope` is specified:

* Only commits that include the scope `(plugin-a)` will be considered for version bumping and changelog generation.
* This is useful for monorepos or multi-module projects where only a subset of commits should affect a specific release.

> ℹ️ After running the `release` goal, make sure to **push the commit and the tag** manually:

```bash
git push origin main
git push origin --tags
```

---

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
* Can be configured to fail the build if any invalid commit is found using the `failOnWarning` parameter.
* Can skip merge commits by enabling the `noMerge` parameter.

```bash
mvn io.github.zorin95670:semantic-version:0.3.0:check-commit \
  -DfailOnWarning=true \
  -DnoMerge=true
```

---

## Usage

Add the plugin to your project or run it using the CLI:

```bash
mvn io.github.zorin95670:semantic-version:check-commit
mvn io.github.zorin95670:semantic-version:check-commit -DfailOnWarning=true
mvn io.github.zorin95670:semantic-version:changelog
mvn io.github.zorin95670:semantic-version:changelog -DdryRun=true
mvn io.github.zorin95670:semantic-version:changelog -DtagPrefix=plugin-a@
mvn io.github.zorin95670:semantic-version:release -DtagPrefix=plugin-a@
```

---

## Version Bumping Rules

The plugin determines the next semantic version based on commit messages using the following logic:

### 🔼 Major version bump

Incremented if:

* The commit contains `BREAKING CHANGE` or `BREAKING-CHANGE` in the body or footer.
* The commit header includes a `!` after the type (e.g., `feat!:`, `fix!:`).

### 🔼 Minor version bump

Incremented if:

* The commit type is `feat`.

### 🔼 Patch version bump

Incremented if:

* The commit type is `fix`.

### No version bump

If only commits of type `chore`, `style`, `refactor`, etc., are found, the version remains unchanged.

Only the highest-priority rule applies per release.

---

## Commit Types

The plugin categorizes commits into changelog sections based on conventional commit prefixes:

| Section        | Supported Prefixes                         |
| -------------- | ------------------------------------------ |
| **Added**      | `feat`                                     |
| **Fixed**      | `fix`                                      |
| **Changed**    | `perf`, `refactor`, `style`, `ci`, `build` |
| **Removed**    | `removed`                                  |
| **Security**   | `security`                                 |
| **Deprecated** | `deprecated`                               |

Example of a valid commit:

```text
feat: add support for dry-run option
```

Invalid or unrecognized prefixes will be ignored in the changelog.
