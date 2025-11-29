package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.exception.DateException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for validating and parsing dates in bibliographic records.
 *
 * <p>This validator supports:
 * <ul>
 *   <li>Month validation (1-12)</li>
 *   <li>Day validation based on month (28/29/30/31)</li>
 *   <li>Leap year handling for February 29</li>
 *   <li>Historical dates (year < 1000)</li>
 *   <li>Future date warnings (year > current year + 5)</li>
 *   <li>Circa dates (e.g., "circa 1850", "c. 1900", "~1750")</li>
 *   <li>Multiple date format parsing</li>
 * </ul>
 *
 * @since 0.1.0
 */
public final class DateValidator {
    private static final Logger logger = LoggerFactory.getLogger(DateValidator.class);

    private static final int[] DAYS_PER_MONTH = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE,              // YYYY-MM-DD
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),     // MM/DD/YYYY
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),    // 15-Jun-2024
        DateTimeFormatter.ofPattern("MMMM d, yyyy"),   // June 15, 2024
        DateTimeFormatter.ofPattern("d MMMM yyyy"),    // 15 June 2024
        DateTimeFormatter.ofPattern("yyyy/MM/dd")      // YYYY/MM/DD
    };

    private static final Pattern[] YEAR_PATTERNS = {
        Pattern.compile("\\b(\\d{4})\\b"),                    // YYYY
        Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})"),        // YYYY-MM-DD
        Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})"),        // MM/DD/YYYY
        Pattern.compile("(?:circa|c\\.|~)\\s*(\\d{4})", Pattern.CASE_INSENSITIVE),  // circa YYYY
    };

    private DateValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates if a date is valid considering month days and leap years.
     *
     * @param year the year (any integer value allowed)
     * @param month the month (1-12)
     * @param day the day of month
     * @return true if the date is valid, false otherwise
     */
    public static boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1) {
            return false;
        }

        int maxDay = DAYS_PER_MONTH[month - 1];
        if (month == 2 && isLeapYear(year)) {
            maxDay = 29;
        }

        return day <= maxDay;
    }

    /**
     * Determines if a year is a leap year according to Gregorian calendar rules.
     *
     * <p>A year is a leap year if:
     * <ul>
     *   <li>It is divisible by 4 AND not divisible by 100, OR</li>
     *   <li>It is divisible by 400</li>
     * </ul>
     *
     * @param year the year to check
     * @return true if the year is a leap year, false otherwise
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Validates a date and throws an exception if invalid.
     *
     * <p>Also logs a warning if the date is more than 5 years in the future,
     * but does not reject it.
     *
     * @param year the year
     * @param month the month (1-12)
     * @param day the day of month
     * @throws DateException if the date is invalid
     */
    public static void validateOrThrow(int year, int month, int day) {
        if (!isValidDate(year, month, day)) {
            throw new DateException(
                    "Invalid date: %04d-%02d-%02d".formatted(year, month, day)
            );
        }

        // Warning for future dates (but allow them)
        int currentYear = LocalDate.now().getYear();
        if (year > currentYear + 5) {
            logger.warn("Future date detected: {} (allowed but unusual)", year);
        }
    }

    /**
     * Extracts a year from a free-form date string.
     *
     * <p>Tries multiple patterns including:
     * <ul>
     *   <li>Four-digit year (YYYY)</li>
     *   <li>ISO date format (YYYY-MM-DD)</li>
     *   <li>US date format (MM/DD/YYYY)</li>
     *   <li>Circa dates (circa YYYY, c. YYYY, ~YYYY)</li>
     * </ul>
     *
     * @param dateString the date string to parse
     * @return the extracted year, or null if no year could be found
     */
    public static Integer extractYearFromFreeForm(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // Check for circa dates
        boolean isCirca = dateString.toLowerCase().contains("circa") ||
                dateString.contains("c.") ||
                dateString.contains("~");

        // Try patterns in order
        for (Pattern pattern : YEAR_PATTERNS) {
            Matcher matcher = pattern.matcher(dateString);
            if (matcher.find()) {
                String yearStr = matcher.group(1);
                try {
                    int year = Integer.parseInt(yearStr);

                    if (isCirca) {
                        logger.info("Circa date detected: {}, extracted year: {}", dateString, year);
                    }

                    return year;
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse year from: {}", yearStr);
                }
            }
        }

        return null;
    }

    /**
     * Parses a date string using multiple format attempts.
     *
     * <p>Tries various common date formats. If none match, falls back to
     * extracting just the year and returning January 1 of that year.
     *
     * @param dateString the date string to parse
     * @return the parsed LocalDate
     * @throws DateException if the date cannot be parsed at all
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new DateException("Date string is null or empty");
        }

        String trimmed = dateString.trim();

        // Try all formatters
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        // Fallback: extract year only and create January 1
        Integer year = extractYearFromFreeForm(trimmed);
        if (year != null) {
            try {
                return LocalDate.of(year, 1, 1);
            } catch (DateTimeException e) {
                throw new DateException("Invalid year extracted: " + year, e);
            }
        }

        throw new DateException("Unable to parse date: " + dateString);
    }

    /**
     * Validates a date and throws if future date (for strict validation).
     *
     * @param year the year
     * @param month the month
     * @param day the day
     * @param allowFuture if false, throws on future dates; if true, only warns
     * @throws DateException if validation fails
     */
    public static void validateWithFutureCheck(int year, int month, int day, boolean allowFuture) {
        validateOrThrow(year, month, day);

        if (!allowFuture) {
            int currentYear = LocalDate.now().getYear();
            if (year > currentYear) {
                throw new DateException(
                        "Future date not allowed: %04d-%02d-%02d".formatted(year, month, day)
                );
            }
        }
    }
}
