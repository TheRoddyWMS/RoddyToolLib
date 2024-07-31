# RoddyToolLib

[![Build Status - Travis](https://travis-ci.org/TheRoddyWMS/RoddyToolLib.svg?branch=master)](https://travis-ci.org/TheRoddyWMS/RoddyToolLib)

Roddy's tool library is used in [BatchEuphoria](https://github.com/TheRoddyWMS/BatchEuphoria) and [Roddy](https://github.com/TheRoddyWMS/Roddy).

> This software is for research-use only (RUO).

## Build

Building is as simple as

```bash
./gradlew build
```

If you are behind a firewall and need to access the internet via a proxy, you can configure the proxy in `$HOME/.gradle/gradle.properties`:

```groovy
systemProp.http.proxyHost=HTTP_proxy
systemProp.http.proxyPort=HTTP_proxy_port
systemProp.https.proxyHost=HTTPS_proxy
systemProp.https.proxyPort=HTTPS_proxy_port
```

where you substitute the correct proxies and ports required for your environment.


## Contributors Information

The full developer information is available in the [Roddy documentation](https://roddy-documentation.readthedocs.io/en/latest/roddyDevelopment/developersGuide.html).

Some basic information:

* We use [Semantic Versioning 2.0](https://semver.org/).
   * Release versions are named according to the pattern `\d\.\d\.\d(-(RC)?\d+`.
   * The first three levels are the "major", "minor", and "patch" number. The patch number is occasionally also called "build" number.
   * Additionally, to the major, minor, and patch numbers, a "revision" number `-\d+` can be attached.
   * It is possible to tag release candidate using suffixes `-RC\d+`
* We use [Github-Flow](https://githubflow.github.io/) as branching models.
* Additional to the "master" branch for long-term support of older versions it is possible to have dedicated release branches.
   * Release branches should be named according to the pattern `ReleaseBranch_\d+\.\d+(\.\d+)`.
* Issues can be marked with the following labels
  * `in progress`:
  * `bug::candidate`:
  * `bug::minor`:
  * `bug::normal`:
  * `bug::critical`:

## Change-Log

Change-Log entries have the form

```markdown
* $version
   * major: A change that breaks backwards compatibility
   * minor: A change that adds features, without breaking backwards compatibility
   * patch: A change that does neither add a feature, nor breaks backwards compatibility
```

* Next

  - patch: Added `listConfigurations`, `allBoms`, and a `...Bom` task for every Gradle configuration set. The `allBoms` and `...Bom` tasks generate JSON CycloneDX SBOMs in `gradleBuild/reports/cyclonedx`.

* 2.2.2

  - Patch: Moved from `StringBuilder` to `StringBuffer` in `LocalExecutionHelper`.

* 2.2.1

  - Minor: Made `ExecutionResult.resultLines` protected (from private)

* 2.2.0

  - Minor: Added `AsyncExecutionResult`
  - Minor: Cornercase bugfix `RoddyConversionHelperMethods`

* 2.1.1

  - Patch: Update to Gradle 5.1.1

* 2.1.0

  - Minor: Added `DateTimeHelper`
  - Minor: Added `convertMapToTwoColumnsPrettyTable`

* 2.0.0

  - Minor: Bash expression handling (escaping)
  - Minor: `BufferValue` is value type (implements `hashCode` and `equals`)
  - Patch: Code groovyfication

* 1.0.0

  - Major: Moved package from `eilslabs` to `theroddywms`
  - Minor: Added version management code
     * `CompatibilityChecker`
     * Improved version patterns
     * `VersionInterval`
  - Patch: Update Gradle version from 4.2.1 to 4.8

* 0.0.7

  - Added `AsyncExecutionResult` to handle asynchronous execution of command executions.
