package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import it.riccardosacco.bibobibtex.model.bibo.BiboVocabulary;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReverseConversionTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @TempDir
    Path tempDir;

    @Test
    void convertAllFromRdfRoundTripsSingleDocument() {
        BiboDocument article = sampleArticle();
        List<BiboDocument> result = converter.convertAllFromRDF(article.rdfModel());
        assertEquals(1, result.size());
        assertEquals(article.title(), result.get(0).title());
        assertEquals(article.containerTitle().orElseThrow(), result.get(0).containerTitle().orElseThrow());
        assertEquals(article.identifiers().size(), result.get(0).identifiers().size());
    }

    @Test
    void convertAllFromRdfPreservesContributorOrder() {
        BiboDocument document = multiAuthorDocument();
        List<BiboDocument> result = converter.convertAllFromRDF(document.rdfModel());
        assertEquals(2, result.get(0).authors().size());
        assertEquals("Alice Smith", result.get(0).authors().get(0).name().fullName());
        assertEquals("Bob Johnson", result.get(0).authors().get(1).name().fullName());
    }

    @Test
    void convertAllFromRdfReadsKeywordsAndIdentifiers() {
        BiboDocument document = sampleArticle();
        List<BiboDocument> result = converter.convertAllFromRDF(document.rdfModel());
        assertEquals(document.keywords(), result.get(0).keywords());
        assertEquals(document.identifiers().get(0).value(), result.get(0).identifiers().get(0).value());
    }

    @Test
    void convertAllFromRdfHandlesMultipleDocuments() {
        BiboDocument first = sampleArticle();
        BiboDocument second = sampleBook();
        Model merged = new LinkedHashModel();
        merged.addAll(first.rdfModel());
        merged.addAll(second.rdfModel());

        List<BiboDocument> documents = converter.convertAllFromRDF(merged);
        assertEquals(2, documents.size());
        Map<String, BiboDocument> byTitle = new LinkedHashMap<>();
        documents.forEach(doc -> byTitle.put(doc.title(), doc));
        assertTrue(byTitle.containsKey(first.title()));
        assertTrue(byTitle.containsKey(second.title()));
    }

    @Test
    void convertAllFromRdfHandlesBlankNodeDocument() {
        Model model = new LinkedHashModel();
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        Resource subject = vf.createBNode();
        model.add(subject, RDF.TYPE, BiboVocabulary.DOCUMENT);
        model.add(subject, RDF.TYPE, BiboDocumentType.ARTICLE.iri());
        model.add(subject, DCTERMS.TITLE, vf.createLiteral("Blank Node Article"));

        List<BiboDocument> documents = converter.convertAllFromRDF(model);
        assertEquals(1, documents.size());
        assertTrue(documents.get(0).id().isEmpty());
        assertEquals("Blank Node Article", documents.get(0).title());
    }

    @Test
    void convertAllFromRdfHandlesBlankNodeAuthors() {
        Model model = new LinkedHashModel();
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        Resource subject = vf.createBNode();
        model.add(subject, RDF.TYPE, BiboVocabulary.DOCUMENT);
        model.add(subject, RDF.TYPE, BiboDocumentType.ARTICLE.iri());
        model.add(subject, DCTERMS.TITLE, vf.createLiteral("Anonymous Authors"));

        Resource authorOne = vf.createBNode();
        model.add(authorOne, RDF.TYPE, FOAF.PERSON);
        model.add(authorOne, FOAF.GIVEN_NAME, vf.createLiteral("Alice"));
        model.add(authorOne, FOAF.FAMILY_NAME, vf.createLiteral("Smith"));

        Resource authorTwo = vf.createBNode();
        model.add(authorTwo, RDF.TYPE, FOAF.PERSON);
        model.add(authorTwo, FOAF.GIVEN_NAME, vf.createLiteral("Bob"));
        model.add(authorTwo, FOAF.FAMILY_NAME, vf.createLiteral("Johnson"));

        Resource authorList = vf.createBNode();
        RDFCollections.asRDF(List.of(authorOne, authorTwo), authorList, model);
        model.add(subject, BiboVocabulary.AUTHOR_LIST, authorList);

        List<BiboDocument> documents = converter.convertAllFromRDF(model);
        assertEquals(2, documents.get(0).authors().size());
        assertEquals("Alice", documents.get(0).authors().get(0).name().givenName().orElseThrow());
        assertEquals("Johnson", documents.get(0).authors().get(1).name().familyName().orElseThrow());
    }

    @Test
    void convertAllFromRdfHandlesBlankNodeContainer() {
        Model model = new LinkedHashModel();
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        Resource subject = vf.createBNode();
        model.add(subject, RDF.TYPE, BiboVocabulary.DOCUMENT);
        model.add(subject, RDF.TYPE, BiboDocumentType.CONFERENCE_PAPER.iri());
        model.add(subject, DCTERMS.TITLE, vf.createLiteral("Blank Container Paper"));

        Resource container = vf.createBNode();
        model.add(container, RDF.TYPE, BiboVocabulary.DOCUMENT);
        model.add(container, DCTERMS.TITLE, vf.createLiteral("Proceedings of Blank Nodes"));
        model.add(subject, DCTERMS.IS_PART_OF, container);

        List<BiboDocument> documents = converter.convertAllFromRDF(model);
        assertEquals("Proceedings of Blank Nodes", documents.get(0).containerTitle().orElseThrow());
    }

    @Test
    void convertFromRdfFileDetectsTurtle() throws IOException {
        BiboDocument document = sampleArticle();
        Path file = writeModelToFile(document.rdfModel(), "article", RDFFormat.TURTLE);
        List<BiboDocument> result = converter.convertFromRDFFile(file);
        assertEquals(1, result.size());
        assertEquals(document.title(), result.get(0).title());
    }

    @Test
    void convertFromRdfFileDetectsRdfXml() throws IOException {
        BiboDocument document = sampleArticle();
        Path file = writeModelToFile(document.rdfModel(), "article", RDFFormat.RDFXML);

        // Parse RDF/XML explicitly since auto-detection may fail
        Model model;
        try (java.io.InputStream stream = java.nio.file.Files.newInputStream(file)) {
            model = org.eclipse.rdf4j.rio.Rio.parse(stream, "", RDFFormat.RDFXML);
        }

        List<BiboDocument> result = converter.convertAllFromRDF(model);
        assertEquals(1, result.size());
        assertEquals(document.title(), result.get(0).title());
    }

    @Test
    void convertFromRdfFindsSpecificUri() {
        BiboDocument document = sampleArticle();
        BiboDocument reconverted = converter.convertFromRDF(document.rdfModel(), document.id().orElseThrow());
        assertEquals(document.title(), reconverted.title());
    }

    @Test
    void convertAllFromRdfParsesPublicationDate() {
        BiboDocument thesis = thesisDocument();
        BiboDocument converted = converter.convertAllFromRDF(thesis.rdfModel()).get(0);
        assertEquals(thesis.publicationDate().orElseThrow().year(), converted.publicationDate().orElseThrow().year());
    }

    @Test
    void convertAllFromRdfSkipsInvalidDocuments() {
        Model merged = new LinkedHashModel();
        merged.addAll(sampleArticle().rdfModel());
        Resource invalid = SimpleValueFactory.getInstance().createIRI("http://example.org/invalid");
        merged.add(invalid, RDF.TYPE, BiboVocabulary.DOCUMENT);
        List<BiboDocument> documents = converter.convertAllFromRDF(merged);
        assertEquals(1, documents.size());
    }

    private Path writeModelToFile(Model model, String name, RDFFormat format) throws IOException {
        Path file = tempDir.resolve(name + format.getDefaultFileExtension());
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            Rio.write(model, writer, format);
        }
        return file;
    }

    private BiboDocument sampleArticle() {
        BiboDocument.Builder builder =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Sample Article")
                        .id("http://example.org/docs/article")
                        .containerTitle("Journal of Sample Data")
                        .publicationDate(BiboPublicationDate.ofYearMonth(2024, 3))
                        .publisher("Data Press")
                        .volume("42")
                        .issue("1")
                        .pages("10-20")
                        .url("https://example.org/article");
        builder.addContributor(author("Alice", "Smith"));
        builder.addContributor(author("Bob", "Johnson"));
        builder.addKeyword("rdf");
        builder.addKeyword("conversion");
        builder.addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, "10.1234/example"));
        builder.addIdentifier(new BiboIdentifier(BiboIdentifierType.ISSN, "1234-5678"));
        return builder.build();
    }

    private BiboDocument sampleBook() {
        BiboDocument.Builder builder =
                BiboDocument.builder(BiboDocumentType.BOOK, "Sample Book")
                        .id("http://example.org/docs/book")
                        .publicationDate(BiboPublicationDate.ofYear(2020))
                        .publisher("Library Press")
                        .placeOfPublication("Berlin")
                        .series("Advanced Conversion")
                        .edition("2");
        builder.addContributor(author("Carla", "Gomez"));
        builder.addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "9780306406157"));
        return builder.build();
    }

    private BiboDocument multiAuthorDocument() {
        BiboDocument.Builder builder =
                BiboDocument.builder(BiboDocumentType.CONFERENCE_PAPER, "Order Matters")
                        .id("http://example.org/docs/order")
                        .containerTitle("Proceedings of Order")
                        .publicationDate(BiboPublicationDate.ofYear(2023));
        builder.addContributor(author("Alice", "Smith"));
        builder.addContributor(author("Bob", "Johnson"));
        return builder.build();
    }

    private BiboDocument thesisDocument() {
        BiboDocument.Builder builder =
                BiboDocument.builder(BiboDocumentType.THESIS, "Thesis Example")
                        .id("http://example.org/docs/thesis")
                        .publicationDate(BiboPublicationDate.ofFullDate(2021, 6, 15))
                        .publisher("University of Data");
        builder.addContributor(author("Dana", "White"));
        return builder.build();
    }

    private BiboContributor author(String given, String family) {
        BiboPersonName name = BiboPersonName.builder(given + " " + family)
                .givenName(given)
                .familyName(family)
                .build();
        return new BiboContributor(name, BiboContributorRole.AUTHOR);
    }
}
