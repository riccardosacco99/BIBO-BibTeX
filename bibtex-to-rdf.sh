#!/bin/bash
# Script to convert BibTeX files to RDF (BIBO ontology)
# Converts all .bib files from test-data/bibtex/ to test-data/bibo/

set -e  # Exit on error

echo "======================================"
echo "  BibTeX → RDF Conversion"
echo "======================================"

# Directories
INPUT_DIR="test-data/bibtex"
OUTPUT_DIR="test-data/bibo"

# Check if input directory exists
if [ ! -d "$INPUT_DIR" ]; then
    echo "ERROR: Input directory $INPUT_DIR not found"
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

echo ""
echo "Converting BibTeX files to RDF..."
echo "Input:  $INPUT_DIR/*.bib"
echo "Output: $OUTPUT_DIR/*.ttl"
echo ""

# Count files
bib_count=0
success_count=0
error_count=0

# Run BatchConversion: BibTeX → RDF for all files at once
# This is much faster than processing files individually
if mvn -q exec:java -pl core \
    -Dexec.mainClass="it.riccardosacco.bibobibtex.examples.BatchConversion" \
    -Dexec.args="$INPUT_DIR $OUTPUT_DIR" 2>&1 | grep -v "WARNING\|SLF4J"; then
    success_count=1
else
    error_count=1
fi

# Count generated files
bib_count=$(ls -1 "$INPUT_DIR"/*.bib 2>/dev/null | wc -l | tr -d ' ')

echo ""
echo "======================================"
echo "  Conversion Complete!"
echo "======================================"
echo "Total files processed: $bib_count"
echo "Successful: $success_count"
if [ $error_count -gt 0 ]; then
    echo "With warnings: $error_count"
fi
echo ""
echo "RDF files generated in: $OUTPUT_DIR/"
echo ""
