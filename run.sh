#!/bin/sh
# Usage: ./run.sh [test|demo|cli]
set -e
mkdir -p out
javac -d out src/pmmr/*.java
case "$1" in
  test) java -cp out pmmr.Tests ;;
  demo) java -cp out pmmr.Main demo ;;
  *)    java -cp out pmmr.Main ;;
esac
