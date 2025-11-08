package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for identifier validation (Sprint 02 - US-11).
 */
class IdentifierValidatorTest {

    // ========== ISBN-10 Tests ==========

    @Test
    void testValidISBN10() {
        assertTrue(IdentifierValidator.validateISBN10("0-306-40615-2"));
        assertTrue(IdentifierValidator.validateISBN10("0306406152"));
        assertTrue(IdentifierValidator.validateISBN10("0 306 40615 2"));
    }

    @Test
    void testValidISBN10WithX() {
        assertTrue(IdentifierValidator.validateISBN10("043942089X"));
        assertTrue(IdentifierValidator.validateISBN10("0-439-42089-X"));
        assertTrue(IdentifierValidator.validateISBN10("0-439-42089-x")); // lowercase x
    }

    @Test
    void testInvalidISBN10Checksum() {
        assertFalse(IdentifierValidator.validateISBN10("0-306-40615-3")); // Wrong checksum
        assertFalse(IdentifierValidator.validateISBN10("0306406153"));
    }

    @Test
    void testInvalidISBN10Length() {
        assertFalse(IdentifierValidator.validateISBN10("123456789")); // Too short
        assertFalse(IdentifierValidator.validateISBN10("12345678901")); // Too long
    }

    @Test
    void testInvalidISBN10Format() {
        assertFalse(IdentifierValidator.validateISBN10("abc-def-ghij-k"));
        assertFalse(IdentifierValidator.validateISBN10(""));
        assertFalse(IdentifierValidator.validateISBN10(null));
        assertFalse(IdentifierValidator.validateISBN10("   "));
    }

    // ========== ISBN-13 Tests ==========

    @Test
    void testValidISBN13() {
        assertTrue(IdentifierValidator.validateISBN13("978-0-306-40615-7"));
        assertTrue(IdentifierValidator.validateISBN13("9780306406157"));
        assertTrue(IdentifierValidator.validateISBN13("978 0 306 40615 7"));
    }

    @Test
    void testValidISBN13Examples() {
        assertTrue(IdentifierValidator.validateISBN13("978-3-16-148410-0"));
        assertTrue(IdentifierValidator.validateISBN13("978-1-86197-876-9"));
    }

    @Test
    void testInvalidISBN13Checksum() {
        assertFalse(IdentifierValidator.validateISBN13("978-0-306-40615-8")); // Wrong checksum
        assertFalse(IdentifierValidator.validateISBN13("9780306406158"));
    }

    @Test
    void testInvalidISBN13Length() {
        assertFalse(IdentifierValidator.validateISBN13("978030640615")); // Too short
        assertFalse(IdentifierValidator.validateISBN13("97803064061577")); // Too long
    }

    @Test
    void testInvalidISBN13Format() {
        assertFalse(IdentifierValidator.validateISBN13("978-abc-def-ghi-j"));
        assertFalse(IdentifierValidator.validateISBN13(""));
        assertFalse(IdentifierValidator.validateISBN13(null));
    }

    // ========== ISSN Tests ==========

    @Test
    void testValidISSN() {
        assertTrue(IdentifierValidator.validateISSN("0378-5955"));
        assertTrue(IdentifierValidator.validateISSN("03785955"));
        assertTrue(IdentifierValidator.validateISSN("0378 5955"));
    }

    @Test
    void testValidISSNWithX() {
        assertTrue(IdentifierValidator.validateISSN("0028-078X"));
        assertTrue(IdentifierValidator.validateISSN("0028078X"));
        assertTrue(IdentifierValidator.validateISSN("0028-078x")); // lowercase x
    }

    @Test
    void testInvalidISSNChecksum() {
        assertFalse(IdentifierValidator.validateISSN("0378-5956")); // Wrong checksum
        assertFalse(IdentifierValidator.validateISSN("03785956"));
    }

    @Test
    void testInvalidISSNLength() {
        assertFalse(IdentifierValidator.validateISSN("0378595")); // Too short
        assertFalse(IdentifierValidator.validateISSN("037859556")); // Too long
    }

    @Test
    void testInvalidISSNFormat() {
        assertFalse(IdentifierValidator.validateISSN("abcd-efgh"));
        assertFalse(IdentifierValidator.validateISSN(""));
        assertFalse(IdentifierValidator.validateISSN(null));
    }

    // ========== DOI Tests ==========

    @Test
    void testValidDOI() {
        assertTrue(IdentifierValidator.validateDOI("10.1000/xyz123"));
        assertTrue(IdentifierValidator.validateDOI("10.1234/abcd.efgh-5678"));
        assertTrue(IdentifierValidator.validateDOI("10.1016/j.cell.2019.11.025"));
    }

    @Test
    void testValidDOIWithPrefix() {
        assertTrue(IdentifierValidator.validateDOI("doi:10.1000/xyz123"));
        assertTrue(IdentifierValidator.validateDOI("DOI:10.1000/xyz123"));
        assertTrue(IdentifierValidator.validateDOI("https://doi.org/10.1000/xyz123"));
        assertTrue(IdentifierValidator.validateDOI("http://dx.doi.org/10.1000/xyz123"));
    }

    @Test
    void testValidDOIWithSpecialCharacters() {
        assertTrue(IdentifierValidator.validateDOI("10.1000/test_123"));
        assertTrue(IdentifierValidator.validateDOI("10.1000/test-123"));
        assertTrue(IdentifierValidator.validateDOI("10.1000/test.123"));
        assertTrue(IdentifierValidator.validateDOI("10.1000/test(123)"));
        assertTrue(IdentifierValidator.validateDOI("10.1000/test:123"));
    }

    @Test
    void testInvalidDOI() {
        assertFalse(IdentifierValidator.validateDOI("11.1000/xyz123")); // Wrong prefix
        assertFalse(IdentifierValidator.validateDOI("10.1000")); // Missing suffix
        assertFalse(IdentifierValidator.validateDOI("xyz123")); // Not a DOI
        assertFalse(IdentifierValidator.validateDOI(""));
        assertFalse(IdentifierValidator.validateDOI(null));
    }

    // ========== Handle Tests ==========

    @Test
    void testValidHandle() {
        assertTrue(IdentifierValidator.validateHandle("20.500.12345/abcd-1234"));
        assertTrue(IdentifierValidator.validateHandle("1234/5678"));
        assertTrue(IdentifierValidator.validateHandle("10.1234/test_handle"));
    }

    @Test
    void testValidHandleWithPrefix() {
        assertTrue(IdentifierValidator.validateHandle("hdl:20.500.12345/abcd"));
        assertTrue(IdentifierValidator.validateHandle("HDL:20.500.12345/abcd"));
        assertTrue(IdentifierValidator.validateHandle("https://hdl.handle.net/20.500.12345/abcd"));
        assertTrue(IdentifierValidator.validateHandle("http://hdl.handle.net/20.500.12345/abcd"));
    }

    @Test
    void testInvalidHandle() {
        assertFalse(IdentifierValidator.validateHandle("notahandle"));
        assertFalse(IdentifierValidator.validateHandle("abc/def")); // Prefix must be numeric
        assertFalse(IdentifierValidator.validateHandle(""));
        assertFalse(IdentifierValidator.validateHandle(null));
    }

    // ========== URL Tests ==========

    @Test
    void testValidURL() {
        assertTrue(IdentifierValidator.validateURL("https://www.example.com"));
        assertTrue(IdentifierValidator.validateURL("http://example.com/path/to/page"));
        assertTrue(IdentifierValidator.validateURL("ftp://ftp.example.com/file.txt"));
        assertTrue(IdentifierValidator.validateURL("https://example.com:8080/path?query=value"));
    }

    @Test
    void testInvalidURL() {
        assertFalse(IdentifierValidator.validateURL("not a url"));
        assertFalse(IdentifierValidator.validateURL("htp://broken.com")); // Invalid scheme
        assertFalse(IdentifierValidator.validateURL(""));
        assertFalse(IdentifierValidator.validateURL(null));
        assertFalse(IdentifierValidator.validateURL("   "));
    }

    // ========== URI Tests ==========

    @Test
    void testValidURI() {
        assertTrue(IdentifierValidator.validateURI("urn:isbn:0451450523"));
        assertTrue(IdentifierValidator.validateURI("urn:issn:0028-0836"));
        assertTrue(IdentifierValidator.validateURI("http://example.com"));
        assertTrue(IdentifierValidator.validateURI("mailto:test@example.com"));
        assertTrue(IdentifierValidator.validateURI("custom-scheme:value"));
    }

    @Test
    void testInvalidURI() {
        assertFalse(IdentifierValidator.validateURI("no-colon"));
        assertFalse(IdentifierValidator.validateURI(":no-scheme"));
        assertFalse(IdentifierValidator.validateURI("123:invalid-scheme")); // Scheme must start with letter
        assertFalse(IdentifierValidator.validateURI(""));
        assertFalse(IdentifierValidator.validateURI(null));
    }

    // ========== Utility Tests ==========

    @Test
    void testIsDigitsOnly() {
        assertTrue(IdentifierValidator.isDigitsOnly("123456789"));
        assertTrue(IdentifierValidator.isDigitsOnly("0"));
        assertFalse(IdentifierValidator.isDigitsOnly("123-456"));
        assertFalse(IdentifierValidator.isDigitsOnly("123 456"));
        assertFalse(IdentifierValidator.isDigitsOnly("abc123"));
        assertFalse(IdentifierValidator.isDigitsOnly(""));
        assertFalse(IdentifierValidator.isDigitsOnly(null));
    }

    @Test
    void testStripPrefixDOI() {
        assertEquals("10.1000/xyz", IdentifierValidator.stripPrefix("doi:10.1000/xyz"));
        assertEquals("10.1000/xyz", IdentifierValidator.stripPrefix("DOI:10.1000/xyz"));
        assertEquals("10.1000/xyz", IdentifierValidator.stripPrefix("https://doi.org/10.1000/xyz"));
        assertEquals("10.1000/xyz", IdentifierValidator.stripPrefix("http://dx.doi.org/10.1000/xyz"));
        assertEquals("10.1000/xyz", IdentifierValidator.stripPrefix("10.1000/xyz")); // No prefix
    }

    @Test
    void testStripPrefixHandle() {
        assertEquals("20.500/xyz", IdentifierValidator.stripPrefix("hdl:20.500/xyz"));
        assertEquals("20.500/xyz", IdentifierValidator.stripPrefix("https://hdl.handle.net/20.500/xyz"));
        assertEquals("20.500/xyz", IdentifierValidator.stripPrefix("http://hdl.handle.net/20.500/xyz"));
    }

    @Test
    void testStripPrefixURN() {
        assertEquals("0451450523", IdentifierValidator.stripPrefix("urn:isbn:0451450523"));
        assertEquals("0028-0836", IdentifierValidator.stripPrefix("urn:issn:0028-0836"));
    }

    @Test
    void testStripPrefixNull() {
        assertNull(IdentifierValidator.stripPrefix(null));
    }
}
