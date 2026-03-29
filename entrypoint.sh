#!/usr/bin/env bash
set -euo pipefail

# ── Defaults (overridable via -e flags or docker run env vars) ────────────────
BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-true}"
BASE_URL="${BASE_URL:-https://www.saucedemo.com}"
RESULTS_DIR="/app/target/allure-results"
REPORT_DIR="/app/target/site/allure-maven-plugin"

echo "=============================================="
echo "  SauceDemo UI Automation"
echo "=============================================="
echo "  Browser  : ${BROWSER}"
echo "  Headless : ${HEADLESS}"
echo "  Base URL : ${BASE_URL}"
echo "  Date     : $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo "=============================================="

# ── Run tests ─────────────────────────────────────────────────────────────────
EXIT_CODE=0
mvn test \
  -Dbrowser="${BROWSER}" \
  -Dheadless="${HEADLESS}" \
  -Dbase.url="${BASE_URL}" \
  --no-transfer-progress \
  || EXIT_CODE=$?

# ── Generate Allure HTML report ───────────────────────────────────────────────
echo ""
echo "Generating Allure report..."
mvn allure:report --no-transfer-progress || true

# ── Print summary from Surefire XML ──────────────────────────────────────────
echo ""
echo "=============================================="
echo "  Test Results Summary"
echo "=============================================="

TOTAL=0; PASSED=0; FAILED=0; ERRORS=0; SKIPPED=0
SUREFIRE_DIR="/app/target/surefire-reports"

if [ -d "$SUREFIRE_DIR" ]; then
  for f in "$SUREFIRE_DIR"/TEST-*.xml; do
    [ -f "$f" ] || continue
    t=$(grep -oP 'tests="\K[0-9]+' "$f" 2>/dev/null | head -1 || echo 0)
    fa=$(grep -oP 'failures="\K[0-9]+' "$f" 2>/dev/null | head -1 || echo 0)
    e=$(grep -oP 'errors="\K[0-9]+' "$f" 2>/dev/null | head -1 || echo 0)
    s=$(grep -oP 'skipped="\K[0-9]+' "$f" 2>/dev/null | head -1 || echo 0)
    TOTAL=$((TOTAL + t))
    FAILED=$((FAILED + fa))
    ERRORS=$((ERRORS + e))
    SKIPPED=$((SKIPPED + s))
  done
  PASSED=$((TOTAL - FAILED - ERRORS - SKIPPED))
fi

echo "  Total   : ${TOTAL}"
echo "  Passed  : ${PASSED}"
echo "  Failed  : ${FAILED}"
echo "  Errors  : ${ERRORS}"
echo "  Skipped : ${SKIPPED}"
echo "=============================================="

if [ -d "$RESULTS_DIR" ]; then
  echo "  Allure results : ${RESULTS_DIR}"
fi
if [ -d "$REPORT_DIR" ]; then
  echo "  Allure report  : ${REPORT_DIR}"
fi
echo "=============================================="

# Exit with Maven's exit code so CI picks up failures
exit $EXIT_CODE
