package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for extended BibTeX fields added in Sprint 01.
 *
 * @since Sprint 01 - US-03, US-04
 */
class ExtendedFieldsTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    @DisplayName("Series field is preserved in round-trip conversion")
    void testSeriesRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("test2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Book", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_SERIES, new StringValue("Lecture Notes in Computer Science", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("Lecture Notes in Computer Science",
                result.getField(BibTeXEntry.KEY_SERIES).toUserString());
    }

    @Test
    @DisplayName("Edition field is preserved in round-trip conversion")
    void testEditionRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("test2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Book", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_EDITION, new StringValue("2nd", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertEquals("2nd", result.getField(BibTeXEntry.KEY_EDITION).toUserString());
    }

    @Test
    @DisplayName("Keywords field is preserved in round-trip conversion")
    void testKeywordsRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Article", StringValue.Style.BRACED));
        original.addField(new Key("keywords"), new StringValue("machine learning, AI, neural networks", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        assertEquals(3, bibo.keywords().size());
        assertTrue(bibo.keywords().contains("machine learning"));
        assertTrue(bibo.keywords().contains("AI"));
        assertTrue(bibo.keywords().contains("neural networks"));

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();
        String keywords = result.getField(new Key("keywords")).toUserString();
        assertTrue(keywords.contains("machine learning"));
        assertTrue(keywords.contains("AI"));
        assertTrue(keywords.contains("neural networks"));
    }

    @Test
    @DisplayName("Organization field is preserved in round-trip conversion")
    void testOrganizationRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("test2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Paper", StringValue.Style.BRACED));
        original.addField(new Key("organization"), new StringValue("ACM", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        assertTrue(bibo.organization().isPresent());
        assertEquals("ACM", bibo.organization().get());

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();
        assertEquals("ACM", result.getField(new Key("organization")).toUserString());
    }

    @Test
    @DisplayName("HowPublished field is preserved for misc entries")
    void testHowPublishedRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("test2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Misc", StringValue.Style.BRACED));
        original.addField(new Key("howpublished"), new StringValue("Online", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();
        assertTrue(bibo.howPublished().isPresent());
        assertEquals("Online", bibo.howPublished().get());

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();
        assertEquals("Online", result.getField(new Key("howpublished")).toUserString());
    }

    @Test
    @DisplayName("All extended fields together in round-trip conversion")
    void testAllExtendedFieldsRoundTrip() {
        BibTeXEntry original = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("comprehensive2024"));
        original.addField(BibTeXEntry.KEY_TITLE, new StringValue("Comprehensive Book", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_SERIES, new StringValue("Series Name", StringValue.Style.BRACED));
        original.addField(BibTeXEntry.KEY_EDITION, new StringValue("3rd", StringValue.Style.BRACED));
        original.addField(new Key("keywords"), new StringValue("keyword1, keyword2", StringValue.Style.BRACED));

        BiboDocument bibo = converter.convertToBibo(original).orElseThrow();

        assertTrue(bibo.series().isPresent());
        assertTrue(bibo.edition().isPresent());
        assertEquals(2, bibo.keywords().size());

        BibTeXEntry result = converter.convertFromBibo(bibo).orElseThrow();

        assertNotNull(result.getField(BibTeXEntry.KEY_SERIES));
        assertNotNull(result.getField(BibTeXEntry.KEY_EDITION));
        assertNotNull(result.getField(new Key("keywords")));
    }
}
