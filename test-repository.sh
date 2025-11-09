#!/bin/bash

# Test repository functionality
# Imports BibTeX → Repository, then queries

set -e

BIBTEX_FILE="${1:-test-data/bibtex/PapersDB.bib}"
REPO_DIR="${2:-repository-data}"

echo "=== Testing Repository Functionality ==="
echo

# Clean previous repository
if [ -d "$REPO_DIR" ]; then
    echo "Removing existing repository: $REPO_DIR"
    rm -rf "$REPO_DIR"
    echo
fi

# Import BibTeX into repository
echo "Step 1: Import BibTeX → Repository"
echo "------------------------------------"
mvn -q exec:java -pl vocbench-plugin \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryImportExample \
    -Dexec.args="$BIBTEX_FILE $REPO_DIR"

echo
echo

# Query repository
echo "Step 2: Query Repository"
echo "------------------------"
mvn -q exec:java -pl vocbench-plugin \
    -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryQueryExample \
    -Dexec.args="$REPO_DIR"

echo
echo "=== Repository Test Complete ==="
echo "Repository persisted at: $REPO_DIR"
