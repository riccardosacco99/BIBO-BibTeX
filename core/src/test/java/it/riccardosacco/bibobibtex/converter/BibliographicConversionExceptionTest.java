package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for validation and exception handling in bibliographic conversion.
 *
 * @since Sprint 01 - US-02
 */
class BibliographicConversionExceptionTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    @DisplayName("Converting null BibTeX entry throws exception")
    void testConvertNullBibTeXEntry() {
        BibliographicConversionException exception =
                assertThrows(
                        BibliographicConversionException.class, () -> converter.convertToBibo(null));

        assertTrue(exception.getMessage().contains("BibTeX entry"));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Converting BibTeX entry without title or citation key throws exception")
    void testConvertBibTeXEntryWithoutTitle() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, null);
        // No title field and no citation key

        BibliographicConversionException exception =
                assertThrows(
                        BibliographicConversionException.class, () -> converter.convertToBibo(entry));

        assertTrue(exception.getMessage().contains("title"));
    }

    @Test
    @DisplayName("Converting null BIBO document throws exception")
    void testConvertNullBiboDocument() {
        BibliographicConversionException exception =
                assertThrows(
                        BibliographicConversionException.class, () -> converter.convertFromBibo(null));

        assertTrue(exception.getMessage().contains("BIBO document"));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Exception factory methods create correct messages")
    void testExceptionFactoryMethods() {
        BibliographicConversionException nullInputEx =
                BibliographicConversionException.nullInput("test input");
        assertTrue(nullInputEx.getMessage().contains("test input"));
        assertTrue(nullInputEx.getMessage().contains("null"));

        BibliographicConversionException missingFieldEx =
                BibliographicConversionException.missingRequiredField("author");
        assertTrue(missingFieldEx.getMessage().contains("author"));
        assertTrue(missingFieldEx.getMessage().toLowerCase().contains("missing") ||
                   missingFieldEx.getMessage().toLowerCase().contains("required"));

        BibliographicConversionException invalidFieldEx =
                BibliographicConversionException.invalidFieldValue("year", "invalid", "not a number");
        assertTrue(invalidFieldEx.getMessage().contains("year"));
        assertTrue(invalidFieldEx.getMessage().contains("invalid"));
        assertTrue(invalidFieldEx.getMessage().contains("not a number"));

        BibliographicConversionException invalidIdentifierEx =
                BibliographicConversionException.invalidIdentifier("ISBN", "123", "10 or 13 digits");
        assertTrue(invalidIdentifierEx.getMessage().contains("ISBN"));
        assertTrue(invalidIdentifierEx.getMessage().contains("123"));
        assertTrue(invalidIdentifierEx.getMessage().contains("10 or 13 digits"));
    }

    @Test
    @DisplayName("Valid BibTeX entry converts successfully")
    void testValidBibTeXEntryConverts() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Article", StringValue.Style.BRACED));

        assertDoesNotThrow(() -> converter.convertToBibo(entry));
    }

    @Test
    @DisplayName("Valid BIBO document converts successfully")
    void testValidBiboDocumentConverts() {
        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Test Article").id("test2024").build();

        assertDoesNotThrow(() -> converter.convertFromBibo(document));
    }
}
