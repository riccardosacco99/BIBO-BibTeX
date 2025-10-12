package it.riccardosacco.bibobibtex.examples;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;

public final class ReverseConversion {
    private static final Map<IRI, BiboContributorRole> CONTRIBUTOR_PREDICATES =
            Map.of(
                    DCTERMS.CREATOR, BiboContributorRole.AUTHOR,
                    BiboVocabulary.EDITOR, BiboContributorRole.EDITOR,
                    BiboVocabulary.TRANSLATOR, BiboContributorRole.TRANSLATOR,
                    BiboVocabulary.ADVISOR, BiboContributorRole.ADVISOR,
                    BiboVocabulary.REVIEWER, BiboContributorRole.REVIEWER,
                    DCTERMS.CONTRIBUTOR, BiboContributorRole.CONTRIBUTOR);

    private static final Map<IRI, BiboIdentifierType> IDENTIFIER_PREDICATES =
            Arrays.stream(BiboIdentifierType.values())
                    .filter(type -> type != BiboIdentifierType.URL)
                    .map(type -> type.predicate().map(predicate -> Map.entry(predicate, type)))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    private ReverseConversion() {
        // utility class
    }

    public static void main(String[] args) throws IOException {
        Path input = args.length > 0 ? Path.of(args[0]) : Path.of("test-data", "bibo");
        Path output = args.length > 1 ? Path.of(args[1]) : Path.of("test-data", "bibtex-roundtrip");
        convertRdfToBibTex(input, output);
    }

    private static void convertRdfToBibTex(Path inputDir, Path outputDir) throws IOException {
        if (!Files.exists(inputDir)) {
            throw new IOException("Directory RDF non trovata: " + inputDir.toAbsolutePath());
        }
        Files.createDirectories(outputDir);

        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

        try (Stream<Path> files = Files.list(inputDir).filter(path -> path.toString().endsWith(".rdf"))) {
            files.forEach(path -> {
                try {
                    BiboDocument document = parseDocument(path);
                    converter.convertFromBibo(document).ifPresentOrElse(
                            entry -> writeBibTex(outputDir, document, entry),
                            () -> System.out.println("Documento ignorato perché incompleto: " + path.getFileName()));
                } catch (IOException ex) {
                    System.err.println("Errore durante la lettura di " + path.getFileName() + ": " + ex.getMessage());
                }
            });
        }
    }

    private static BiboDocument parseDocument(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Model model = Rio.parse(reader, "", RDFFormat.RDFXML);
            Resource subject = selectSubject(model);
            return buildDocument(model, subject);
        } catch (Exception ex) {
            throw new IOException("Impossibile convertire " + file.getFileName(), ex);
        }
    }

    private static Resource selectSubject(Model model) {
        return Models.subject(model.filter(null, RDF.TYPE, BiboVocabulary.DOCUMENT))
                .or(() -> Models.subject(model.filter(null, RDF.TYPE, null)))
                .orElseThrow(() -> new IllegalStateException("Nessun bibo:Document trovato"));
    }

    private static BiboDocument buildDocument(Model model, Resource subject) {
        String title = literal(model, subject, DCTERMS.TITLE)
                .orElseThrow(() -> new IllegalStateException("Titolo mancante"));
        BiboDocumentType type =
                model.filter(subject, RDF.TYPE, null).objects().stream()
                        .filter(value -> value instanceof IRI)
                        .map(value -> (IRI) value)
                        .map(BiboDocumentType::fromIri)
                        .filter(candidate -> candidate != BiboDocumentType.OTHER)
                        .findFirst()
                        .orElse(BiboDocumentType.OTHER);

        BiboDocument.Builder builder = BiboDocument.builder(type, title);
        literal(model, subject, DCTERMS.IDENTIFIER).ifPresent(builder::id);
        literal(model, subject, BiboVocabulary.SUBTITLE).ifPresent(builder::subtitle);

        readContributors(model, subject).forEach(builder::addContributor);
        parsePublicationDate(model, subject).ifPresent(builder::publicationDate);

        literal(model, subject, DCTERMS.PUBLISHER).ifPresent(builder::publisher);
        literal(model, subject, DCTERMS.SPATIAL).ifPresent(builder::placeOfPublication);
        literal(model, subject, DCTERMS.LANGUAGE).ifPresent(builder::language);
        literal(model, subject, DCTERMS.ABSTRACT).ifPresent(builder::abstractText);
        literal(model, subject, RDFS.COMMENT).ifPresent(builder::notes);

        literal(model, subject, BiboVocabulary.VOLUME).ifPresent(builder::volume);
        literal(model, subject, BiboVocabulary.ISSUE).ifPresent(builder::issue);
        literal(model, subject, BiboVocabulary.PAGES).ifPresent(builder::pages);

        containerTitle(model, subject).ifPresent(builder::containerTitle);
        iriOrLiteral(model, subject, FOAF.PAGE).ifPresent(builder::url);

        builder.identifiers(readIdentifiers(model, subject));

        return builder.build();
    }

    private static List<BiboContributor> readContributors(Model model, Resource subject) {
        List<OrderedContributor> contributors = new ArrayList<>();

        for (Map.Entry<IRI, BiboContributorRole> entry : CONTRIBUTOR_PREDICATES.entrySet()) {
            IRI predicate = entry.getKey();
            BiboContributorRole role = entry.getValue();

            model.filter(subject, predicate, null).objects().stream()
                    .filter(value -> value instanceof Resource)
                    .map(value -> (Resource) value)
                    .forEach(resource -> {
                        Optional<String> given = literal(model, resource, FOAF.GIVEN_NAME);
                        Optional<String> family = literal(model, resource, FOAF.FAMILY_NAME);
                        String fullName = literal(model, resource, FOAF.NAME)
                                .orElseGet(() -> Stream.of(given.orElse(null), family.orElse(null))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.joining(" ")));
                        if (fullName.isBlank()) {
                            return;
                        }

                        BiboPersonName.Builder nameBuilder = BiboPersonName.builder(fullName);
                        given.ifPresent(nameBuilder::givenName);
                        family.ifPresent(nameBuilder::familyName);

                        int order = literal(model, resource, BiboVocabulary.ORDER)
                                .flatMap(ReverseConversion::parseInteger)
                                .orElse(Integer.MAX_VALUE);

                        contributors.add(new OrderedContributor(order, new BiboContributor(nameBuilder.build(), role)));
                    });
        }

        return contributors.stream()
                .sorted(Comparator.comparingInt(candidate -> candidate.order))
                .map(candidate -> candidate.contributor)
                .collect(Collectors.toList());
    }

    private static List<BiboIdentifier> readIdentifiers(Model model, Resource subject) {
        List<BiboIdentifier> identifiers = new ArrayList<>();

        for (Map.Entry<IRI, BiboIdentifierType> entry : IDENTIFIER_PREDICATES.entrySet()) {
            IRI predicate = entry.getKey();
            BiboIdentifierType type = entry.getValue();

            model.filter(subject, predicate, null).objects().forEach(value -> {
                String text = value.stringValue();
                if (!text.isBlank()) {
                    identifiers.add(new BiboIdentifier(type, text));
                }
            });
        }

        return identifiers;
    }

    private static Optional<BiboPublicationDate> parsePublicationDate(Model model, Resource subject) {
        return Models.objectLiteral(model.filter(subject, DCTERMS.ISSUED, null)).flatMap(ReverseConversion::parseDate);
    }

    private static Optional<BiboPublicationDate> parseDate(Literal literal) {
        String label = literal.getLabel().trim();
        IRI datatype = literal.getDatatype();

        try {
            if (datatype != null) {
                if (XSD.DATE.equals(datatype) || XSD.DATETIME.equals(datatype)) {
                    LocalDate date = LocalDate.parse(label.substring(0, 10));
                    return Optional.of(BiboPublicationDate.ofFullDate(date.getYear(), date.getMonthValue(),
                            date.getDayOfMonth()));
                }
                if (XSD.GYEARMONTH.equals(datatype)) {
                    String[] parts = label.split("-");
                    return Optional.of(BiboPublicationDate.ofYearMonth(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1])));
                }
                if (XSD.GYEAR.equals(datatype)) {
                    return Optional.of(BiboPublicationDate.ofYear(Integer.parseInt(label)));
                }
            }

            if (label.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate date = LocalDate.parse(label);
                return Optional.of(BiboPublicationDate.ofFullDate(date.getYear(), date.getMonthValue(),
                        date.getDayOfMonth()));
            }
            if (label.matches("\\d{4}-\\d{2}")) {
                String[] parts = label.split("-");
                return Optional.of(BiboPublicationDate.ofYearMonth(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
            }
            if (label.matches("\\d{4}")) {
                return Optional.of(BiboPublicationDate.ofYear(Integer.parseInt(label)));
            }
        } catch (Exception ignored) {
            // mantieni l'opzione vuota se il parsing fallisce
        }
        return Optional.empty();
    }

    private static Optional<String> containerTitle(Model model, Resource subject) {
        return model.filter(subject, DCTERMS.IS_PART_OF, null).objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .map(container -> literal(model, container, DCTERMS.TITLE))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<String> literal(Model model, Resource subject, IRI predicate) {
        return Models.objectLiteral(model.filter(subject, predicate, null))
                .map(Literal::getLabel)
                .map(String::trim)
                .filter(text -> !text.isEmpty());
    }

    private static Optional<String> iriOrLiteral(Model model, Resource subject, IRI predicate) {
        return model.filter(subject, predicate, null).objects().stream()
                .map(value -> value instanceof Literal literal ? literal.getLabel() : value.stringValue())
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .findFirst();
    }

    private static Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static void writeBibTex(Path outputDir, BiboDocument document, BibTeXEntry entry) {
        String baseName =
                entry.getKey() != null
                        ? entry.getKey().getValue()
                        : document.id().orElseGet(() -> document.title().replaceAll("\\s+", "_"));

        Path outputFile = outputDir.resolve(baseName + ".bib");

        BibTeXDatabase database = new BibTeXDatabase();
        database.addObject(entry);

        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            new BibTeXFormatter().format(database, writer);
            System.out.println("Generato " + outputFile);
        } catch (IOException ex) {
            System.err.println("Errore nello scrivere " + outputFile.getFileName() + ": " + ex.getMessage());
        }
    }

    private static final class OrderedContributor {
        private final int order;
        private final BiboContributor contributor;

        private OrderedContributor(int order, BiboContributor contributor) {
            this.order = order;
            this.contributor = contributor;
        }
    }
}