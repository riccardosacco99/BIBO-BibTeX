package it.riccardosacco.bibobibtex.vocbench.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.vocbench.RDF4JRepositoryGateway;
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
 * Example showing BibTeX import into RDF4J repository.
 *
 * Demonstrates:
 * - Creating Native Store repository
 * - Converting BibTeX → BIBO
 * - Storing RDF in repository
 * - Querying repository stats
 *
 * Usage:
 *   mvn -q exec:java -pl vocbench-plugin \
 *     -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryImportExample \
 *     -Dexec.args="test-data/bibtex/PapersDB.bib repository-data"
 */
public class RepositoryImportExample {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: RepositoryImportExample <bibtex-file> <repository-dir>");
            System.out.println("\nExample:");
            System.out.println("  mvn -q exec:java -pl vocbench-plugin \\");
            System.out.println("    -Dexec.mainClass=it.riccardosacco.bibobibtex.vocbench.examples.RepositoryImportExample \\");
            System.out.println("    -Dexec.args=\"test-data/bibtex/PapersDB.bib repository-data\"");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        String repoDir = args[1];

        System.out.println("=== BibTeX → RDF4J Repository Import ===\n");
        System.out.println("Input file: " + inputFile);
        System.out.println("Repository: " + repoDir);
        System.out.println();

        // Create repository gateway
        RDF4JRepositoryGateway gateway = new RDF4JRepositoryGateway(repoDir);

        try {
            // Parse BibTeX
            BibTeXDatabase database;
            try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
                BibTeXParser parser = new BibTeXParser();
                database = parser.parse(reader);
            }

            Collection<BibTeXEntry> entries = database.getEntries().values();
            System.out.println("Found " + entries.size() + " BibTeX entries\n");

            // Convert and store
            BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

            int stored = 0;
            int skipped = 0;
            for (BibTeXEntry entry : entries) {
                String key = entry.getKey().getValue();
                System.out.println("Processing: " + key);

                try {
                    Optional<BiboDocument> doc = converter.convertToBibo(entry);
                    if (doc.isPresent()) {
                        BiboDocument biboDoc = doc.get();

                        // Store in repository
                        gateway.store(biboDoc.rdfModel());

                        System.out.println("  ✓ Stored: " + biboDoc.title());
                        System.out.println("    Statements: " + biboDoc.rdfModel().size());
                        stored++;
                    } else {
                        System.out.println("  ✗ Failed to convert");
                        skipped++;
                    }
                } catch (Exception e) {
                    System.out.println("  ✗ Validation error: " + e.getMessage());
                    skipped++;
                }
            }

            System.out.println("\n=== Summary ===");
            System.out.println("Total entries: " + entries.size());
            System.out.println("Stored: " + stored);
            System.out.println("Skipped: " + skipped);
            System.out.println("Repository location: " + repoDir);
            System.out.println("\nNote: Full query capabilities in Phase 7.B");
            System.out.println("Note: Repository persists data - re-run to add more documents");

        } finally {
            gateway.shutdown();
            System.out.println("\nRepository closed successfully");
        }
    }
}
