#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
bash scripts/build.sh
echo "Launching 100k transaction benchmark demo..."
java -cp out pmmr.Main demo
