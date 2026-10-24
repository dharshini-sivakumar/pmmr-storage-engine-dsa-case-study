#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."
mkdir -p out
echo "Compiling PMMR Storage Engine..."
javac -d out src/pmmr/*.java
echo "Compilation successful!"
