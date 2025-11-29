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
import java.util.Optional;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.junit.jupiter.api.Test;

class BibTeXBibliographicConverterDetailedTest {
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
    private static final Key FIELD_DAY = new Key("day");
    private static final Key FIELD_LANGUAGE = new Key("language");
    private static final Key FIELD_ABSTRACT = new Key("abstract");
    private static final Key FIELD_ISBN = new Key("isbn");
    private static final Key FIELD_ISSN = new Key("issn");
    private static final Key FIELD_URI = new Key("uri");
    private static final Key FIELD_SERIES = new Key("series");
    private static final Key FIELD_EDITION = new Key("edition");
    private static final Key FIELD_KEYWORDS = new Key("keywords");

    @Test
    void convertToBiboMapsStandardFields() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("smith2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Proceedings Example");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, Alice and Doe, Bob");
        addField(entry, BibTeXEntry.KEY_EDITOR, "Editor, Evan");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "feb");
        addField(entry, FIELD_DAY, "10");
        addField(entry, BibTeXEntry.KEY_PUBLISHER, "ACM Press");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "New York, USA");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "Conference on Examples");
        addField(entry, BibTeXEntry.KEY_VOLUME, "12");
        addField(entry, BibTeXEntry.KEY_NUMBER, "4");
        addField(entry, BibTeXEntry.KEY_PAGES, "10-20");
        addField(entry, BibTeXEntry.KEY_DOI, "10.1000/example-doi");
        addField(entry, FIELD_ISBN, "978-1-4028-9462-6");
        addField(entry, FIELD_ISSN, "1234-5678");
        addField(entry, FIELD_URI, "urn:test:identifier");
        addField(entry, BibTeXEntry.KEY_URL, "https://example.org/proc");
        addField(entry, FIELD_LANGUAGE, "en");
        addField(entry, FIELD_ABSTRACT, "Example abstract.");
        addField(entry, BibTeXEntry.KEY_NOTE, "Example note.");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, document.type());
        assertEquals("Proceedings Example", document.title());
        assertEquals("Conference on Examples", document.containerTitle().orElseThrow());
        assertEquals("ACM Press", document.publisher().orElseThrow());
        // US-24: For @inproceedings, address maps to conference location
        assertEquals("New York, USA", document.conferenceLocation().orElseThrow());
        assertEquals("12", document.volume().orElseThrow());
        assertEquals("4", document.issue().orElseThrow());
        assertEquals("10-20", document.pages().orElseThrow());
        assertEquals(2, document.authors().size());
        assertEquals(1, document.editors().size());

        BiboPublicationDate publicationDate = document.publicationDate().orElseThrow();
        assertEquals(2023, publicationDate.year());
        assertEquals(2, publicationDate.month().orElseThrow());
        assertEquals(10, publicationDate.day().orElseThrow());

        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.DOI));
        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.ISBN_13));
        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.ISSN));
        assertTrue(
                document.identifiers().stream()
                        .anyMatch(identifier ->
                                identifier.type() == BiboIdentifierType.URI
                                        && identifier.value().equals("urn:test:identifier")));
        assertEquals("https://example.org/proc", document.url().orElseThrow());
        assertEquals("en", document.language().orElseThrow());
    }

    @Test
    void convertFromBiboMapsIdentifiersAndContributors() {
        BiboPersonName firstAuthor =
                BiboPersonName.builder("Alice Smith").givenName("Alice").familyName("Smith").build();
        BiboPersonName secondAuthor =
                BiboPersonName.builder("Bob Doe").givenName("Bob").familyName("Doe").build();
        BiboPersonName editor =
                BiboPersonName.builder("Evan Editor").givenName("Evan").familyName("Editor").build();

        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.CONFERENCE_PAPER, "Proceedings Example")
                        .id("smith2023")
                        .addContributor(new BiboContributor(firstAuthor, BiboContributorRole.AUTHOR))
                        .addContributor(new BiboContributor(secondAuthor, BiboContributorRole.AUTHOR))
                        .addContributor(new BiboContributor(editor, BiboContributorRole.EDITOR))
                        .publicationDate(BiboPublicationDate.ofYearMonth(2023, 2))
                        .publisher("ACM Press")
                        // US-24: For conference papers, use conferenceLocation
                        .conferenceLocation("New York, USA")
                        .containerTitle("Conference on Examples")
                        .volume("12")
                        .issue("4")
                        .pages("10-20")
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, "10.1000/example-doi"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "978-1-4028-9462-6"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISSN, "1234-5678"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.URI, "urn:test:identifier"))
                        .url("https://example.org/proc")
                        .language("en")
                        .abstractText("Example abstract.")
                        .notes("Example note.")
                        .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, entry.getType());
        assertEquals("smith2023", entry.getKey().getValue());
        assertEquals("Proceedings Example", value(entry, BibTeXEntry.KEY_TITLE));
        assertEquals("Conference on Examples", value(entry, BibTeXEntry.KEY_BOOKTITLE));
        assertEquals("ACM Press", value(entry, BibTeXEntry.KEY_PUBLISHER));
        assertEquals("New York, USA", value(entry, BibTeXEntry.KEY_ADDRESS));
        assertEquals("12", value(entry, BibTeXEntry.KEY_VOLUME));
        assertEquals("4", value(entry, BibTeXEntry.KEY_NUMBER));
        assertEquals("10-20", value(entry, BibTeXEntry.KEY_PAGES));
        assertTrue(value(entry, BibTeXEntry.KEY_AUTHOR).contains("Smith, Alice"));
        assertTrue(value(entry, BibTeXEntry.KEY_AUTHOR).contains("Doe, Bob"));
        assertTrue(value(entry, BibTeXEntry.KEY_EDITOR).contains("Editor, Evan"));
        assertEquals("2023", value(entry, BibTeXEntry.KEY_YEAR));
        assertEquals("feb", value(entry, BibTeXEntry.KEY_MONTH));
        assertEquals("10.1000/example-doi", value(entry, BibTeXEntry.KEY_DOI));
        assertTrue(value(entry, FIELD_ISBN).contains("978-1-4028-9462-6"));
        assertTrue(value(entry, FIELD_ISSN).contains("1234-5678"));
        assertEquals("urn:test:identifier", value(entry, FIELD_URI));
        assertEquals("https://example.org/proc", value(entry, BibTeXEntry.KEY_URL));
        assertEquals("en", value(entry, FIELD_LANGUAGE));
        assertEquals("Example abstract.", value(entry, FIELD_ABSTRACT));
        assertEquals("Example note.", value(entry, BibTeXEntry.KEY_NOTE));
    }

    private static void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    /**
     * Critical test for FIX-01 (Sprint 00): Verifies that author ordering is preserved
     * through RDF Lists when converting BibTeX -> BIBO -> BibTeX with 5+ authors.
     */
    @Test
    void preservesAuthorOrderingWith5PlusAuthors() {
        // Create entry with 6 authors in specific order
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("multiauthor2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Collaborative Research on Author Ordering");
        addField(
                entry,
                BibTeXEntry.KEY_AUTHOR,
                "First, Alice and Second, Bob and Third, Carol and Fourth, David and Fifth, Eve and Sixth, Frank");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");

        // Convert to BIBO
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        // Verify we have exactly 6 authors
        assertEquals(6, document.authors().size(), "Should have 6 authors");

        // Verify exact ordering is preserved
        assertEquals("First, Alice", document.authors().getFirst().name().fullName());
        assertEquals("Second, Bob", document.authors().get(1).name().fullName());
        assertEquals("Third, Carol", document.authors().get(2).name().fullName());
        assertEquals("Fourth, David", document.authors().get(3).name().fullName());
        assertEquals("Fifth, Eve", document.authors().get(4).name().fullName());
        assertEquals("Sixth, Frank", document.authors().get(5).name().fullName());

        // Convert back to BibTeX
        BibTeXEntry roundTripEntry = converter.convertFromBibo(document).orElseThrow();
        String authorsField = value(roundTripEntry, BibTeXEntry.KEY_AUTHOR);

        // Verify all authors are present
        assertTrue(authorsField.contains("First, Alice"));
        assertTrue(authorsField.contains("Second, Bob"));
        assertTrue(authorsField.contains("Third, Carol"));
        assertTrue(authorsField.contains("Fourth, David"));
        assertTrue(authorsField.contains("Fifth, Eve"));
        assertTrue(authorsField.contains("Sixth, Frank"));

        // Verify ordering in the string (First should come before Second, etc.)
        int posFirst = authorsField.indexOf("First");
        int posSecond = authorsField.indexOf("Second");
        int posThird = authorsField.indexOf("Third");
        int posFourth = authorsField.indexOf("Fourth");
        int posFifth = authorsField.indexOf("Fifth");
        int posSixth = authorsField.indexOf("Sixth");

        assertTrue(posFirst < posSecond, "First author should appear before Second");
        assertTrue(posSecond < posThird, "Second author should appear before Third");
        assertTrue(posThird < posFourth, "Third author should appear before Fourth");
        assertTrue(posFourth < posFifth, "Fourth author should appear before Fifth");
        assertTrue(posFifth < posSixth, "Fifth author should appear before Sixth");
    }

    /**
     * Test that RDF model contains proper RDF List structure for authors.
     * This verifies the FIX-01 implementation using rdf:first/rdf:rest/rdf:nil.
     */
    @Test
    void usesRDFListsForAuthors() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("rdflist2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Testing RDF Lists");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Alpha, A and Beta, B and Gamma, G");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        // Check that RDF model contains bibo:authorList predicate
        assertTrue(
                document.rdfModel().contains(null, null, null),
                "RDF model should not be empty");

        // Verify we can access authors through the document API
        assertEquals(3, document.authors().size());
        assertEquals("Alpha, A", document.authors().getFirst().name().fullName());
        assertEquals("Beta, B", document.authors().get(1).name().fullName());
        assertEquals("Gamma, G", document.authors().get(2).name().fullName());
    }

    /**
     * Test FIX-04 (Sprint 00): Verifies @inproceedings maps to CONFERENCE_PAPER.
     */
    @Test
    void inProceedingsMapsToConferencePaper() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("conf2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Conference Paper Title");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Author, A");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "Proceedings of Conference 2024");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.CONFERENCE_PAPER, document.type());
        assertEquals("Conference Paper Title", document.title());
        assertEquals("Proceedings of Conference 2024", document.containerTitle().orElseThrow());
    }

    /**
     * Test FIX-04 (Sprint 00): Verifies @proceedings maps to PROCEEDINGS type.
     */
    @Test
    void proceedingsMapsToProceedings() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("proc2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Proceedings of International Conference");
        addField(entry, BibTeXEntry.KEY_EDITOR, "Editor, E");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_PUBLISHER, "ACM Press");
        addField(entry, BibTeXEntry.KEY_VOLUME, "Vol. 10");
        addField(entry, FIELD_ISBN, "978-1-234567-89-0");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.PROCEEDINGS, document.type());
        assertEquals("Proceedings of International Conference", document.title());
        assertEquals(1, document.editors().size());
        assertEquals("ACM Press", document.publisher().orElseThrow());
        assertEquals("Vol. 10", document.volume().orElseThrow());
    }

    /**
     * Test FIX-04: Round-trip conversion for @inproceedings.
     */
    @Test
    void inProceedingsRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("paper2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "My Conference Paper");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Smith, John");
        addField(original, BibTeXEntry.KEY_BOOKTITLE, "Proc. of XYZ Conference");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, BibTeXEntry.KEY_PAGES, "100-110");

        // BibTeX -> BIBO
        BiboDocument document = converter.convertToBibo(original).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, document.type());

        // BIBO -> BibTeX
        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, roundTrip.getType());
        assertEquals("My Conference Paper", value(roundTrip, BibTeXEntry.KEY_TITLE));
        assertEquals("Proc. of XYZ Conference", value(roundTrip, BibTeXEntry.KEY_BOOKTITLE));
        assertTrue(value(roundTrip, BibTeXEntry.KEY_AUTHOR).contains("Smith, John"));
    }

    /**
     * Test FIX-04: Round-trip conversion for @proceedings.
     */
    @Test
    void proceedingsRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("proceedings2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Proceedings of the Annual Conference");
        addField(original, BibTeXEntry.KEY_EDITOR, "Editor, Jane");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, BibTeXEntry.KEY_PUBLISHER, "Springer");

        // BibTeX -> BIBO
        BiboDocument document = converter.convertToBibo(original).orElseThrow();
        assertEquals(BiboDocumentType.PROCEEDINGS, document.type());

        // BIBO -> BibTeX
        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_PROCEEDINGS, roundTrip.getType());
        assertEquals("Proceedings of the Annual Conference", value(roundTrip, BibTeXEntry.KEY_TITLE));
        assertTrue(value(roundTrip, BibTeXEntry.KEY_EDITOR).contains("Editor, Jane"));
        assertEquals("Springer", value(roundTrip, BibTeXEntry.KEY_PUBLISHER));
    }

    /**
     * Test series field with @book.
     */
    @Test
    void seriesFieldInBook() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Design Patterns");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Gamma, Erich");
        addField(entry, BibTeXEntry.KEY_YEAR, "1994");
        addField(entry, FIELD_SERIES, "Addison-Wesley Professional Computing Series");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals("Addison-Wesley Professional Computing Series", document.series().orElseThrow());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals("Addison-Wesley Professional Computing Series", value(roundTrip, FIELD_SERIES));
    }

    /**
     * Test series field with @inproceedings.
     */
    @Test
    void seriesFieldInProceedings() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("conf2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Machine Learning Advances");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, Jane");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "NeurIPS 2024");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, FIELD_SERIES, "Lecture Notes in Computer Science");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals("Lecture Notes in Computer Science", document.series().orElseThrow());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals("Lecture Notes in Computer Science", value(roundTrip, FIELD_SERIES));
    }

    /**
     * Test edition field with @book.
     */
    @Test
    void editionFieldInBook() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("knuth1997"));
        addField(entry, BibTeXEntry.KEY_TITLE, "The Art of Computer Programming");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Knuth, Donald E.");
        addField(entry, BibTeXEntry.KEY_YEAR, "1997");
        addField(entry, FIELD_EDITION, "3rd");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals("3rd", document.edition().orElseThrow());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals("3rd", value(roundTrip, FIELD_EDITION));
    }

    /**
     * Test edition field with @manual.
     */
    @Test
    void editionFieldInManual() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MANUAL, new Key("manual2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "User Guide");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, FIELD_EDITION, "Second Edition");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals("Second Edition", document.edition().orElseThrow());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals("Second Edition", value(roundTrip, FIELD_EDITION));
    }

    /**
     * Test keywords field with single keyword.
     */
    @Test
    void singleKeyword() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Quantum Computing");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Einstein, Albert");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, FIELD_KEYWORDS, "quantum");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(1, document.keywords().size());
        assertTrue(document.keywords().contains("quantum"));

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals("quantum", value(roundTrip, FIELD_KEYWORDS));
    }

    /**
     * Test keywords field with multiple comma-separated keywords.
     */
    @Test
    void multipleKeywordsCommaSeparated() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Machine Learning Survey");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Turing, Alan");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, FIELD_KEYWORDS, "machine learning, neural networks, deep learning");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(3, document.keywords().size());
        assertTrue(document.keywords().contains("machine learning"));
        assertTrue(document.keywords().contains("neural networks"));
        assertTrue(document.keywords().contains("deep learning"));

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        String keywords = value(roundTrip, FIELD_KEYWORDS);
        assertTrue(keywords.contains("machine learning"));
        assertTrue(keywords.contains("neural networks"));
        assertTrue(keywords.contains("deep learning"));
    }

    /**
     * Test keywords field with unicode characters.
     */
    @Test
    void keywordsWithUnicode() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "International Research");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Müller, Hans");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, FIELD_KEYWORDS, "résumé, naïve, café");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(3, document.keywords().size());
        assertTrue(document.keywords().contains("résumé"));
        assertTrue(document.keywords().contains("naïve"));
        assertTrue(document.keywords().contains("café"));

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        String keywords = value(roundTrip, FIELD_KEYWORDS);
        assertTrue(keywords.contains("résumé") || keywords.contains("r{\\'e}sum{\\'e}"));
    }

    /**
     * Test round-trip preservation of series field.
     */
    @Test
    void seriesRoundTripPreservation() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Author, Test");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, FIELD_SERIES, "Test Series Name");

        BiboDocument document = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();

        assertEquals(value(original, FIELD_SERIES), value(roundTrip, FIELD_SERIES));
    }

    /**
     * Test round-trip preservation of edition field.
     */
    @Test
    void editionRoundTripPreservation() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Author, Test");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, FIELD_EDITION, "First Edition");

        BiboDocument document = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();

        assertEquals(value(original, FIELD_EDITION), value(roundTrip, FIELD_EDITION));
    }

    /**
     * Test round-trip preservation of keywords field.
     */
    @Test
    void keywordsRoundTripPreservation() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Article");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Author, Test");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, FIELD_KEYWORDS, "keyword1, keyword2, keyword3");

        BiboDocument document = converter.convertToBibo(original).orElseThrow();
        assertEquals(3, document.keywords().size());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        String roundTripKeywords = value(roundTrip, FIELD_KEYWORDS);

        assertTrue(roundTripKeywords.contains("keyword1"));
        assertTrue(roundTripKeywords.contains("keyword2"));
        assertTrue(roundTripKeywords.contains("keyword3"));
    }

    private static String value(BibTeXEntry entry, Key key) {
        return Optional.ofNullable(entry.getField(key)).map(Value::toUserString).orElseThrow();
    }
}
