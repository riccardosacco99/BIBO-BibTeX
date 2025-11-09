package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * Example showing BibTeX → BIBO → RDF conversion without repository.
 *
 * Use this to test conversion pipeline:
 * - Reads BibTeX file
 * - Converts to BIBO
 * - Exports to RDF/Turtle
 *
 * For repository testing, use vocbench-plugin examples.
 */
public class RepositoryExample {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: RepositoryExample <bibtex-file>");
            System.out.println("\nExample:");
            System.out.println("  mvn -q exec:java -pl core \\");
            System.out.println("    -Dexec.mainClass=it.riccardosacco.bibobibtex.examples.RepositoryExample \\");
            System.out.println("    -Dexec.args=\"test-data/bibtex/PapersDB.bib\"");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);

        System.out.println("=== BibTeX → BIBO Conversion Example ===\n");
        System.out.println("Input file: " + inputFile);

        // Parse BibTeX
        BibTeXDatabase database;
        try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            BibTeXParser parser = new BibTeXParser();
            database = parser.parse(reader);
        }

        Collection<BibTeXEntry> entries = database.getEntries().values();
        System.out.println("Found " + entries.size() + " BibTeX entries\n");

        // Convert to BIBO
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

        int converted = 0;
        int skipped = 0;
        for (BibTeXEntry entry : entries) {
            String key = entry.getKey().getValue();
            System.out.println("Converting entry: " + key);

            try {
                Optional<BiboDocument> doc = converter.convertToBibo(entry);
                if (doc.isPresent()) {
                    BiboDocument biboDoc = doc.get();
                    System.out.println("  ✓ Title: " + biboDoc.title());
                    System.out.println("    Type: " + biboDoc.type());
                    System.out.println("    RDF statements: " + biboDoc.rdfModel().size());
                    System.out.println("    Contributors: " + biboDoc.contributors().size());
                    System.out.println();
                    converted++;
                } else {
                    System.out.println("  ✗ Failed to convert");
                    skipped++;
                }
            } catch (Exception e) {
                System.out.println("  ✗ Validation error: " + e.getMessage());
                skipped++;
            }
        }

        System.out.println("=== Summary ===");
        System.out.println("Total entries: " + entries.size());
        System.out.println("Converted: " + converted);
        System.out.println("Skipped: " + skipped);
        System.out.println("\nNote: Use BatchConversion for file export");
        System.out.println("Note: Use vocbench-plugin examples for repository operations");
    }
}
