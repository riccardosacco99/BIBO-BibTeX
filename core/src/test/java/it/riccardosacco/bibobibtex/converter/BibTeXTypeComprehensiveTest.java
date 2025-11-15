package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test for all 14 standard BibTeX entry types.
 * Verifies bidirectional conversion (BibTeX ↔ BIBO) for each type.
 */
class BibTeXTypeComprehensiveTest {

    private BibTeXBibliographicConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BibTeXBibliographicConverter();
    }

    @Test
    void testArticleRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Article Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Smith, John"));
        entry.addField(BibTeXEntry.KEY_JOURNAL, value("Journal of Tests"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.ARTICLE, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_ARTICLE, roundtrip.getType());
    }

    @Test
    void testBookRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Book Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Author, Alice"));
        entry.addField(BibTeXEntry.KEY_PUBLISHER, value("Test Publisher"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.BOOK, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_BOOK, roundtrip.getType());
    }

    @Test
    void testInproceedingsRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Paper Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Researcher, Bob"));
        entry.addField(BibTeXEntry.KEY_BOOKTITLE, value("Proceedings of TestConf"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, roundtrip.getType());
    }

    @Test
    void testConferenceAliasForInproceedings() {
        // @conference should map to CONFERENCE_PAPER like @inproceedings
        BibTeXEntry entry = new BibTeXEntry(new Key("conference"), new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Paper Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Researcher, Bob"));
        entry.addField(BibTeXEntry.KEY_BOOKTITLE, value("Proceedings of TestConf"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, roundtrip.getType());
    }

    @Test
    void testProceedingsRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PROCEEDINGS, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Proceedings Title"));
        entry.addField(BibTeXEntry.KEY_EDITOR, value("Editor, Eve"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.PROCEEDINGS, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_PROCEEDINGS, roundtrip.getType());
    }

    @Test
    void testIncollectionRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INCOLLECTION, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Chapter Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Writer, Wendy"));
        entry.addField(BibTeXEntry.KEY_BOOKTITLE, value("Collected Works"));
        entry.addField(BibTeXEntry.KEY_PUBLISHER, value("Publisher Inc"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.BOOK_SECTION, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INCOLLECTION, roundtrip.getType());
    }

    @Test
    void testInbookRoundtrip() {
        // @inbook also maps to BOOK_SECTION
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INBOOK, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Chapter Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Author, A"));
        entry.addField(BibTeXEntry.KEY_BOOKTITLE, value("Book Title"));
        entry.addField(BibTeXEntry.KEY_PUBLISHER, value("Publisher"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));
        entry.addField(BibTeXEntry.KEY_PAGES, value("10-20"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.BOOK_SECTION, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        // Reverse mapping prefers @incollection
        assertEquals(BibTeXEntry.TYPE_INCOLLECTION, roundtrip.getType());
    }

    @Test
    void testPhdthesisRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_PHDTHESIS, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("PhD Thesis Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Scholar, Sam"));
        entry.addField(BibTeXEntry.KEY_SCHOOL, value("Test University"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.THESIS, doc.type());
        // degreeType should NOT be set for @phdthesis (default)
        assertFalse(doc.degreeType().isPresent());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_PHDTHESIS, roundtrip.getType());
    }

    @Test
    void testMastersthesisRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MASTERSTHESIS, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Master's Thesis Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Student, Sue"));
        entry.addField(BibTeXEntry.KEY_SCHOOL, value("Test University"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.THESIS, doc.type());
        // degreeType should be "Master's thesis"
        assertTrue(doc.degreeType().isPresent());
        assertEquals("Master's thesis", doc.degreeType().get());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        // Should map back to @mastersthesis, not @phdthesis
        assertEquals(BibTeXEntry.TYPE_MASTERSTHESIS, roundtrip.getType());
    }

    @Test
    void testTechreportRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_TECHREPORT, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Technical Report Title"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Engineer, Ed"));
        entry.addField(BibTeXEntry.KEY_INSTITUTION, value("Research Institute"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.REPORT, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_TECHREPORT, roundtrip.getType());
    }

    @Test
    void testBookletRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(new Key("booklet"), new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Booklet Title"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.BOOKLET, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals("booklet", roundtrip.getType().getValue());
    }

    @Test
    void testManualRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(new Key("manual"), new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Manual Title"));
        entry.addField(new Key("organization"), value("Manual Org"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.MANUAL, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals("manual", roundtrip.getType().getValue());
    }

    @Test
    void testUnpublishedRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(new Key("unpublished"), new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Unpublished Work"));
        entry.addField(BibTeXEntry.KEY_AUTHOR, value("Author, A"));
        entry.addField(BibTeXEntry.KEY_NOTE, value("Draft version"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.MANUSCRIPT, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals("unpublished", roundtrip.getType().getValue());
    }

    @Test
    void testMiscRoundtrip() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, value("Miscellaneous Item"));
        entry.addField(BibTeXEntry.KEY_YEAR, value("2024"));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.OTHER, doc.type());

        BibTeXEntry roundtrip = converter.convertFromBibo(doc).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_MISC, roundtrip.getType());
    }

    private StringValue value(String text) {
        return new StringValue(text, StringValue.Style.BRACED);
    }
}
