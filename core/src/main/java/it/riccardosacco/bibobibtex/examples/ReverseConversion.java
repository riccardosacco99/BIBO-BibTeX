package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;

public final class ReverseConversion {
    private ReverseConversion() {}

    public static void main(String[] args) throws IOException {
        Path input = args.length > 0 ? Path.of(args[0]) : Path.of("test-data", "bibo");
        Path output = args.length > 1 ? Path.of(args[1]) : Path.of("test-data", "bibtex-roundtrip");
        Files.createDirectories(output);

        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
        try (Stream<Path> files = Files.list(input).filter(ReverseConversion::isRdfFile)) {
            files.forEach(file -> convertFile(converter, file, output));
        }
    }

    private static void convertFile(BibTeXBibliographicConverter converter, Path file, Path outputDir) {
        try {
            List<BibTeXEntry> entries = converter.convertFromRDFFile(file).stream()
                    .map(document -> converter.convertFromBibo(document))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
            if (!entries.isEmpty()) {
                writeBibTexFile(outputDir, file, entries);
                System.out.printf("Converted %s (%d entries)%n", file.getFileName(), entries.size());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to convert " + file.getFileName(), ex);
        }
    }

    private static boolean isRdfFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".ttl") || name.endsWith(".rdf") || name.endsWith(".jsonld");
    }

    private static void writeBibTexFile(Path outputDir, Path source, List<BibTeXEntry> entries) throws IOException {
        Path fileName = source.getFileName();
        if (fileName == null) {
            return;
        }
        String baseName = fileName.toString();
        int dot = baseName.lastIndexOf('.');
        if (dot > 0) {
            baseName = baseName.substring(0, dot);
        }
        Path output = outputDir.resolve(baseName + ".bib");
        BibTeXDatabase database = new BibTeXDatabase();
        entries.forEach(database::addObject);

        try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            new BibTeXFormatter().format(database, writer);
        }
    }
}
