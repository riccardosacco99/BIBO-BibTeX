package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test cases for IdentifierValidator utility class.
 *
 * <p>Covers validation for:
 * <ul>
 *   <li>ISBN-10 with checksum</li>
 *   <li>ISBN-13 with checksum</li>
 *   <li>ISSN with checksum</li>
 *   <li>DOI format</li>
 *   <li>Handle format</li>
 *   <li>URL format</li>
 * </ul>
 */
class IdentifierValidationTest {

    // ==================== ISBN-10 Tests ====================

    @Test
    void isbn10_Valid_WithHyphens() {
        assertTrue(IdentifierValidator.validateISBN10("0-306-40615-2"));
    }

    @Test
    void isbn10_Valid_WithoutHyphens() {
        assertTrue(IdentifierValidator.validateISBN10("0306406152"));
    }

    @Test
    void isbn10_Valid_WithX() {
        assertTrue(IdentifierValidator.validateISBN10("043942089X"));
        assertTrue(IdentifierValidator.validateISBN10("0-439-42089-X"));
    }

    @Test
    void isbn10_Valid_AnotherExample() {
        // Harry Potter ISBN-10
        assertTrue(IdentifierValidator.validateISBN10("0-7475-3269-9"));
    }

    @Test
    void isbn10_Invalid_WrongChecksum() {
        assertFalse(IdentifierValidator.validateISBN10("0-306-40615-3"));
        assertFalse(IdentifierValidator.validateISBN10("0306406153"));
    }

    @Test
    void isbn10_Invalid_TooShort() {
        assertFalse(IdentifierValidator.validateISBN10("123456789"));
    }

    @Test
    void isbn10_Invalid_TooLong() {
        assertFalse(IdentifierValidator.validateISBN10("12345678901"));
    }

    @Test
    void isbn10_Invalid_ContainsLetters() {
        assertFalse(IdentifierValidator.validateISBN10("03064A6152"));
    }

    @Test
    void isbn10_Invalid_XNotAtEnd() {
        assertFalse(IdentifierValidator.validateISBN10("0X06406152"));
    }

    @Test
    void isbn10_Null() {
        assertFalse(IdentifierValidator.validateISBN10(null));
    }

    // ==================== ISBN-13 Tests ====================

    @Test
    void isbn13_Valid_WithHyphens() {
        assertTrue(IdentifierValidator.validateISBN13("978-0-306-40615-7"));
    }

    @Test
    void isbn13_Valid_WithoutHyphens() {
        assertTrue(IdentifierValidator.validateISBN13("9780306406157"));
    }

    @Test
    void isbn13_Valid_AnotherExample() {
        // The Lord of the Rings ISBN-13
        assertTrue(IdentifierValidator.validateISBN13("978-0-618-00222-1"));
    }

    @Test
    void isbn13_Valid_StartingWith979() {
        assertTrue(IdentifierValidator.validateISBN13("979-10-90636-07-1"));
    }

    @Test
    void isbn13_Invalid_WrongChecksum() {
        assertFalse(IdentifierValidator.validateISBN13("978-0-306-40615-8"));
        assertFalse(IdentifierValidator.validateISBN13("9780306406158"));
    }

    @Test
    void isbn13_Invalid_TooShort() {
        assertFalse(IdentifierValidator.validateISBN13("978030640615"));
    }

    @Test
    void isbn13_Invalid_TooLong() {
        assertFalse(IdentifierValidator.validateISBN13("97803064061579"));
    }

    @Test
    void isbn13_Invalid_ContainsLetters() {
        assertFalse(IdentifierValidator.validateISBN13("978030640A157"));
    }

    @Test
    void isbn13_Null() {
        assertFalse(IdentifierValidator.validateISBN13(null));
    }

    // ==================== ISSN Tests ====================

    @Test
    void issn_Valid_WithHyphen() {
        assertTrue(IdentifierValidator.validateISSN("0378-5955"));
    }

    @Test
    void issn_Valid_WithoutHyphen() {
        assertTrue(IdentifierValidator.validateISSN("03785955"));
    }

    @Test
    void issn_Valid_WithX() {
        // ISSN with check digit X (10): needs sum mod 11 = 1
        // Calculated example: 1234-567X
        // 8*1 + 7*2 + 6*3 + 5*4 + 4*5 + 3*6 + 2*7 = 8+14+18+20+20+18+14 = 112
        // 112 mod 11 = 2, check = 11-2 = 9, not X
        // For now, skip this test until we find a valid ISSN with X
        // assertTrue(IdentifierValidator.validateISSN("0028-084X"));
        // assertTrue(IdentifierValidator.validateISSN("0028084X"));

        // Test a different valid ISSN instead
        assertTrue(IdentifierValidator.validateISSN("1144-875X"));
    }

    @Test
    void issn_Valid_AnotherExample() {
        // Nature ISSN
        assertTrue(IdentifierValidator.validateISSN("0028-0836"));
    }

    @Test
    void issn_Invalid_WrongChecksum() {
        assertFalse(IdentifierValidator.validateISSN("0378-5956"));
    }

    @Test
    void issn_Invalid_TooShort() {
        assertFalse(IdentifierValidator.validateISSN("0378595"));
    }

    @Test
    void issn_Invalid_TooLong() {
        assertFalse(IdentifierValidator.validateISSN("037859559"));
    }

    @Test
    void issn_Invalid_ContainsLetters() {
        assertFalse(IdentifierValidator.validateISSN("0378A955"));
    }

    @Test
    void issn_Null() {
        assertFalse(IdentifierValidator.validateISSN(null));
    }

    // ==================== DOI Tests ====================

    @Test
    void doi_Valid_Simple() {
        assertTrue(IdentifierValidator.validateDOI("10.1234/example"));
    }

    @Test
    void doi_Valid_Complex() {
        assertTrue(IdentifierValidator.validateDOI("10.1038/nature12373"));
    }

    @Test
    void doi_Valid_WithSpecialChars() {
        assertTrue(IdentifierValidator.validateDOI("10.1000/xyz123"));
        assertTrue(IdentifierValidator.validateDOI("10.1016/j.cell.2024.01.001"));
    }

    @Test
    void doi_Valid_LongRegistrant() {
        assertTrue(IdentifierValidator.validateDOI("10.123456/example"));
    }

    @Test
    void doi_Invalid_MissingPrefix() {
        assertFalse(IdentifierValidator.validateDOI("1234/example"));
    }

    @Test
    void doi_Invalid_WrongPrefix() {
        assertFalse(IdentifierValidator.validateDOI("11.1234/example"));
    }

    @Test
    void doi_Invalid_NoSlash() {
        assertFalse(IdentifierValidator.validateDOI("10.1234"));
    }

    @Test
    void doi_Invalid_ShortRegistrant() {
        assertFalse(IdentifierValidator.validateDOI("10.123/example"));
    }

    @Test
    void doi_Invalid_EmptySuffix() {
        assertFalse(IdentifierValidator.validateDOI("10.1234/"));
    }

    @Test
    void doi_Null() {
        assertFalse(IdentifierValidator.validateDOI(null));
    }

    @Test
    void doi_Empty() {
        assertFalse(IdentifierValidator.validateDOI(""));
    }

    // ==================== Handle Tests ====================

    @Test
    void handle_Valid_Simple() {
        assertTrue(IdentifierValidator.validateHandle("20.500/example"));
    }

    @Test
    void handle_Valid_LongPrefix() {
        assertTrue(IdentifierValidator.validateHandle("20.500.12345/67890"));
    }

    @Test
    void handle_Valid_ComplexSuffix() {
        assertTrue(IdentifierValidator.validateHandle("1234/xyz-abc_123"));
    }

    @Test
    void handle_Invalid_NoSlash() {
        assertFalse(IdentifierValidator.validateHandle("20.500"));
    }

    @Test
    void handle_Invalid_EmptySuffix() {
        assertFalse(IdentifierValidator.validateHandle("20.500/"));
    }

    @Test
    void handle_Invalid_NonNumericPrefix() {
        assertFalse(IdentifierValidator.validateHandle("abc.def/example"));
    }

    @Test
    void handle_Null() {
        assertFalse(IdentifierValidator.validateHandle(null));
    }

    @Test
    void handle_Empty() {
        assertFalse(IdentifierValidator.validateHandle(""));
    }

    // ==================== URL Tests ====================

    @Test
    void url_Valid_HTTP() {
        assertTrue(IdentifierValidator.validateURL("http://example.com"));
    }

    @Test
    void url_Valid_HTTPS() {
        assertTrue(IdentifierValidator.validateURL("https://example.com"));
    }

    @Test
    void url_Valid_WithPath() {
        assertTrue(IdentifierValidator.validateURL("https://example.com/path/to/resource"));
    }

    @Test
    void url_Valid_WithQuery() {
        assertTrue(IdentifierValidator.validateURL("https://example.com/page?param=value"));
    }

    @Test
    void url_Valid_WithFragment() {
        assertTrue(IdentifierValidator.validateURL("https://example.com/page#section"));
    }

    @Test
    void url_Valid_WithPort() {
        assertTrue(IdentifierValidator.validateURL("https://example.com:8080/path"));
    }

    @Test
    void url_Valid_FTP() {
        assertTrue(IdentifierValidator.validateURL("ftp://ftp.example.com/file.txt"));
    }

    @Test
    void url_Invalid_NoProtocol() {
        assertFalse(IdentifierValidator.validateURL("example.com"));
    }

    @Test
    void url_Invalid_Spaces() {
        assertFalse(IdentifierValidator.validateURL("https://example .com"));
    }

    @Test
    void url_Invalid_MalformedProtocol() {
        assertFalse(IdentifierValidator.validateURL("ht!tp://example.com"));
    }

    @Test
    void url_Null() {
        assertFalse(IdentifierValidator.validateURL(null));
    }

    @Test
    void url_Empty() {
        assertFalse(IdentifierValidator.validateURL(""));
    }

    // ==================== Combined ISBN Tests ====================

    @Test
    void isbn_ValidatesEither10Or13() {
        assertTrue(IdentifierValidator.validateISBN("0-306-40615-2"));  // ISBN-10
        assertTrue(IdentifierValidator.validateISBN("978-0-306-40615-7")); // ISBN-13
    }

    @Test
    void isbn_Invalid_WrongLength() {
        assertFalse(IdentifierValidator.validateISBN("123456789012")); // Length 12
    }

    @Test
    void isbn_Classify_ISBN10() {
        assertEquals("ISBN-10", IdentifierValidator.classifyISBN("0-306-40615-2"));
    }

    @Test
    void isbn_Classify_ISBN13() {
        assertEquals("ISBN-13", IdentifierValidator.classifyISBN("978-0-306-40615-7"));
    }

    @Test
    void isbn_Classify_Invalid() {
        assertNull(IdentifierValidator.classifyISBN("0-306-40615-3")); // Wrong checksum
        assertNull(IdentifierValidator.classifyISBN("12345")); // Wrong length
    }

    @Test
    void isbn_Classify_Null() {
        assertNull(IdentifierValidator.classifyISBN(null));
    }
}
