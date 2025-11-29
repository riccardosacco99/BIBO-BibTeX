package it.riccardosacco.bibobibtex.converter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utility class for validating bibliographic identifiers.
 *
 * <p>Supports validation for:
 * <ul>
 *   <li>ISBN-10 with checksum validation</li>
 *   <li>ISBN-13 with checksum validation</li>
 *   <li>ISSN with checksum validation</li>
 *   <li>DOI format validation</li>
 *   <li>Handle format validation</li>
 *   <li>URL format validation</li>
 * </ul>
 *
 * @since 0.1.0
 */
public final class IdentifierValidator {

    // DOI pattern: 10.{4,}/.*
    private static final Pattern DOI_PATTERN = Pattern.compile("^10\\.\\d{4,}/[\\S]+$");

    // Handle pattern: prefix/suffix (e.g., 20.500.12345/67890)
    private static final Pattern HANDLE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)*/.+$");

    private IdentifierValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates an ISBN-10 using checksum algorithm.
     *
     * <p>ISBN-10 checksum formula:
     * <pre>
     * (10*d1 + 9*d2 + 8*d3 + 7*d4 + 6*d5 + 5*d6 + 4*d7 + 3*d8 + 2*d9 + d10) mod 11 = 0
     * </pre>
     * where d10 can be 'X' representing 10.
     *
     * @param isbn the ISBN-10 string (may contain hyphens)
     * @return true if valid, false otherwise
     */
    public static boolean validateISBN10(String isbn) {
        if (isbn == null) {
            return false;
        }

        // Remove hyphens and spaces
        String cleaned = isbn.replaceAll("[^0-9X]", "");

        if (cleaned.length() != 10) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char ch = cleaned.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
            sum += (10 - i) * Character.getNumericValue(ch);
        }

        // Last character can be 'X' (representing 10) or a digit
        char checkChar = cleaned.charAt(9);
        int checkDigit;
        if (checkChar == 'X') {
            checkDigit = 10;
        } else if (Character.isDigit(checkChar)) {
            checkDigit = Character.getNumericValue(checkChar);
        } else {
            return false;
        }

        sum += checkDigit;

        return (sum % 11) == 0;
    }

    /**
     * Validates an ISBN-13 using checksum algorithm.
     *
     * <p>ISBN-13 checksum formula:
     * <pre>
     * (d1 + 3*d2 + d3 + 3*d4 + d5 + 3*d6 + d7 + 3*d8 + d9 + 3*d10 + d11 + 3*d12 + d13) mod 10 = 0
     * </pre>
     *
     * @param isbn the ISBN-13 string (may contain hyphens)
     * @return true if valid, false otherwise
     */
    public static boolean validateISBN13(String isbn) {
        if (isbn == null) {
            return false;
        }

        // Remove hyphens and spaces
        String cleaned = isbn.replaceAll("[^0-9]", "");

        if (cleaned.length() != 13) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            char ch = cleaned.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
            int digit = Character.getNumericValue(ch);
            // Alternate between 1 and 3 multiplier
            sum += (i % 2 == 0) ? digit : digit * 3;
        }

        // Check digit
        char checkChar = cleaned.charAt(12);
        if (!Character.isDigit(checkChar)) {
            return false;
        }
        int checkDigit = Character.getNumericValue(checkChar);
        int calculatedCheck = (10 - (sum % 10)) % 10;

        return checkDigit == calculatedCheck;
    }

    /**
     * Validates an ISSN using checksum algorithm.
     *
     * <p>ISSN checksum formula:
     * <pre>
     * (8*d1 + 7*d2 + 6*d3 + 5*d4 + 4*d5 + 3*d6 + 2*d7 + d8) mod 11 = 0
     * </pre>
     * where d8 can be 'X' representing 10.
     *
     * @param issn the ISSN string (may contain hyphens)
     * @return true if valid, false otherwise
     */
    public static boolean validateISSN(String issn) {
        if (issn == null) {
            return false;
        }

        // Remove hyphens and spaces
        String cleaned = issn.replaceAll("[^0-9X]", "");

        if (cleaned.length() != 8) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 7; i++) {
            char ch = cleaned.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
            sum += (8 - i) * Character.getNumericValue(ch);
        }

        // Last character can be 'X' (representing 10) or a digit
        char checkChar = cleaned.charAt(7);
        int checkDigit;
        if (checkChar == 'X') {
            checkDigit = 10;
        } else if (Character.isDigit(checkChar)) {
            checkDigit = Character.getNumericValue(checkChar);
        } else {
            return false;
        }

        sum += checkDigit;

        return (sum % 11) == 0;
    }

    /**
     * Validates a DOI (Digital Object Identifier) format.
     *
     * <p>DOI format: 10.{registrant}/{suffix}
     * <p>Example: 10.1234/example.doi
     *
     * @param doi the DOI string
     * @return true if format is valid, false otherwise
     */
    public static boolean validateDOI(String doi) {
        if (doi == null || doi.isEmpty()) {
            return false;
        }
        return DOI_PATTERN.matcher(doi.trim()).matches();
    }

    /**
     * Validates a Handle identifier format.
     *
     * <p>Handle format: {prefix}/{suffix}
     * <p>Example: 20.500.12345/67890
     *
     * @param handle the Handle string
     * @return true if format is valid, false otherwise
     */
    public static boolean validateHandle(String handle) {
        if (handle == null || handle.isEmpty()) {
            return false;
        }
        return HANDLE_PATTERN.matcher(handle.trim()).matches();
    }

    /**
     * Validates a URL format using Java's URL class.
     *
     * @param urlString the URL string
     * @return true if format is valid, false otherwise
     */
    public static boolean validateURL(String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }
        try {
            URI.create(urlString.trim()).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates either ISBN-10 or ISBN-13.
     *
     * @param isbn the ISBN string
     * @return true if valid as either ISBN-10 or ISBN-13, false otherwise
     */
    public static boolean validateISBN(String isbn) {
        if (isbn == null) {
            return false;
        }
        String cleaned = isbn.replaceAll("[^0-9X]", "");
        if (cleaned.length() == 10) {
            return validateISBN10(isbn);
        } else if (cleaned.length() == 13) {
            return validateISBN13(isbn);
        }
        return false;
    }

    /**
     * Determines the type of ISBN (10 or 13).
     *
     * @param isbn the ISBN string
     * @return "ISBN-10", "ISBN-13", or null if invalid
     */
    public static String classifyISBN(String isbn) {
        if (isbn == null) {
            return null;
        }
        String cleaned = isbn.replaceAll("[^0-9X]", "");
        if (cleaned.length() == 10 && validateISBN10(isbn)) {
            return "ISBN-10";
        } else if (cleaned.length() == 13 && validateISBN13(isbn)) {
            return "ISBN-13";
        }
        return null;
    }
}
