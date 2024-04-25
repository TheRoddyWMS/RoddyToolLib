# RoddyToolLib

[![CircleCI](https://circleci.com/gh/TheRoddyWMS/RoddyToolLib/tree/master.svg?style=svg)](https://circleci.com/gh/TheRoddyWMS/RoddyToolLib/tree/master)

Tool library used in [BatchEuphoria](https://github.com/TheRoddyWMS/BatchEuphoria) and [Roddy](https://github.com/TheRoddyWMS/Roddy).

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

## Changelog

* Next

  - patch: Upgrade to Gradle 8.3
  - patch: Added `listConfigurations`, `allBoms`, and a `...Bom` task for every Gradle configuration set. The `allBoms` and `...Bom` tasks generate JSON CycloneDX SBOMs in `gradleBuild/reports/cyclonedx`.
  - patch: Added cyclonedx SBOM and test-report tarballs to GitHub releases

* 2.4.1

   - Patch: Library update
   - Patch: Changed one logging message
   - Patch: Fixed a unit test
   - Patch: Code layout refactorings and groovification

* 2.4.0

  - Minor: Improved reporting of stdout and stderr for executed commands
  - Minor: Renamed `asExecutionResult` to `asSynchronousExecutionResult`
  - Patch: Use Circle-CI instead of Travis-CI

* 2.3.0

  - Minor: Refactored `AsyncExecutionResult` and `ExecutionResult` to improve the stdout and stderr handling. This also affects `ExecutionResult.resultLines` field/accessors.
  - Minor: Improved support for additional output stream in `LocalExecutionHelper`
  - Changes are required for Roddy 3.6.1 improvements related to better error reporting and handling.

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