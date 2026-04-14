#!/usr/bin/env bash
# =============================================================================
# Patchright Java - Stealth Integration Test Automator
#
# This script:
# 1. Checks if the built patched release exists
# 2. Extracts its version
# 3. Runs the external integration tests (Brotector/Cloudflare/CreepJS)
#
# Note: These tests hit external servers and are designed to be run manually,
# not as part of standard CI/CD due to instability with anti-bot endpoints.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RELEASE_DIR="$PROJECT_DIR/patchright-java-release"
TEST_DIR="$PROJECT_DIR/patchright-java-test"

echo "================================================"
echo " Patchright Java Stealth Test Automator"
echo "================================================"

if [ ! -d "$RELEASE_DIR" ]; then
    echo "Error: patchright-java-release directory not found."
    echo "Please run bash scripts/build.sh first so there is a version to test."
    exit 1
fi

# Extract the patched version from the release pom
cd "$RELEASE_DIR"
PATCHED_VERSION=$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')

echo "Found patched version: $PATCHED_VERSION"
echo "Running external stealth integration tests..."
echo ""

cd "$TEST_DIR"
mvn test --no-transfer-progress \
    -Dpatchright.version=$PATCHED_VERSION \
    -Dorg.slf4j.simpleLogger.showDateTime=true \
    -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss

echo ""
echo "================================================"
echo " Stealth Tests Completed!"
echo "================================================"
