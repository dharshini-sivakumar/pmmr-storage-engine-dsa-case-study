#!/usr/bin/env bash
cd "$(dirname "$0")/.."
echo "Cleaning build and temporary files..."
rm -rf out target build *.pmmr *.bin
find . -name "*.class" -type f -delete
echo "Clean complete."
