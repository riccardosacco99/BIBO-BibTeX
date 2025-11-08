package it.riccardosacco.bibobibtex.converter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Validator for bibliographic identifiers (ISBN, ISSN, DOI, Handle, URL).
 * Implements checksum validation for ISBN-10, ISBN-13, and ISSN.
 * Sprint 02 - US-11: Identifier Validation Library
 */
public final class IdentifierValidator {

    private static final Pattern DOI_PATTERN = Pattern.compile("^10\\.\\d{4,9}/[-._;()/:A-Za-z0-9]+$");
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[\\d.]+/[A-Za-z0-9._-]+$");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    private IdentifierValidator() {
        // Utility class
    }

    /**
     * Validates an ISBN-10 identifier using checksum validation.
     *
     * @param isbn the ISBN-10 string (10 digits, may contain hyphens)
     * @return true if valid, false otherwise
     */
    public static boolean validateISBN10(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        // Remove hyphens and spaces
        String clean = isbn.replaceAll("[- ]", "");

        // Must be exactly 10 characters
        if (clean.length() != 10) {
            return false;
        }

        // First 9 must be digits, last can be digit or 'X'
        if (!clean.substring(0, 9).matches("\\d{9}")) {
            return false;
        }
        char lastChar = clean.charAt(9);
        if (!Character.isDigit(lastChar) && lastChar != 'X' && lastChar != 'x') {
            return false;
        }

        // Calculate checksum
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (10 - i) * Character.getNumericValue(clean.charAt(i));
        }
        // Add check digit
        int checkDigit = (lastChar == 'X' || lastChar == 'x') ? 10 : Character.getNumericValue(lastChar);
        sum += checkDigit;

        // Valid if divisible by 11
        return sum % 11 == 0;
    }

    /**
     * Validates an ISBN-13 identifier using checksum validation.
     *
     * @param isbn the ISBN-13 string (13 digits, may contain hyphens)
     * @return true if valid, false otherwise
     */
    public static boolean validateISBN13(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }

        // Remove hyphens and spaces
        String clean = isbn.replaceAll("[- ]", "");

        // Must be exactly 13 digits
        if (!clean.matches("\\d{13}")) {
            return false;
        }

        // Calculate checksum (alternating 1 and 3 multiplier)
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(clean.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        int checkDigit = Character.getNumericValue(clean.charAt(12));
        int calculatedCheck = (10 - (sum % 10)) % 10;

        return checkDigit == calculatedCheck;
    }

    /**
     * Validates an ISSN identifier using checksum validation.
     *
     * @param issn the ISSN string (8 digits, may contain hyphen)
     * @return true if valid, false otherwise
     */
    public static boolean validateISSN(String issn) {
        if (issn == null || issn.isBlank()) {
            return false;
        }

        // Remove hyphens and spaces
        String clean = issn.replaceAll("[- ]", "");

        // Must be exactly 8 characters
        if (clean.length() != 8) {
            return false;
        }

        // First 7 must be digits, last can be digit or 'X'
        if (!clean.substring(0, 7).matches("\\d{7}")) {
            return false;
        }
        char lastChar = clean.charAt(7);
        if (!Character.isDigit(lastChar) && lastChar != 'X' && lastChar != 'x') {
            return false;
        }

        // Calculate checksum
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            sum += (8 - i) * Character.getNumericValue(clean.charAt(i));
        }
        // Add check digit
        int checkDigit = (lastChar == 'X' || lastChar == 'x') ? 10 : Character.getNumericValue(lastChar);
        sum += checkDigit;

        // Valid if divisible by 11
        return sum % 11 == 0;
    }

    /**
     * Validates a DOI (Digital Object Identifier) format.
     * DOI format: 10.prefix/suffix
     *
     * @param doi the DOI string
     * @return true if valid format, false otherwise
     */
    public static boolean validateDOI(String doi) {
        if (doi == null || doi.isBlank()) {
            return false;
        }

        String clean = doi.strip();

        // Remove common prefixes if present
        if (clean.toLowerCase().startsWith("doi:")) {
            clean = clean.substring(4).strip();
        }
        if (clean.toLowerCase().startsWith("https://doi.org/")) {
            clean = clean.substring(16);
        }
        if (clean.toLowerCase().startsWith("http://dx.doi.org/")) {
            clean = clean.substring(18);
        }

        return DOI_PATTERN.matcher(clean).matches();
    }

    /**
     * Validates a Handle identifier format.
     * Handle format: prefix/suffix (e.g., 20.500.12345/abcd-1234)
     *
     * @param handle the Handle string
     * @return true if valid format, false otherwise
     */
    public static boolean validateHandle(String handle) {
        if (handle == null || handle.isBlank()) {
            return false;
        }

        String clean = handle.strip();

        // Remove common prefixes if present
        if (clean.toLowerCase().startsWith("hdl:")) {
            clean = clean.substring(4).strip();
        }
        if (clean.toLowerCase().startsWith("https://hdl.handle.net/")) {
            clean = clean.substring(23);
        }
        if (clean.toLowerCase().startsWith("http://hdl.handle.net/")) {
            clean = clean.substring(22);
        }

        return HANDLE_PATTERN.matcher(clean).matches();
    }

    /**
     * Validates a URL format.
     *
     * @param url the URL string
     * @return true if valid URL, false otherwise
     */
    public static boolean validateURL(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            new URL(url.strip());
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validates a URI format (more permissive than URL).
     *
     * @param uri the URI string
     * @return true if valid URI format, false otherwise
     */
    public static boolean validateURI(String uri) {
        if (uri == null || uri.isBlank()) {
            return false;
        }

        String clean = uri.strip();

        // Basic URI validation: must contain a colon for scheme
        if (!clean.contains(":")) {
            return false;
        }

        // Check scheme part is valid (alphanumeric + some special chars)
        int colonIndex = clean.indexOf(':');
        if (colonIndex == 0) {
            return false;
        }

        String scheme = clean.substring(0, colonIndex);
        return scheme.matches("[a-zA-Z][a-zA-Z0-9+.-]*");
    }

    /**
     * Checks if a string contains only digits (no hyphens, spaces, or other characters).
     *
     * @param value the string to check
     * @return true if contains only digits, false otherwise
     */
    public static boolean isDigitsOnly(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return DIGITS_ONLY.matcher(value).matches();
    }

    /**
     * Strips common prefixes from identifiers (doi:, http://, https://, etc.).
     *
     * @param identifier the identifier string
     * @return the identifier with common prefixes removed
     */
    public static String stripPrefix(String identifier) {
        if (identifier == null) {
            return null;
        }

        String clean = identifier.strip();

        // DOI prefixes
        if (clean.toLowerCase().startsWith("doi:")) {
            return clean.substring(4).strip();
        }
        if (clean.toLowerCase().startsWith("https://doi.org/")) {
            return clean.substring(16);
        }
        if (clean.toLowerCase().startsWith("http://dx.doi.org/")) {
            return clean.substring(18);
        }

        // Handle prefixes
        if (clean.toLowerCase().startsWith("hdl:")) {
            return clean.substring(4).strip();
        }
        if (clean.toLowerCase().startsWith("https://hdl.handle.net/")) {
            return clean.substring(23);
        }
        if (clean.toLowerCase().startsWith("http://hdl.handle.net/")) {
            return clean.substring(22);
        }

        // URN prefix
        if (clean.toLowerCase().startsWith("urn:isbn:")) {
            return clean.substring(9);
        }
        if (clean.toLowerCase().startsWith("urn:issn:")) {
            return clean.substring(9);
        }

        return clean;
    }
}
