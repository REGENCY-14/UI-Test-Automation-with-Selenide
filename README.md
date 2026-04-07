# UI Test Automation Framework — SauceDemo

A production-ready UI automation framework for [saucedemo.com](https://www.saucedemo.com) built with Java 17, Selenide, JUnit 5, Maven, Allure Reports, GitHub Actions CI/CD, and Docker.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Language runtime |
| Selenide | 7.2.2 | Browser automation |
| JUnit 5 | 5.10.2 | Test framework |
| Allure | 2.27.0 | Test reporting |
| Maven | 3.9.x | Build & dependency management |
| Docker | latest | Containerized execution |
| GitHub Actions | — | CI/CD pipeline |

---

## Project Structure

```
ui-automation/
├── .github/workflows/ci.yml       # GitHub Actions pipeline
├── Dockerfile                     # Multi-stage Docker image
├── entrypoint.sh                  # Container test runner script
├── pom.xml                        # Maven dependencies & plugins
└── src/
    ├── main/java/com/saucedemo/
    │   ├── config/
    │   │   ├── ConfigProvider.java    # Reads config.properties
    │   │   └── Environment.java       # Environment enum (PROD/STAGING/DEV)
    │   ├── driver/
    │   │   └── DriverFactory.java     # Chrome options (disables password popups)
    │   ├── pages/
    │   │   ├── BasePage.java
    │   │   ├── LoginPage.java
    │   │   ├── ProductsPage.java
    │   │   ├── CartPage.java
    │   │   ├── CheckoutStepOnePage.java
    │   │   ├── CheckoutStepTwoPage.java
    │   │   └── CheckoutCompletePage.java
    │   └── utils/
    │       └── AllureAttachmentUtils.java
    ├── main/resources/config/
    │   └── config.properties          # Default configuration
    └── test/java/com/saucedemo/
        ├── annotations/
        │   └── Retry.java             # @Retry annotation
        ├── base/
        │   └── BaseTest.java          # JUnit 5 base class
        ├── extensions/
        │   └── RetryExtension.java    # Auto-retry on failure
        ├── smoke/
        │   └── SmokeTest.java
        └── tests/
            ├── LoginTest.java
            ├── AddToCartTest.java
            └── CheckoutTest.java
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Google Chrome (latest)
- Docker (optional)

### Run Tests Locally

```bash
# Run all tests (headless)
mvn test

# Run with visible browser
mvn test -Dheadless=false

# Run specific tag
mvn test -Dgroups=smoke
mvn test -Dgroups=regression

# Run against a different environment
mvn test -Denv=staging
```

### Generate Allure Report

```bash
mvn test
mvn allure:serve
```

---

## Configuration

All defaults are in `src/main/resources/config/config.properties`:

```properties
base.url=https://www.saucedemo.com
browser=chrome
headless=true
timeout.ms=10000
```

Override any value via system property or environment variable:

```bash
mvn test -Dbase.url=https://staging.saucedemo.com -Dbrowser=chrome -Dheadless=false
```

| Property | Env Var | Default |
|---|---|---|
| `base.url` | `BASE_URL` | `https://www.saucedemo.com` |
| `browser` | `BROWSER` | `chrome` |
| `headless` | `HEADLESS` | `true` |
| `timeout.ms` | `TIMEOUT_MS` | `10000` |
| `env` | `ENV` | `prod` |

---

## Test Coverage

### LoginTest — 8 tests
| Scenario | Type |
|---|---|
| Standard user login | Positive |
| Performance glitch user login | Positive |
| Wrong password | Negative |
| Unknown username | Negative |
| Empty credentials | Negative |
| Empty password | Negative |
| Locked out user | Edge case |
| SQL injection attempt | Edge case |

### AddToCartTest — 9 tests
| Scenario | Type |
|---|---|
| Add one product | Positive |
| Add multiple products | Positive |
| Product appears in cart | Positive |
| Add all 6 products | Positive |
| Remove product decrements badge | Negative |
| Remove all hides badge | Negative |
| Removed item gone from cart | Negative |
| Cart persists after navigation | Edge case |
| Products page shows 6 items | Edge case |

### CheckoutTest — 10 tests
| Scenario | Type |
|---|---|
| Complete full checkout flow | Positive |
| Order summary item count | Positive |
| Total and tax displayed | Positive |
| Return to products after order | Positive |
| Missing first name error | Negative |
| Missing last name error | Negative |
| Missing postal code error | Negative |
| Cancel step one returns to cart | Negative |
| Cancel step two returns to products | Negative |
| Checkout with multiple items | Edge case |

---

## Docker

### Build Image

```bash
docker build -t saucedemo-tests .
```

### Run Tests in Container

```bash
docker run --rm \
  -v ${PWD}/target:/app/target \
  saucedemo-tests
```

### Override Configuration

```bash
docker run --rm \
  -e BASE_URL=https://www.saucedemo.com \
  -e BROWSER=chrome \
  -e HEADLESS=true \
  -v ${PWD}/target:/app/target \
  saucedemo-tests
```

Results are exported to `./target/allure-results` on the host via the volume mount.

---

## CI/CD — GitHub Actions

The pipeline triggers on:
- Push to `main`, `develop`, `feature/**`
- Pull requests to `main`, `develop`
- Manual trigger (`workflow_dispatch`)

### Pipeline Steps

1. Checkout → Java 17 (Temurin) → Maven cache
2. Install Chrome stable
3. Run tests (`mvn test`)
4. Generate Allure HTML report
5. Upload artifacts (results, report, surefire XML)
6. Deploy report to GitHub Pages (on `main` only)
7. Send Slack notification with test summary

### Required GitHub Secrets

| Secret | Description |
|---|---|
| `SLACK_WEBHOOK_URL` | Slack Incoming Webhook URL for notifications |

### Slack Notification

After each run, a message is posted to your Slack channel with:
- Pass/fail status with colour coding
- Total / Passed / Failed / Errors / Skipped counts
- Branch name and commit SHA
- Links to the Actions run and Allure report

To set up: create an Incoming Webhook at [api.slack.com/apps](https://api.slack.com/apps) and add the URL as the `SLACK_WEBHOOK_URL` secret.

---

## Advanced Features

### Parallel Execution

Test classes run in parallel by default. Controlled via `junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

Increase fork count for faster CI runs:

```bash
mvn test -Dparallel.forks=2
```

### Retry on Failure

All tests automatically retry up to 2 times on failure via `RetryExtension`. Configure the count:

```bash
mvn test -Dtest.retry.count=3
```

Use `@Retry` on individual tests for opt-in retry:

```java
@Test
@Retry
void flakyTest() { ... }
```

### Test Tagging

```bash
mvn test -Dgroups=smoke          # smoke tests only
mvn test -Dgroups=regression     # cart + checkout
mvn test -DexcludedGroups=regression  # skip regression
```

### Environment Switching

```bash
mvn test -Denv=staging   # uses https://staging.saucedemo.com
mvn test -Denv=dev       # uses https://dev.saucedemo.com
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Production-ready code |
| `feature/project-setup` | Phase 1 — Maven setup |
| `feature/selenide-config` | Phase 2 — Selenide & BaseTest |
| `feature/page-objects` | Phase 3 — Page Object Model |
| `feature/test-cases` | Phase 4 — Test implementation |
| `feature/allure-reporting` | Phase 5 — Allure integration |
| `feature/ci-cd-pipeline` | Phase 6 — GitHub Actions |
| `feature/slack-integration` | Phase 7 — Slack notifications |
| `feature/docker-support` | Phase 8 — Dockerization |
| `feature/framework-enhancements` | Phase 9 — Parallel, retry, tagging |
