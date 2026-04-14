#!/usr/bin/env bash
# =============================================================================
# Patchright Java - Local Test Automator
#
# This script:
# 1. Checks if the patched release directory exists
# 2. Installs playwright headless browsers and OS dependencies
# 3. Runs the extensive unit testing suite from upstream playwright
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RELEASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)/patchright-java-release"

echo "================================================"
echo " Patchright Java Test Automator"
echo "================================================"

if [ ! -d "$RELEASE_DIR" ]; then
    echo "Error: patchright-java-release directory not found."
    echo "Please run bash scripts/build.sh first to patch the sources."
    exit 1
fi

cd "$RELEASE_DIR"

# Ensure we're testing the Chromium browser since it's the primary patch target.
export BROWSER=chromium

echo ""
echo "[Step 1] Installing Playwright Browsers & OS Dependencies..."
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium" -f playwright/pom.xml --no-transfer-progress

echo ""
echo "[Step 2] Running the patched test suite..."
# Run the test suite. We use fail-at-end to show all failures if multiple specs break due to patching.
mvn test --no-transfer-progress --fail-at-end \
    -Dorg.slf4j.simpleLogger.showDateTime=true \
    -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss

echo ""
echo "================================================"
echo " Tests Passed Successfully!"
echo "================================================"
