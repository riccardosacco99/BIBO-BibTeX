package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive round-trip conversion tests to ensure data preservation.
 *
 * @since Sprint 01 - US-05
 */
class RoundTripConversionTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    @DisplayName("Article with all fields survives round-trip")
    void testArticleFullRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("smith2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Complete Article", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Smith, John and Doe, Jane", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_JOURNAL, new StringValue("Nature", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_VOLUME, new StringValue("123", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_NUMBER, new StringValue("4", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_PAGES, new StringValue("100--110", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_DOI, new StringValue("10.1000/test.2024", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("Complete Article", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
        assertTrue(result.getField(BibTeXEntry.KEY_AUTHOR).toUserString().contains("Smith"));
        assertTrue(result.getField(BibTeXEntry.KEY_AUTHOR).toUserString().contains("Doe"));
        assertEquals("Nature", result.getField(BibTeXEntry.KEY_JOURNAL).toUserString());
        assertEquals("2024", result.getField(BibTeXEntry.KEY_YEAR).toUserString());
        assertEquals("123", result.getField(BibTeXEntry.KEY_VOLUME).toUserString());
        assertEquals("4", result.getField(BibTeXEntry.KEY_NUMBER).toUserString());
        assertEquals("100--110", result.getField(BibTeXEntry.KEY_PAGES).toUserString());
        assertEquals("10.1000/test.2024", result.getField(BibTeXEntry.KEY_DOI).toUserString());
    }

    @Test
    @DisplayName("Book with series and edition survives round-trip")
    void testBookWithSeriesRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("jones2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Programming Guide", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Jones, Alice", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_PUBLISHER, new StringValue("Springer", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_SERIES, new StringValue("LNCS", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_EDITION, new StringValue("2nd", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("Programming Guide", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
        assertEquals("Springer", result.getField(BibTeXEntry.KEY_PUBLISHER).toUserString());
        assertEquals("LNCS", result.getField(BibTeXEntry.KEY_SERIES).toUserString());
        assertEquals("2nd", result.getField(BibTeXEntry.KEY_EDITION).toUserString());
    }

    @Test
    @DisplayName("Conference paper with booktitle survives round-trip")
    void testConferencePaperRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("miller2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("AI Research", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Miller, Bob", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_BOOKTITLE, new StringValue("Proceedings of ICML 2024", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_PAGES, new StringValue("50--60", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, bibo.type());
        assertTrue(bibo.containerTitle().isPresent());
        assertEquals("Proceedings of ICML 2024", bibo.containerTitle().get());

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("AI Research", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
        assertEquals("Proceedings of ICML 2024", result.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString());
        assertEquals("2024", result.getField(BibTeXEntry.KEY_YEAR).toUserString());
    }

    @Test
    @DisplayName("BIBO to BibTeX to BIBO preserves document structure")
    void testBiboRoundTrip() {
        BiboDocument original = BiboDocument.builder(BiboDocumentType.ARTICLE, "Original Article")
                .id("original2024")
                .addAuthor(BiboPersonName.builder("John Smith")
                        .givenName("John")
                        .familyName("Smith")
                        .build())
                .publicationDate(BiboPublicationDate.ofFullDate(2024, 6, 15))
                .publisher("IEEE")
                .containerTitle("IEEE Transactions")
                .volume("42")
                .issue("3")
                .pages("200--250")
                .series("Computer Science Series")
                .edition("Special Edition")
                .addKeyword("machine learning")
                .addKeyword("deep learning")
                .build();

        BibTeXEntry bibtex = converter.convertFromBibo(original).orElseThrow();
        BiboDocument result = converter.convertToBibo(bibtex).orElseThrow();

        assertEquals(original.type(), result.type());
        assertEquals(original.title(), result.title());
        assertEquals(original.publisher(), result.publisher());
        assertEquals(original.volume(), result.volume());
        assertEquals(original.issue(), result.issue());
        assertEquals(original.series(), result.series());
        assertEquals(original.edition(), result.edition());
        assertTrue(result.keywords().containsAll(original.keywords()));
        assertFalse(result.authors().isEmpty());
    }

    @Test
    @DisplayName("Minimal article survives round-trip")
    void testMinimalArticleRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("minimal2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Minimal", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("Minimal", result.getField(BibTeXEntry.KEY_TITLE).toUserString());
    }

    @Test
    @DisplayName("Multiple authors preserved in order")
    void testMultipleAuthorsOrder() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("multi2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Multi-Author Paper", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_AUTHOR,
                new StringValue("First, Author and Second, Author and Third, Author", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        assertEquals(3, bibo.authors().size());

        // Check order is preserved
        assertEquals("Author", bibo.authors().get(0).name().givenName().orElseThrow());
        assertEquals("First", bibo.authors().get(0).name().familyName().orElseThrow());

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();
        String authors = result.getField(BibTeXEntry.KEY_AUTHOR).toUserString();

        // All authors should be present
        assertTrue(authors.contains("First"));
        assertTrue(authors.contains("Second"));
        assertTrue(authors.contains("Third"));
    }
}
