#!/usr/bin/env bash
# =============================================================================
# Docker Entrypoint Script — SauceDemo UI Automation
# =============================================================================
# This script is the ENTRYPOINT for the saucedemo-tests Docker container.
# It performs the following steps:
#   1. Print a configuration summary banner
#   2. Run the Maven test suite with the configured browser/URL settings
#   3. Generate the Allure HTML report from the raw JSON results
#   4. Parse Surefire XML reports and print a human-readable summary
#   5. Exit with Maven's exit code so CI correctly detects failures
#
# Environment variables (all overridable via docker run -e):
#   BROWSER   — browser to use (default: chrome)
#   HEADLESS  — headless mode flag (default: true)
#   BASE_URL  — application base URL (default: https://www.saucedemo.com)
#
# Usage:
#   docker run --rm -v $(pwd)/target:/app/target saucedemo-tests
#   docker run --rm -e HEADLESS=false -v $(pwd)/target:/app/target saucedemo-tests
# =============================================================================

# Exit immediately on unhandled errors; treat unset variables as errors
set -euo pipefail

# ── Configuration defaults ────────────────────────────────────────────────────
# Use environment variable values if set, otherwise fall back to defaults
BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
BASE_URL="${BASE_URL:-https://www.saucedemo.com}"

# Directories for test output artifacts
RESULTS_DIR="/app/target/allure-results"
REPORT_DIR="/app/target/site/allure-maven-plugin"

# ── Banner ────────────────────────────────────────────────────────────────────
echo "=============================================="
echo "  SauceDemo UI Automation"
echo "=============================================="
echo "  Browser  : ${BROWSER}"
echo "  Headless : ${HEADLESS}"
echo "  Base URL : ${BASE_URL}"
echo "  Date     : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo "=============================================="

# ── Run tests ─────────────────────────────────────────────────────────────────
# Capture Maven's exit code separately so we can still generate the report
# and print the summary even when tests fail (|| EXIT_CODE=$? prevents
# the script from exiting immediately on test failure due to set -e).
EXIT_CODE=0
mvn test \
  -Dbrowser="${BROWSER}" \
  -Dheadless="${HEADLESS}" \
  -Dbase.url="${BASE_URL}" \
  --no-transfer-progress \
  || EXIT_CODE=$?

# ── Generate Allure HTML report ───────────────────────────────────────────────
# Run allure:report regardless of test outcome so the report is always
# available as a CI artifact. '|| true' prevents script failure if the
# report generation itself encounters a non-critical error.
echo ""
echo "Generating Allure report..."
mvn allure:report --no-transfer-progress || true

# ── Parse Surefire XML for summary ───────────────────────────────────────────
# Surefire writes one XML file per test class to target/surefire-reports/.
# We parse the 'tests', 'failures', 'errors', and 'skipped' attributes from
# each file and aggregate them into totals for the summary banner.
echo ""
echo "=============================================="
echo "  Test Results Summary"
echo "=============================================="

# Initialise counters
TOTAL=0; PASSED=0; FAILED=0; ERRORS=0; SKIPPED=0
SUREFIRE_DIR="/app/target/surefire-reports"

if [ -d "$SUREFIRE_DIR" ]; then
  # Iterate over all Surefire XML report files
  for f in "$SUREFIRE_DIR"/TEST-*.xml; do
    [ -f "$f" ] || continue  # Skip if no XML files exist

    # Extract attribute values using grep with Perl regex
    t=$(grep  -oP 'tests="\K[0-9]+'    "$f" 2>/dev/null | head -1 || echo 0)
    fa=$(grep -oP 'failures="\K[0-9]+' "$f" 2>/dev/null | head -1 || echo 0)
    e=$(grep  -oP 'errors="\K[0-9]+'   "$f" 2>/dev/null | head -1 || echo 0)
    s=$(grep  -oP 'skipped="\K[0-9]+'  "$f" 2>/dev/null | head -1 || echo 0)

    # Accumulate totals
    TOTAL=$((TOTAL + t))
    FAILED=$((FAILED + fa))
    ERRORS=$((ERRORS + e))
    SKIPPED=$((SKIPPED + s))
  done

  # Passed = total minus all non-passing categories
  PASSED=$((TOTAL - FAILED - ERRORS - SKIPPED))
fi

# Print the aggregated summary
echo "  Total   : ${TOTAL}"
echo "  Passed  : ${PASSED}"
echo "  Failed  : ${FAILED}"
echo "  Errors  : ${ERRORS}"
echo "  Skipped : ${SKIPPED}"
echo "=============================================="

# Print artifact locations if they exist
if [ -d "$RESULTS_DIR" ]; then
  echo "  Allure results : ${RESULTS_DIR}"
fi
if [ -d "$REPORT_DIR" ]; then
  echo "  Allure report  : ${REPORT_DIR}"
fi
echo "=============================================="

# ── Exit ──────────────────────────────────────────────────────────────────────
# Propagate Maven's exit code so the Docker container exits with a non-zero
# status when tests fail, allowing CI pipelines to detect the failure.
exit $EXIT_CODE
