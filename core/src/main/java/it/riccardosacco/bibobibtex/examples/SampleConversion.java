package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;

/**
 * Piccolo programma dimostrativo che converte un file BibTeX in RDF (BIBO) sfruttando il core
 * converter. Viene generato un file RDF/XML per ogni entry trovata.
 */
public final class SampleConversion {
    private SampleConversion() {
        // utility class
    }

    public static void main(String[] args) throws IOException, ParseException {
        Path input =
                args.length > 0
                        ? Path.of(args[0])
                        : Path.of("examples", "sample-article.bib");
        Path outputDir =
                args.length > 1
                        ? Path.of(args[1])
                        : Path.of("core", "target", "examples");

        convertBibTeXToRdf(input, outputDir);
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
            BibTeXDatabase database = parser.parse(reader);
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

    static void writeRdf(BiboDocument document, Path outputDir) throws IOException {
        String baseName = document.id()
                .map(SampleConversion::safeFileName)
                .filter(name -> !name.isBlank())
                .orElseGet(() -> safeFileName(document.title()));
        Path outputFile = outputDir.resolve(baseName + ".rdf");

        Model model = document.rdfModel();
        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            Rio.write(model, writer, RDFFormat.RDFXML);
        }

        System.out.printf("Generato %s con %d triple%n", outputFile, model.size());
    }

    static String safeFileName(String value) {
        if (value == null) {
            return "document";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("\\p{M}", "");
        String sanitized = ascii.replaceAll("[^a-zA-Z0-9._-]", "_");
        sanitized = sanitized.replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        return sanitized.isEmpty() ? "document" : sanitized;
    }
}
