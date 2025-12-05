package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a page range with start and end page numbers.
 *
 * <p>Supports parsing various page formats:
 * <ul>
 *   <li>"123-145" → start=123, end=145</li>
 *   <li>"123--145" → start=123, end=145</li>
 *   <li>"123" → start=123, end=123 (single page)</li>
 *   <li>"xii-xiv" → roman numerals (stored as-is)</li>
 * </ul>
 */
public record PageRange(
        String start,
        String end,
        String original
) {
    // Pattern for page ranges: "start-end" or "start--end"
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "^\\s*([\\divxlcm]+)\\s*[-–—]+\\s*([\\divxlcm]+)\\s*$",
            Pattern.CASE_INSENSITIVE);

    // Pattern for single page
    private static final Pattern SINGLE_PATTERN = Pattern.compile(
            "^\\s*([\\divxlcm]+)\\s*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Parses a page string into a PageRange.
     *
     * @param pages the page string to parse
     * @return the parsed PageRange, or empty if parsing fails
     */
    public static Optional<PageRange> parse(String pages) {
        if (pages == null || pages.isBlank()) {
            return Optional.empty();
        }

        String trimmed = pages.trim();

        // Try range pattern first
        Matcher rangeMatcher = RANGE_PATTERN.matcher(trimmed);
        if (rangeMatcher.matches()) {
            return Optional.of(new PageRange(
                    rangeMatcher.group(1),
                    rangeMatcher.group(2),
                    trimmed));
        }

        // Try single page pattern
        Matcher singleMatcher = SINGLE_PATTERN.matcher(trimmed);
        if (singleMatcher.matches()) {
            String page = singleMatcher.group(1);
            return Optional.of(new PageRange(page, page, trimmed));
        }

        // Return with original string if can't parse
        return Optional.of(new PageRange(null, null, trimmed));
    }

    /**
     * Gets the start page as an integer, if possible.
     *
     * @return the start page number, or empty if not a number
     */
    public Optional<Integer> startAsInt() {
        return parseAsInt(start);
    }

    /**
     * Gets the end page as an integer, if possible.
     *
     * @return the end page number, or empty if not a number
     */
    public Optional<Integer> endAsInt() {
        return parseAsInt(end);
    }

    /**
     * Calculates the number of pages in the range.
     *
     * @return the page count, or empty if cannot be calculated
     */
    public Optional<Integer> pageCount() {
        Optional<Integer> startInt = startAsInt();
        Optional<Integer> endInt = endAsInt();

        if (startInt.isPresent() && endInt.isPresent()) {
            return Optional.of(endInt.get() - startInt.get() + 1);
        }

        return Optional.empty();
    }

    /**
     * Formats the page range for display.
     *
     * @return formatted string like "123-145" or "123"
     */
    public String format() {
        if (start == null || end == null) {
            return original != null ? original : "";
        }

        if (start.equals(end)) {
            return start;
        }

        return start + "-" + end;
    }

    private static Optional<Integer> parseAsInt(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
