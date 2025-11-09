package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ObjectResolutionException;
import org.jbibtex.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Batch conversion utility to convert all BibTeX files in a directory to RDF.
 * Much faster than calling SampleConversion for each file separately.
 */
public final class BatchConversion {
    private BatchConversion() {
        // utility class
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: BatchConversion <input-dir> <output-dir>");
            System.exit(1);
        }

        Path inputDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);

        convertBibTeXDirectory(inputDir, outputDir);
    }

    private static void convertBibTeXDirectory(Path inputDir, Path outputDir) throws IOException {
        if (!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
            throw new IOException("Input directory not found: " + inputDir.toAbsolutePath());
        }

        Files.createDirectories(outputDir);
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

        int totalFiles = 0;
        int totalEntries = 0;

        try (Stream<Path> files = Files.list(inputDir).filter(path -> path.toString().endsWith(".bib"))) {
            for (Path bibFile : (Iterable<Path>) files::iterator) {
                totalFiles++;
                System.out.println("Processing: " + bibFile.getFileName());

                try {
                    int entries = processBibTeXFile(bibFile, outputDir, converter);
                    totalEntries += entries;
                } catch (Exception e) {
                    System.err.println("Error processing " + bibFile.getFileName() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\nBatch conversion complete:");
        System.out.println("  Files processed: " + totalFiles);
        System.out.println("  Total entries converted: " + totalEntries);
    }

    private static int processBibTeXFile(Path bibFile, Path outputDir, BibTeXBibliographicConverter converter)
            throws IOException, ParseException {
        BibTeXParser parser = new BibTeXParser();

        try (Reader reader = Files.newBufferedReader(bibFile, StandardCharsets.UTF_8)) {
            BibTeXDatabase database;
            try {
                database = parser.parse(reader);
            } catch (ObjectResolutionException e) {
                System.err.println("  Warning: Cross-reference resolution failed, continuing with partial data");
                database = parser.getDatabase();
            }

            Collection<BibTeXEntry> entries = database.getEntries().values();
            if (entries.isEmpty()) {
                System.out.println("  No entries found");
                return 0;
            }

            // Create a single RDF model for all entries in this file
            Model combinedModel = new org.eclipse.rdf4j.model.impl.LinkedHashModel();
            int converted = 0;

            for (BibTeXEntry entry : entries) {
                Optional<BiboDocument> document = converter.convertToBibo(entry);
                if (document.isPresent()) {
                    combinedModel.addAll(document.get().rdfModel());
                    converted++;
                }
            }

            // Write all entries to a single file with the same name as the input file
            if (converted > 0) {
                writeRdf(combinedModel, bibFile, outputDir);
            }

            System.out.println("  Converted: " + converted + " entries");
            return converted;
        }
    }

    private static void writeRdf(Model model, Path inputFile, Path outputDir) throws IOException {
        // Use the same filename as the input file, but with .ttl extension
        String baseName = inputFile.getFileName().toString();
        baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        Path outputFile = outputDir.resolve(baseName + ".ttl");

        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            Rio.write(model, writer, RDFFormat.TURTLE);
        }
    }
}
