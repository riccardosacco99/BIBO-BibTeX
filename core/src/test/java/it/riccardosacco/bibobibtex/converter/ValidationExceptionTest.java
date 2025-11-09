package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.exception.ValidationException;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests validation exception behavior for BibTeX entries and BIBO documents.
 */
class ValidationExceptionTest {

    // Null inputs (3 tests)

    @Test
    void validateBibTeXEntry_nullEntry_throwsException() {
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(null)
        );
        assertEquals("BibTeX entry cannot be null", ex.getMessage());
    }

    @Test
    void validateBiboDocument_nullDocument_throwsException() {
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBiboDocument(null)
        );
        assertEquals("BIBO document cannot be null", ex.getMessage());
    }

    @Test
    void validateIdentifier_emptyOrNull_throwsException() {
        // BiboIdentifier constructor already validates blank/null values
        assertThrows(
            NullPointerException.class,
            () -> new BiboIdentifier(BiboIdentifierType.DOI, null)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new BiboIdentifier(BiboIdentifierType.DOI, "")
        );
    }

    // Missing required fields (4 tests)

    @Test
    void validateBibTeXEntry_missingType_throwsException() {
        BibTeXEntry entry = new BibTeXEntry(null, new Key("test"));
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(entry)
        );
        assertTrue(ex.getMessage().contains("type is required"));
    }

    @Test
    void validateBibTeXEntry_missingTitle_throwsException() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test"));
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(entry)
        );
        assertTrue(ex.getMessage().contains("Title is required"));
        assertEquals("title", ex.getFieldName());
    }

    @Test
    void validateBibTeXEntry_blankTitle_throwsException() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("   ", StringValue.Style.BRACED));
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(entry)
        );
        assertTrue(ex.getMessage().contains("Title is required"));
    }

    @Test
    void validateBiboDocument_invalidYear_throwsException() {
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.ARTICLE, "Test")
            .publicationDate(BiboPublicationDate.ofYear(999))
            .build();
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBiboDocument(doc)
        );
        assertTrue(ex.getMessage().contains("must be >= 1000"));
    }

    // Invalid identifiers (6 tests)

    @Test
    void validateIdentifier_invalidISBN10Checksum_throwsException() {
        BiboIdentifier isbn = new BiboIdentifier(BiboIdentifierType.ISBN_10, "0123456780"); // Wrong checksum
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(isbn)
        );
        assertTrue(ex.getMessage().contains("Invalid ISBN-10 checksum"));
    }

    @Test
    void validateIdentifier_invalidISBN13Checksum_throwsException() {
        BiboIdentifier isbn = new BiboIdentifier(BiboIdentifierType.ISBN_13, "9780123456780"); // Wrong checksum
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(isbn)
        );
        assertTrue(ex.getMessage().contains("Invalid ISBN-13 checksum"));
    }

    @Test
    void validateIdentifier_invalidISBNLength_throwsException() {
        BiboIdentifier isbn = new BiboIdentifier(BiboIdentifierType.ISBN_10, "123456789"); // 9 digits
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(isbn)
        );
        assertTrue(ex.getMessage().contains("must be 10 or 13 digits"));
    }

    @Test
    void validateIdentifier_invalidDOIFormat_throwsException() {
        BiboIdentifier doi = new BiboIdentifier(BiboIdentifierType.DOI, "not-a-doi");
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(doi)
        );
        assertTrue(ex.getMessage().contains("Invalid DOI format"));
    }

    @Test
    void validateIdentifier_doiWithWhitespace_throwsException() {
        BiboIdentifier doi = new BiboIdentifier(BiboIdentifierType.DOI, "10.1234/test paper");
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(doi)
        );
        assertTrue(ex.getMessage().contains("cannot contain whitespace"));
    }

    @Test
    void validateIdentifier_invalidURL_throwsException() {
        BiboIdentifier url = new BiboIdentifier(BiboIdentifierType.URL, "not a url");
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(url)
        );
        assertTrue(ex.getMessage().contains("Invalid URL format"));
    }

    // Invalid dates (3 tests)

    @Test
    void validateBiboDocument_yearTooOld_throwsException() {
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.ARTICLE, "Test")
            .publicationDate(BiboPublicationDate.ofYear(999))
            .build();
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBiboDocument(doc)
        );
        assertTrue(ex.getMessage().contains("must be >= 1000"));
    }

    @Test
    void validateBiboDocument_yearTooFarInFuture_throwsException() {
        int futureYear = java.time.Year.now().getValue() + 10;
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.ARTICLE, "Test")
            .publicationDate(BiboPublicationDate.ofYear(futureYear))
            .build();
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBiboDocument(doc)
        );
        assertTrue(ex.getMessage().contains("cannot be more than"));
    }

    @Test
    void validateBiboDocument_invalidMonthInConstructor_throwsException() {
        // BiboPublicationDate already validates month in constructor
        assertThrows(
            IllegalArgumentException.class,
            () -> BiboPublicationDate.ofYearMonth(2024, 13)
        );
    }

    // Error message clarity (4 tests)

    @Test
    void validationException_containsFieldName() {
        BiboIdentifier doi = new BiboIdentifier(BiboIdentifierType.DOI, "invalid");
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(doi)
        );
        assertEquals("doi", ex.getFieldName());
    }

    @Test
    void validationException_containsFieldValue() {
        BiboIdentifier doi = new BiboIdentifier(BiboIdentifierType.DOI, "invalid");
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateIdentifier(doi)
        );
        assertEquals("invalid", ex.getFieldValue());
    }

    @Test
    void validateBibTeXEntry_invalidCitationKey_throwsException() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("invalid key!"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test", StringValue.Style.BRACED));
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(entry)
        );
        assertTrue(ex.getMessage().contains("Citation key"));
        assertTrue(ex.getMessage().contains("invalid characters"));
    }

    @Test
    void validationException_messageIncludesValue() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test"));
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> BibliographicValidator.validateBibTeXEntry(entry)
        );
        // Should include field name in message
        String message = ex.getMessage();
        assertTrue(message.contains("Title") || message.contains("title"));
    }

    // Additional edge cases

    @Test
    void validateIdentifier_validISBN10_doesNotThrow() {
        BiboIdentifier isbn = new BiboIdentifier(BiboIdentifierType.ISBN_10, "0-306-40615-2");
        assertDoesNotThrow(() -> BibliographicValidator.validateIdentifier(isbn));
    }

    @Test
    void validateIdentifier_validISBN13_doesNotThrow() {
        BiboIdentifier isbn = new BiboIdentifier(BiboIdentifierType.ISBN_13, "978-0-306-40615-7");
        assertDoesNotThrow(() -> BibliographicValidator.validateIdentifier(isbn));
    }

    @Test
    void validateIdentifier_validDOI_doesNotThrow() {
        BiboIdentifier doi = new BiboIdentifier(BiboIdentifierType.DOI, "10.1234/test.article");
        assertDoesNotThrow(() -> BibliographicValidator.validateIdentifier(doi));
    }

    @Test
    void validateIdentifier_validURL_doesNotThrow() {
        BiboIdentifier url = new BiboIdentifier(BiboIdentifierType.URL, "https://example.com/article");
        assertDoesNotThrow(() -> BibliographicValidator.validateIdentifier(url));
    }

    @Test
    void validateBiboDocument_feb29LeapYear_doesNotThrow() {
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.ARTICLE, "Test")
            .publicationDate(BiboPublicationDate.ofFullDate(2024, 2, 29))
            .build();
        assertDoesNotThrow(() -> BibliographicValidator.validateBiboDocument(doc));
    }

    @Test
    void validateBiboDocument_validYear_doesNotThrow() {
        BiboDocument doc = BiboDocument.builder(BiboDocumentType.ARTICLE, "Test")
            .publicationDate(BiboPublicationDate.ofYear(2024))
            .build();
        assertDoesNotThrow(() -> BibliographicValidator.validateBiboDocument(doc));
    }

    @Test
    void validateBibTeXEntry_valid_doesNotThrow() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Test Article", StringValue.Style.BRACED));
        assertDoesNotThrow(() -> BibliographicValidator.validateBibTeXEntry(entry));
    }
}
