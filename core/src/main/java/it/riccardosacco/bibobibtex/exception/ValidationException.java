package it.riccardosacco.bibobibtex.exception;

/**
 * Exception thrown when input validation fails.
 *
 * Used for invalid BibTeX entries, BIBO documents, or field values that don't meet
 * validation requirements (null checks, format validation, range checks, etc.).
 */
public class ValidationException extends BibliographicConversionException {

    /**
     * Constructs validation exception with error message.
     *
     * @param message validation error description
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs validation exception with message and cause.
     *
     * @param message validation error description
     * @param cause underlying exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs validation exception with field-specific details.
     *
     * @param message validation error description
     * @param fieldName name of invalid field
     * @param fieldValue invalid value
     */
    public ValidationException(String message, String fieldName, Object fieldValue) {
        super(message, fieldName, fieldValue);
    }
}
