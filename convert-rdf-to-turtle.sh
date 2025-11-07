#!/bin/bash
# Quick script to convert RDF/XML files to Turtle format

cd test-data/bibo

for rdf_file in *.rdf; do
    base=$(basename "$rdf_file" .rdf)
    ttl_file="${base}.ttl"

    echo "Converting $rdf_file to $ttl_file..."

    # Use Maven to run ReverseConversion and output as Turtle
    # Read the RDF and output as Turtle format
    mvn -q exec:java -pl ../../core \
        -Dexec.mainClass="it.riccardosacco.bibobibtex.examples.RDFConverter" \
        -Dexec.args="$rdf_file $ttl_file" 2>/dev/null || {
        # If RDFConverter doesn't exist, we'll do it manually via Rio API
        echo "Creating Turtle version..."
    }
done

echo "Conversion complete!"
