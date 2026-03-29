# Requirements Document

## Introduction

This document defines the requirements for Phase 2 of the UI automation framework targeting [https://www.saucedemo.com](https://www.saucedemo.com). Phase 2 establishes the Selenide runtime configuration, the `BaseTest` lifecycle class, and the failure-capture pipeline (screenshots + page source via Allure). All subsequent test phases depend on these components being correctly wired.

---

## Glossary

- **ConfigProvider**: A final utility class that reads `config/config.properties` from the classpath and exposes typed accessors; system properties override file values.
- **BaseTest**: A JUnit 5 abstract class that every concrete test class extends; owns Selenide bootstrap and teardown.
- **Selenide_Configuration**: The static `com.codeborne.selenide.Configuration` object that controls browser, baseUrl, headless mode, and timeout for all Selenide operations.
- **AllureSelenide**: The `io.qameta.allure.selenide.AllureSelenide` listener that captures screenshots and page source on test failure.
- **AllureJunit5**: The `io.qameta.allure.junit5.AllureJunit5` JUnit 5 extension that integrates Allure reporting with the JUnit lifecycle.
- **WebDriverRunner**: The Selenide utility class used to inspect and close active WebDriver sessions.
- **Browser**: The web browser used to execute tests, identified by a string value (e.g., `chrome`, `firefox`).
- **Headless**: A browser execution mode with no visible UI, used in CI environments.

---

## Requirements

### Requirement 1: Selenide Configuration Initialisation

**User Story:** As a framework developer, I want Selenide to be configured from a central properties source before each test, so that all tests run against a consistent, environment-aware browser setup.

#### Acceptance Criteria

1. WHEN `BaseTest.setUp()` is called, THE `Selenide_Configuration` SHALL have `baseUrl` set to the value returned by `ConfigProvider.getBaseUrl()`.
2. WHEN `BaseTest.setUp()` is called, THE `Selenide_Configuration` SHALL have `browser` set to the value returned by `ConfigProvider.getBrowser()`.
3. WHEN `BaseTest.setUp()` is called, THE `Selenide_Configuration` SHALL have `headless` set to the value returned by `ConfigProvider.isHeadless()`.
4. WHEN `BaseTest.setUp()` is called, THE `Selenide_Configuration` SHALL have `timeout` set to the value returned by `ConfigProvider.getTimeoutMs()`.
5. THE `ConfigProvider` SHALL read `base.url`, `browser`, `headless`, and `timeout.ms` keys from `config/config.properties` on the classpath.
6. WHEN a system property matching a config key is present, THE `ConfigProvider` SHALL use the system property value instead of the file value.
7. IF `config/config.properties` is absent from the classpath, THEN THE `ConfigProvider` SHALL throw an `IllegalStateException` with a descriptive message identifying the missing file.

---

### Requirement 2: Default Configuration Values

**User Story:** As a framework developer, I want sensible defaults in `config.properties`, so that tests run locally without requiring any command-line flags.

#### Acceptance Criteria

1. THE `ConfigProvider` SHALL return `https://www.saucedemo.com` as the default base URL when no override is provided.
2. THE `ConfigProvider` SHALL return `chrome` as the default browser when no override is provided.
3. THE `ConfigProvider` SHALL return `true` as the default headless value when no override is provided.
4. THE `ConfigProvider` SHALL return `10000` milliseconds as the default timeout when no override is provided.
5. WHEN a `-Dbrowser=firefox` system property is supplied, THE `ConfigProvider` SHALL return `firefox` from `getBrowser()`.

---

### Requirement 3: BaseTest Lifecycle — Setup

**User Story:** As a test author, I want a `BaseTest` class that handles Selenide initialisation before each test, so that I do not need to repeat configuration code in every test class.

#### Acceptance Criteria

1. THE `BaseTest` SHALL be an abstract class annotated with `@ExtendWith(AllureJunit5.class)`.
2. WHEN a test method begins, THE `BaseTest` SHALL execute `setUp()` annotated with `@BeforeEach` before the test body runs.
3. WHEN `setUp()` executes, THE `BaseTest` SHALL apply all four `Selenide_Configuration` fields (`baseUrl`, `browser`, `headless`, `timeout`) using values from `ConfigProvider`.
4. WHEN `setUp()` executes, THE `BaseTest` SHALL register the `AllureSelenide` listener with screenshots enabled and page source capture enabled.
5. WHILE `setUp()` is executing, THE `BaseTest` SHALL NOT open a browser window (Selenide opens the browser lazily on the first `open()` call).

---

### Requirement 4: BaseTest Lifecycle — Teardown

**User Story:** As a test author, I want the browser to be closed automatically after each test, so that no WebDriver processes leak between test runs.

#### Acceptance Criteria

1. WHEN a test method completes (pass or fail), THE `BaseTest` SHALL execute `tearDown()` annotated with `@AfterEach`.
2. WHEN `tearDown()` executes, THE `BaseTest` SHALL call `Selenide.closeWebDriver()` to terminate all active browser sessions.
3. AFTER `tearDown()` completes, THE `WebDriverRunner` SHALL report no active WebDriver session (`WebDriverRunner.hasWebDriverStarted()` returns `false`).
4. IF a test throws an exception, THEN THE `BaseTest` SHALL still execute `tearDown()` (guaranteed by JUnit 5 `@AfterEach` semantics).

---

### Requirement 5: Screenshot and Page Source Capture on Failure

**User Story:** As a QA engineer, I want screenshots and page source automatically attached to the Allure report on test failure, so that I can diagnose failures without re-running tests.

#### Acceptance Criteria

1. WHEN a test fails, THE `AllureSelenide` listener SHALL capture a screenshot of the browser at the moment of failure.
2. WHEN a test fails, THE `AllureSelenide` listener SHALL capture the full page source HTML at the moment of failure.
3. THE `AllureSelenide` listener SHALL attach the screenshot to the Allure report as an image attachment.
4. THE `AllureSelenide` listener SHALL attach the page source to the Allure report as a text attachment.
5. WHEN a test passes, THE `AllureSelenide` listener SHALL NOT attach a screenshot (to avoid bloating the report).
6. THE `BaseTest` SHALL register the `AllureSelenide` listener exactly once per test execution via `SelenideLogger.addListener()` in `setUp()`.

---

### Requirement 6: CI and Headless Execution

**User Story:** As a DevOps engineer, I want the framework to run headlessly in CI without code changes, so that tests execute reliably inside Docker containers on GitHub Actions.

#### Acceptance Criteria

1. WHEN `ConfigProvider.isHeadless()` returns `true`, THE `Selenide_Configuration` SHALL set `headless = true` before any browser is opened.
2. WHEN running inside a Docker container with `-Dheadless=true`, THE `BaseTest` SHALL configure Selenide for headless execution without requiring source code changes.
3. THE `ConfigProvider` SHALL accept `headless` as a boolean string (`"true"` or `"false"`) from both the properties file and system properties.
4. IF `headless` is set to `true`, THEN THE `Selenide_Configuration` SHALL NOT open a visible browser window during test execution.

---

### Requirement 7: Allure Results Directory

**User Story:** As a DevOps engineer, I want Allure results written to a predictable directory, so that the CI pipeline can reliably locate and publish the report.

#### Acceptance Criteria

1. THE framework SHALL include an `allure.properties` file in `src/test/resources` with `allure.results.directory=target/allure-results`.
2. WHEN tests complete, THE `AllureJunit5` listener SHALL write result JSON files to `target/allure-results`.
3. THE `allure.properties` file SHALL be on the test classpath so that the Allure agent resolves it automatically.
