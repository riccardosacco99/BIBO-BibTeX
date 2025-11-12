package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.exception.ValidationException;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import org.jbibtex.BibTeXEntry;

import java.net.URI;
import java.time.Year;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates bibliographic entries and documents.
 *
 * Provides validation for BibTeX entries, BIBO documents, and identifiers (ISBN, DOI, URL).
 */
public class BibliographicValidator {

    private static final Pattern DOI_PATTERN = Pattern.compile("10\\.\\d{4,}/[^\\s]+");
    // Allow alphanumeric, underscore, hyphen, slash, colon, plus (common in DBLP, arXiv, BibTeX databases)
    private static final Pattern CITATION_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-/:+.]+$");
    private static final int MIN_VALID_YEAR = 1000;
    private static final int MAX_FUTURE_YEARS = 5;

    /**
     * Validates BibTeX entry.
     *
     * @param entry BibTeX entry to validate
     * @throws ValidationException if validation fails
     */
    public static void validateBibTeXEntry(BibTeXEntry entry) {
        if (entry == null) {
            throw new ValidationException("BibTeX entry cannot be null");
        }

        // Validate type
        if (entry.getType() == null) {
            throw new ValidationException("BibTeX entry type is required");
        }

        // Validate title
        String title = getFieldValue(entry, BibTeXEntry.KEY_TITLE);
        if (title == null || title.isBlank()) {
            throw new ValidationException("Title is required", "title", title);
        }

        // Validate citation key format (allow common formats: DBLP, arXiv, etc.)
        String citationKey = entry.getKey() != null ? entry.getKey().getValue() : null;
        if (citationKey != null && !citationKey.isBlank()) {
            if (!CITATION_KEY_PATTERN.matcher(citationKey).matches()) {
                throw new ValidationException(
                    "Citation key contains invalid characters (allowed: alphanumeric, -_/:+.)",
                    "citation_key",
                    citationKey
                );
            }
        }
    }

    /**
     * Validates BIBO document.
     *
     * @param document BIBO document to validate
     * @throws ValidationException if validation fails
     */
    public static void validateBiboDocument(BiboDocument document) {
        if (document == null) {
            throw new ValidationException("BIBO document cannot be null");
        }

        // Validate type
        if (document.type() == null) {
            throw new ValidationException("Document type is required");
        }

        // Validate title
        if (document.title() == null || document.title().isBlank()) {
            throw new ValidationException("Title is required", "title", document.title());
        }

        // Validate publication date
        if (document.publicationDate().isPresent()) {
            validatePublicationDate(document.publicationDate().get());
        }

        // Validate identifiers
        for (BiboIdentifier identifier : document.identifiers()) {
            validateIdentifier(identifier);
        }
    }

    /**
     * Validates publication date ranges.
     *
     * @param date publication date to validate
     * @throws ValidationException if date is out of valid range
     */
    private static void validatePublicationDate(BiboPublicationDate date) {
        if (date.year() < MIN_VALID_YEAR) {
            throw new ValidationException(
                "Year must be >= " + MIN_VALID_YEAR,
                "year",
                date.year()
            );
        }

        int currentYear = Year.now().getValue();
        int maxYear = currentYear + MAX_FUTURE_YEARS;
        if (date.year() > maxYear) {
            throw new ValidationException(
                "Year cannot be more than " + MAX_FUTURE_YEARS + " years in the future",
                "year",
                date.year()
            );
        }

        // Validate month
        if (date.month().isPresent()) {
            int month = date.month().get();
            if (month < 1 || month > 12) {
                throw new ValidationException(
                    "Month must be between 1 and 12",
                    "month",
                    month
                );
            }
        }

        // Validate day
        if (date.day().isPresent()) {
            int day = date.day().get();
            if (day < 1 || day > 31) {
                throw new ValidationException(
                    "Day must be between 1 and 31",
                    "day",
                    day
                );
            }

            // Check Feb 29 in non-leap years
            if (date.month().isPresent()) {
                int month = date.month().get();
                if (month == 2 && day == 29) {
                    if (!Year.isLeap(date.year())) {
                        throw new ValidationException(
                            "February 29 is invalid for non-leap year " + date.year(),
                            "day",
                            day
                        );
                    }
                }
            }
        }
    }

    /**
     * Validates identifier based on type.
     *
     * @param identifier identifier to validate
     * @throws ValidationException if identifier is invalid
     */
    public static void validateIdentifier(BiboIdentifier identifier) {
        if (identifier == null) {
            return;
        }

        String value = identifier.value();
        if (value == null || value.isBlank()) {
            throw new ValidationException("Identifier value cannot be empty");
        }

        switch (identifier.type()) {
            case ISBN_10, ISBN_13 -> validateISBN(value);
            case DOI -> validateDOI(value);
            case URL -> validateURL(value);
            default -> {
                // Other identifier types don't require specific validation
            }
        }
    }

    /**
     * Validates ISBN-10 or ISBN-13 checksum.
     *
     * @param isbn ISBN string to validate
     * @throws ValidationException if ISBN is invalid
     */
    private static void validateISBN(String isbn) {
        // Remove hyphens and spaces
        String cleanISBN = isbn.replaceAll("[\\s-]", "");

        if (cleanISBN.length() == 10) {
            validateISBN10(cleanISBN);
        } else if (cleanISBN.length() == 13) {
            validateISBN13(cleanISBN);
        } else {
            throw new ValidationException(
                "ISBN must be 10 or 13 digits",
                "isbn",
                isbn
            );
        }
    }

    private static void validateISBN10(String isbn) {
        if (!isbn.matches("\\d{9}[\\dXx]")) {
            throw new ValidationException(
                "Invalid ISBN-10 format",
                "isbn",
                isbn
            );
        }

        int checksum = 0;
        for (int i = 0; i < 9; i++) {
            checksum += (isbn.charAt(i) - '0') * (10 - i);
        }

        char lastChar = isbn.charAt(9);
        int lastDigit = (lastChar == 'X' || lastChar == 'x') ? 10 : (lastChar - '0');
        checksum += lastDigit;

        if (checksum % 11 != 0) {
            throw new ValidationException(
                "Invalid ISBN-10 checksum",
                "isbn",
                isbn
            );
        }
    }

    private static void validateISBN13(String isbn) {
        if (!isbn.matches("\\d{13}")) {
            throw new ValidationException(
                "Invalid ISBN-13 format",
                "isbn",
                isbn
            );
        }

        int checksum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            checksum += (i % 2 == 0) ? digit : digit * 3;
        }

        int checksumDigit = (10 - (checksum % 10)) % 10;
        int actualDigit = isbn.charAt(12) - '0';

        if (checksumDigit != actualDigit) {
            throw new ValidationException(
                "Invalid ISBN-13 checksum",
                "isbn",
                isbn
            );
        }
    }

    /**
     * Validates DOI format.
     *
     * @param doi DOI string to validate
     * @throws ValidationException if DOI is invalid
     */
    private static void validateDOI(String doi) {
        if (!DOI_PATTERN.matcher(doi.trim()).find()) {
            throw new ValidationException(
                "Invalid DOI format. Expected pattern: 10.xxxx/xxxxx",
                "doi",
                doi
            );
        }

        if (doi.contains(" ")) {
            throw new ValidationException(
                "DOI cannot contain whitespace",
                "doi",
                doi
            );
        }
    }

    /**
     * Validates URL format.
     *
     * @param url URL string to validate
     * @throws ValidationException if URL is invalid
     */
    private static void validateURL(String url) {
        if (url == null || url.isBlank()) {
            throw new ValidationException("URL cannot be null or blank", "url", url);
        }

        String normalized = url.trim();

        // Use URI instead of deprecated URL(String) constructor
        try {
            URI.create(normalized).toURL();
        } catch (Exception e) {
            throw new ValidationException(
                "Invalid URL format",
                "url",
                normalized
            );
        }

        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (!lowerCase.startsWith("http://") && !lowerCase.startsWith("https://")) {
            throw new ValidationException(
                "URL must start with http:// or https://",
                "url",
                normalized
            );
        }
    }

    /**
     * Helper to extract field value from BibTeX entry.
     */
    private static String getFieldValue(BibTeXEntry entry, org.jbibtex.Key key) {
        if (entry.getField(key) == null) {
            return null;
        }
        return entry.getField(key).toUserString();
    }
}
