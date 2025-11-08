package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ObjectResolutionException;
import org.jbibtex.ParseException;

/**
 * Piccolo programma dimostrativo che converte un file BibTeX in RDF (BIBO) sfruttando il core
 * converter. Viene generato un file Turtle per ogni entry trovata.
 */
public final class SampleConversion {
    private SampleConversion() {
        // utility class
    }

    public static void main(String[] args) throws IOException {
        Path input =
                args.length > 0
                        ? Path.of(args[0])
                        : Path.of("examples", "sample-article.bib");
        Path outputDir =
                args.length > 1
                        ? Path.of(args[1])
                        : Path.of("core", "target", "examples");

        try {
            convertBibTeXToRdf(input, outputDir);
        } catch (ParseException e) {
            System.err.println("ERROR: Failed to parse BibTeX file: " + e.getMessage());
            throw new IOException("BibTeX parsing failed", e);
        }
    }

    private static void convertBibTeXToRdf(Path input, Path outputDir) throws IOException, ParseException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(outputDir, "outputDir");

        if (!Files.exists(input)) {
            throw new IOException("Input BibTeX file not found: " + input.toAbsolutePath());
        }

        BibTeXParser parser = new BibTeXParser();
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            BibTeXDatabase database;
            try {
                database = parser.parse(reader);
            } catch (ObjectResolutionException e) {
                System.err.println("WARNING: Cross-reference resolution failed: " + e.getMessage());
                System.err.println("The BibTeX file contains references to entries that don't exist.");
                System.err.println("Processing will continue with entries that were successfully parsed.");
                // Get the partially parsed database from the parser and continue
                database = parser.getDatabase();
            }
            Collection<BibTeXEntry> entries = database.getEntries().values();

            if (entries.isEmpty()) {
                System.out.println("Nessuna entry trovata in " + input);
                return;
            }

            Files.createDirectories(outputDir);

            for (BibTeXEntry entry : entries) {
                Optional<BiboDocument> document = converter.convertToBibo(entry);
                if (document.isEmpty()) {
                    System.out.println("Entry ignorata perché non convertibile: " + entry);
                    continue;
                }

                writeRdf(document.get(), outputDir);
            }
        }
    }

    private static void writeRdf(BiboDocument document, Path outputDir) throws IOException {
        String baseName = document.id().orElseGet(() -> document.title().replaceAll("\\s+", "_"));
        // Sanitize filename by replacing filesystem-problematic characters
        baseName = baseName.replaceAll("[/\\\\:*?\"<>|]", "_");
        Path outputFile = outputDir.resolve(baseName + ".ttl");

        Model model = document.rdfModel();
        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            Rio.write(model, writer, RDFFormat.TURTLE);
        }

        System.out.printf("Generato %s con %d triple%n", outputFile, model.size());
    }
}
