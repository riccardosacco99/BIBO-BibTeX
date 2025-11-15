package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

/**
 * Tests for BibTeX.com field conventions (US-24).
 * Tests context-aware field semantics based on entry type.
 */
class BibTeXConventionsTest {
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    private static void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    /**
     * Tests that address in @inproceedings maps to conference location (not publisher location).
     * Reference: https://www.bibtex.com/e/entry-types/#inproceedings
     */
    @Test
    void testInproceedingsAddressIsConferenceLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("smith2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Semantic Web Technologies");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, Alice");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "International Conference on Web Engineering");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Berlin, Germany");  // Conference location

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.CONFERENCE_PAPER, doc.type());
        assertTrue(doc.conferenceLocation().isPresent(), "Conference location should be set");
        assertEquals("Berlin, Germany", doc.conferenceLocation().orElseThrow());
        assertTrue(doc.placeOfPublication().isEmpty(), "Publisher location should not be set");
    }

    /**
     * Tests that address in @proceedings maps to conference location.
     */
    @Test
    void testProceedingsAddressIsConferenceLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("icwe2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Proceedings of ICWE 2024");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Paris, France");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.PROCEEDINGS, doc.type());
        assertTrue(doc.conferenceLocation().isPresent());
        assertEquals("Paris, France", doc.conferenceLocation().orElseThrow());
        assertTrue(doc.placeOfPublication().isEmpty());
    }

    /**
     * Tests that address in @book maps to publisher location (not conference).
     * Reference: https://www.bibtex.com/e/entry-types/#book
     */
    @Test
    void testBookAddressIsPublisherLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("knuth1984"));
        addField(entry, BibTeXEntry.KEY_TITLE, "The TeXbook");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Knuth, Donald E.");
        addField(entry, BibTeXEntry.KEY_PUBLISHER, "Addison-Wesley");
        addField(entry, BibTeXEntry.KEY_YEAR, "1984");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Reading, Massachusetts");  // Publisher location

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.BOOK, doc.type());
        assertTrue(doc.placeOfPublication().isPresent(), "Publisher location should be set");
        assertEquals("Reading, Massachusetts", doc.placeOfPublication().orElseThrow());
        assertTrue(doc.conferenceLocation().isEmpty(), "Conference location should not be set");
    }

    /**
     * Tests that address in @article maps to publisher location.
     */
    @Test
    void testArticleAddressIsPublisherLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("doe2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Machine Learning Advances");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Doe, John");
        addField(entry, BibTeXEntry.KEY_JOURNAL, "Journal of AI Research");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "New York, NY");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.ARTICLE, doc.type());
        assertEquals("New York, NY", doc.placeOfPublication().orElseThrow());
        assertTrue(doc.conferenceLocation().isEmpty());
    }

    /**
     * Tests that organization in @proceedings maps to conference organizer.
     * Reference: https://www.bibtex.com/e/entry-types/#proceedings
     */
    @Test
    void testProceedingsOrganizationIsConferenceOrganizer() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("www2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Proceedings of WWW 2024");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_ORGANIZATION, "ACM");  // Conference organizer

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.conferenceOrganizer().isPresent());
        assertEquals("ACM", doc.conferenceOrganizer().orElseThrow());
    }

    /**
     * Tests that organization in @inproceedings maps to conference organizer.
     */
    @Test
    void testInproceedingsOrganizationIsConferenceOrganizer() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("jones2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Graph Neural Networks");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Jones, Bob");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "NeurIPS 2024");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_ORGANIZATION, "IEEE");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.conferenceOrganizer().isPresent());
        assertEquals("IEEE", doc.conferenceOrganizer().orElseThrow());
    }

    /**
     * Tests that organization in @manual maps to publisher.
     * Reference: https://www.bibtex.com/e/entry-types/#manual
     */
    @Test
    void testManualOrganizationIsPublisher() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MANUAL, new Key("gcc2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "GNU Compiler Collection Manual");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_ORGANIZATION, "Free Software Foundation");  // Publisher

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.publisher().isPresent());
        assertEquals("Free Software Foundation", doc.publisher().orElseThrow());
        assertTrue(doc.conferenceOrganizer().isEmpty());
    }

    /**
     * Tests that @phdthesis without explicit type field does NOT set degreeType (it's the default).
     */
    @Test
    void testPhdThesisDegreeType() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PHDTHESIS, new Key("brown2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Advanced Algorithms");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Brown, Charlie");
        addField(entry, BibTeXEntry.KEY_SCHOOL, "MIT");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // PhD thesis without explicit type field should NOT have degreeType (it's the default)
        assertTrue(doc.degreeType().isEmpty());
    }

    /**
     * Tests that type field in @mastersthesis maps to degree type.
     */
    @Test
    void testMastersThesisDegreeType() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MASTERSTHESIS, new Key("green2022"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Database Optimization");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Green, Dana");
        addField(entry, BibTeXEntry.KEY_SCHOOL, "Stanford University");
        addField(entry, BibTeXEntry.KEY_YEAR, "2022");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.degreeType().isPresent());
        assertEquals("Master's thesis", doc.degreeType().orElseThrow());
    }

    /**
     * Tests that explicit type field overrides inferred degree type.
     */
    @Test
    void testExplicitDegreeTypeOverride() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PHDTHESIS, new Key("white2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Quantum Computing");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "White, Eve");
        addField(entry, BibTeXEntry.KEY_SCHOOL, "Caltech");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_TYPE, "Doctoral dissertation");  // Explicit override

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.degreeType().isPresent());
        assertEquals("Doctoral dissertation", doc.degreeType().orElseThrow());
    }

    /**
     * Tests roundtrip: @inproceedings → BIBO → @inproceedings preserves conference location.
     */
    @Test
    void testInproceedingsRoundTripPreservesConferenceLocation() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("test2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Paper");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Test, Author");
        addField(original, BibTeXEntry.KEY_BOOKTITLE, "Test Conference");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, BibTeXEntry.KEY_ADDRESS, "Tokyo, Japan");

        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();

        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, roundtrip.getType());
        assertEquals("Tokyo, Japan", roundtrip.getField(BibTeXEntry.KEY_ADDRESS).toUserString());
    }

    /**
     * Tests roundtrip: @book → BIBO → @book preserves publisher location.
     */
    @Test
    void testBookRoundTripPreservesPublisherLocation() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("test2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(original, BibTeXEntry.KEY_AUTHOR, "Test, Author");
        addField(original, BibTeXEntry.KEY_PUBLISHER, "Test Publisher");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, BibTeXEntry.KEY_ADDRESS, "London, UK");

        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();

        assertEquals(BibTeXEntry.TYPE_BOOK, roundtrip.getType());
        assertEquals("London, UK", roundtrip.getField(BibTeXEntry.KEY_ADDRESS).toUserString());
    }

    /**
     * Tests roundtrip: @proceedings → BIBO → @proceedings preserves organizer.
     */
    @Test
    void testProceedingsRoundTripPreservesOrganizer() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("conf2024"));
        addField(original, BibTeXEntry.KEY_TITLE, "Test Proceedings");
        addField(original, BibTeXEntry.KEY_YEAR, "2024");
        addField(original, BibTeXEntry.KEY_ORGANIZATION, "ACM");

        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();

        assertEquals(BibTeXEntry.TYPE_PROCEEDINGS, roundtrip.getType());
        assertEquals("ACM", roundtrip.getField(BibTeXEntry.KEY_ORGANIZATION).toUserString());
    }

    /**
     * Tests that @phdthesis address (university location) maps to publisher location.
     */
    @Test
    void testThesisAddressIsPublisherLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PHDTHESIS, new Key("black2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Neural Architecture Search");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Black, Frank");
        addField(entry, BibTeXEntry.KEY_SCHOOL, "UC Berkeley");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Berkeley, CA");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.THESIS, doc.type());
        assertEquals("Berkeley, CA", doc.placeOfPublication().orElseThrow());
        assertTrue(doc.conferenceLocation().isEmpty());
    }

    /**
     * Tests that @techreport address maps to institution location (publisher location).
     */
    @Test
    void testTechReportAddressIsPublisherLocation() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_TECHREPORT, new Key("nasa2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Mission Analysis Report");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Johnson, Sarah");
        addField(entry, BibTeXEntry.KEY_INSTITUTION, "NASA");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Houston, TX");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(BiboDocumentType.REPORT, doc.type());
        assertEquals("Houston, TX", doc.placeOfPublication().orElseThrow());
        assertTrue(doc.conferenceLocation().isEmpty());
    }
}
