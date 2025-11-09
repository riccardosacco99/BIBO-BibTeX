#!/bin/bash
# Quick script to convert Turtle files to RDF/XML format

echo "Converting Turtle files to RDF/XML..."

# Check if any .ttl files exist
if ! ls test-data/bibo/*.ttl 1> /dev/null 2>&1; then
    echo "No .ttl files found in test-data/bibo/"
    exit 1
fi

for ttl_file in test-data/bibo/*.ttl; do
    base=$(basename "$ttl_file" .ttl)
    rdf_file="test-data/bibo/${base}.rdf"

    echo "Converting $(basename "$ttl_file") to $(basename "$rdf_file")..."

    # Use Maven to run RDFConverter from project root
    mvn -q exec:java -pl core \
        -Dexec.mainClass="it.riccardosacco.bibobibtex.examples.RDFConverter" \
        -Dexec.args="$ttl_file $rdf_file" || {
        echo "  Error: RDFConverter failed"
    }
done

echo "Conversion complete!"
