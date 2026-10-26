#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
bash scripts/build.sh
echo "Running full PMMR test suite..."
java -cp out pmmr.Tests
