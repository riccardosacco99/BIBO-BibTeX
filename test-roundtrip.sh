#!/bin/bash
# Script to test complete BibTeX ↔ RDF roundtrip conversion
# Flow: test-data/bibo/*.ttl → test-data/bibtex-roundtrip/*.bib → test-data/bibo-roundtrip/*.ttl

set -e  # Exit on error

echo "=========================================="
echo "  Complete BibTeX ↔ RDF Roundtrip Test"
echo "=========================================="
echo ""
echo "Flow:"
echo "  1. test-data/bibo/            (RDF from original BibTeX)"
echo "  2. test-data/bibtex-roundtrip/ (BibTeX roundtrip)"
echo "  3. test-data/bibo-roundtrip/   (RDF roundtrip)"
echo ""

# Directories
BIBO_DIR="test-data/bibo"
BIBTEX_ROUNDTRIP_DIR="test-data/bibtex-roundtrip"
BIBO_ROUNDTRIP_DIR="test-data/bibo-roundtrip"

# Create directories
mkdir -p "$BIBTEX_ROUNDTRIP_DIR"
mkdir -p "$BIBO_ROUNDTRIP_DIR"

# Clean roundtrip directories
echo "Cleaning roundtrip directories..."
rm -rf "$BIBTEX_ROUNDTRIP_DIR"/* "$BIBO_ROUNDTRIP_DIR"/*

echo ""
echo "=========================================="
echo "Step 1: RDF → BibTeX"
echo "=========================================="
echo "Input:  $BIBO_DIR/*.ttl"
echo "Output: $BIBTEX_ROUNDTRIP_DIR/*.bib"
echo ""

# Run ReverseConversion: RDF → BibTeX
mvn -q exec:java -pl core \
    -Dexec.mainClass="it.riccardosacco.bibobibtex.examples.ReverseConversion" \
    -Dexec.args="$BIBO_DIR $BIBTEX_ROUNDTRIP_DIR" 2>&1 | grep -v "WARNING\|SLF4J" || true

echo ""
echo "=========================================="
echo "Step 2: BibTeX → RDF (Roundtrip)"
echo "=========================================="
echo "Input:  $BIBTEX_ROUNDTRIP_DIR/*.bib"
echo "Output: $BIBO_ROUNDTRIP_DIR/*.ttl"
echo ""

# Convert BibTeX back to RDF using BatchConversion (much faster!)
echo "Converting all BibTeX files in batch..."
mvn -q exec:java -pl core \
    -Dexec.mainClass="it.riccardosacco.bibobibtex.examples.BatchConversion" \
    -Dexec.args="$BIBTEX_ROUNDTRIP_DIR $BIBO_ROUNDTRIP_DIR" 2>&1 | grep -v "WARNING\|SLF4J" || true

bib_count=$(ls "$BIBTEX_ROUNDTRIP_DIR"/*.bib 2>/dev/null | wc -l | tr -d ' ')

echo ""
echo "=========================================="
echo "  Roundtrip Complete!"
echo "=========================================="
echo ""
echo "Files processed: $bib_count BibTeX files"
echo ""
echo "Directory structure:"
echo "  test-data/bibo/             - $(ls $BIBO_DIR/*.ttl 2>/dev/null | wc -l | tr -d ' ') RDF files (original)"
echo "  test-data/bibtex-roundtrip/ - $(ls $BIBTEX_ROUNDTRIP_DIR/*.bib 2>/dev/null | wc -l | tr -d ' ') BibTeX files (roundtrip)"
echo "  test-data/bibo-roundtrip/   - $(ls $BIBO_ROUNDTRIP_DIR/*.ttl 2>/dev/null | wc -l | tr -d ' ') RDF files (roundtrip)"
echo ""
echo "To compare files, use:"
echo "  diff test-data/bibo/<file>.ttl test-data/bibo-roundtrip/<file>.ttl"
echo ""
