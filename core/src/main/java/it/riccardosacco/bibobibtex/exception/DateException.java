package it.riccardosacco.bibobibtex.exception;

/**
 * Exception thrown when date validation or parsing fails.
 *
 * <p>Used for:
 * <ul>
 *   <li>Invalid month values (not 1-12)</li>
 *   <li>Invalid day values (e.g., February 30)</li>
 *   <li>Leap year violations (e.g., February 29 in non-leap years)</li>
 *   <li>Unparseable date strings</li>
 *   <li>Future dates when not allowed</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class DateException extends BibliographicConversionException {

    /**
     * Constructs date exception with error message.
     *
     * @param message date error description
     */
    public DateException(String message) {
        super(message);
    }

    /**
     * Constructs date exception with message and cause.
     *
     * @param message date error description
     * @param cause underlying exception
     */
    public DateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs date exception with field-specific details.
     *
     * @param message date error description
     * @param fieldName name of invalid field
     * @param fieldValue invalid value
     */
    public DateException(String message, String fieldName, Object fieldValue) {
        super(message, fieldName, fieldValue);
    }
}
