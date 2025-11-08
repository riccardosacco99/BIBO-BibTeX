package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for all BibTeX entry types (Sprint 02 - US-08).
 * Tests all 14 standard BibTeX types plus custom types.
 */
class BibTeXTypeComprehensiveTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    void testBookletConversion() {
        // Given: A @booklet entry
        BibTeXEntry entry = new BibTeXEntry(new Key("booklet"), new Key("tourguide2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Paris Tourist Guide", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        entry.addField(new Key("howpublished"), new StringValue("Self-published", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should map to BOOKLET type
        assertEquals(BiboDocumentType.BOOKLET, doc.type());
        assertEquals("Paris Tourist Guide", doc.title());
        assertEquals(2024, doc.publicationDate().orElseThrow().year());
        assertEquals("Self-published", doc.howPublished().orElse(null));
    }

    @Test
    void testBookletRoundTrip() {
        // Given: A booklet
        BibTeXEntry original = new BibTeXEntry(new Key("booklet"), new Key("guide"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Travel Guide", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));

        // When: Round-trip conversion
        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(doc).orElseThrow();

        // Then: Type should be preserved
        assertEquals(new Key("booklet"), result.getType());
        assertEquals("Travel Guide", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
    }

    @Test
    void testManualConversion() {
        // Given: A @manual entry
        BibTeXEntry entry = new BibTeXEntry(new Key("manual"), new Key("latex-manual"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("LaTeX User Manual", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_ORGANIZATION, new StringValue("LaTeX Project", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2023", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_EDITION, new StringValue("3rd", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should map to MANUAL type
        assertEquals(BiboDocumentType.MANUAL, doc.type());
        assertEquals("LaTeX User Manual", doc.title());
        assertEquals("LaTeX Project", doc.organization().orElse(null));
        assertEquals("3rd", doc.edition().orElse(null));
    }

    @Test
    void testManualRoundTrip() {
        // Given: A manual
        BibTeXEntry original = new BibTeXEntry(new Key("manual"), new Key("manual2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Software Manual", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_ORGANIZATION, new StringValue("ACME Corp", StringValue.Style.BRACED));

        // When: Round-trip conversion
        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(doc).orElseThrow();

        // Then: Type should be preserved
        assertEquals(new Key("manual"), result.getType());
        assertEquals("Software Manual", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
        assertEquals("ACME Corp", result.getField(new Key("organization")).toUserString());
    }

    @Test
    void testUnpublishedConversion() {
        // Given: An @unpublished entry
        BibTeXEntry entry = new BibTeXEntry(new Key("unpublished"), new Key("draft-paper"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("My Research Draft", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("John Doe", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_NOTE, new StringValue("Work in progress", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should map to UNPUBLISHED type
        assertEquals(BiboDocumentType.UNPUBLISHED, doc.type());
        assertEquals("My Research Draft", doc.title());
        assertEquals("John Doe", doc.authors().get(0).name().fullName());
        assertEquals("Work in progress", doc.notes().orElse(null));
    }

    @Test
    void testUnpublishedRoundTrip() {
        // Given: An unpublished work
        BibTeXEntry original = new BibTeXEntry(new Key("unpublished"), new Key("draft"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Draft Paper", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Smith, Alice", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_NOTE, new StringValue("Manuscript", StringValue.Style.BRACED));

        // When: Round-trip conversion
        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(doc).orElseThrow();

        // Then: Type should be preserved
        assertEquals(new Key("unpublished"), result.getType());
        assertEquals("Draft Paper", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
    }

    @Test
    void testMastersThesisDegreeType() {
        // Given: A @mastersthesis entry
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MASTERSTHESIS, new Key("ms-thesis"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("My Masters Thesis", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Jane Smith", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_SCHOOL, new StringValue("MIT", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        entry.addField(new Key("type"), new StringValue("M.Sc. Thesis", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should preserve degree type
        assertEquals(BiboDocumentType.THESIS, doc.type());
        assertEquals("M.Sc. Thesis", doc.degreeType().orElse(null));
        assertEquals("MIT", doc.publisher().orElse(null));
    }

    @Test
    void testPhdThesisDegreeType() {
        // Given: A @phdthesis entry
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PHDTHESIS, new Key("phd-thesis"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("My PhD Dissertation", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Dr. Bob", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_SCHOOL, new StringValue("Stanford", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2023", StringValue.Style.BRACED));
        entry.addField(new Key("type"), new StringValue("PhD Dissertation", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should preserve degree type
        assertEquals(BiboDocumentType.THESIS, doc.type());
        assertEquals("PhD Dissertation", doc.degreeType().orElse(null));
        assertEquals("Stanford", doc.publisher().orElse(null));
    }

    @Test
    void testThesisRoundTripPreservesDegreeType() {
        // Given: A masters thesis with degree type
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_MASTERSTHESIS, new Key("thesis"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Thesis Title", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Student, Alice", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_SCHOOL, new StringValue("University", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        original.addField(new Key("type"), new StringValue("M.Sc.", StringValue.Style.BRACED));

        // When: Round-trip conversion
        BiboDocument doc = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(doc).orElseThrow();

        // Then: Degree type should be preserved
        assertNotNull(result.getField(new Key("type")));
        assertEquals("M.Sc.", result.getField(new Key("type")).toUserString());
    }

    @Test
    void testAllStandardTypesConvert() {
        // Test that all 14 standard BibTeX types can be converted without errors
        Key[] types = {
            BibTeXEntry.TYPE_ARTICLE,
            BibTeXEntry.TYPE_BOOK,
            new Key("booklet"),
            BibTeXEntry.TYPE_INBOOK,
            BibTeXEntry.TYPE_INCOLLECTION,
            BibTeXEntry.TYPE_INPROCEEDINGS,
            new Key("manual"),
            BibTeXEntry.TYPE_MASTERSTHESIS,
            BibTeXEntry.TYPE_MISC,
            BibTeXEntry.TYPE_PHDTHESIS,
            BibTeXEntry.TYPE_PROCEEDINGS,
            BibTeXEntry.TYPE_TECHREPORT,
            new Key("unpublished"),
            new Key("online")
        };

        for (Key type : types) {
            BibTeXEntry entry = new BibTeXEntry(type, new Key("test-" + type.getValue()));
            entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test " + type.getValue(), StringValue.Style.BRACED));
            entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));

            BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
            assertNotNull(doc, "Type " + type.getValue() + " should convert");
            assertNotNull(doc.type(), "Type " + type.getValue() + " should have a BIBO type");
        }
    }

    @Test
    void testMiscFallback() {
        // Given: An unknown/custom BibTeX type
        BibTeXEntry entry = new BibTeXEntry(new Key("customtype"), new Key("custom"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Custom Type Document", StringValue.Style.BRACED));

        // When: Convert to BIBO
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Then: Should fall back to OTHER
        assertEquals(BiboDocumentType.OTHER, doc.type());
    }

    @Test
    void testOtherTypeMapsToMisc() {
        // Given: A BIBO document with OTHER type
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.OTHER, "Other Document").build();

        // When: Convert to BibTeX
        BibTeXEntry entry = converter.convertFromBibo(doc).orElseThrow();

        // Then: Should map to @misc
        assertEquals(BibTeXEntry.TYPE_MISC, entry.getType());
    }
}
