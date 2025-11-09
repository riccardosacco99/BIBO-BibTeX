package it.riccardosacco.bibobibtex.examples;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple utility to convert RDF files between formats.
 * Reads an RDF file and writes it in a different format (e.g., RDF/XML to Turtle).
 */
public final class RDFConverter {
    private RDFConverter() {
        // utility class
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: RDFConverter <input-file> <output-file>");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);
        Path outputPath = Path.of(args[1]);

        convertRdfFile(inputPath, outputPath);
    }

    private static void convertRdfFile(Path inputPath, Path outputPath) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new IOException("Input file not found: " + inputPath.toAbsolutePath());
        }

        // Read RDF file (auto-detect format from file extension)
        Model model;
        try (Reader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            RDFFormat inputFormat = Rio.getParserFormatForFileName(inputPath.toString())
                    .orElse(RDFFormat.RDFXML);
            model = Rio.parse(reader, "", inputFormat);
        } catch (Exception e) {
            throw new IOException("Failed to read input file: " + inputPath.getFileName(), e);
        }

        // Write RDF file (auto-detect format from file extension)
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            RDFFormat outputFormat = Rio.getWriterFormatForFileName(outputPath.toString())
                    .orElse(RDFFormat.TURTLE);
            Rio.write(model, writer, outputFormat);
        } catch (Exception e) {
            throw new IOException("Failed to write output file: " + outputPath.getFileName(), e);
        }

        System.out.println("Successfully converted " + inputPath.getFileName() + " to " + outputPath.getFileName());
    }
}
