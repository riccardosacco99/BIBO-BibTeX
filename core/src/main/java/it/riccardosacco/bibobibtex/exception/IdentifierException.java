package it.riccardosacco.bibobibtex.exception;

/**
 * Exception thrown when identifier validation or processing fails.
 *
 * <p>Used for:
 * <ul>
 *   <li>Invalid ISBN-10 or ISBN-13 checksums</li>
 *   <li>Invalid ISSN checksums</li>
 *   <li>Malformed DOI formats</li>
 *   <li>Malformed Handle formats</li>
 *   <li>Invalid URLs</li>
 *   <li>Unsupported identifier types</li>
 * </ul>
 *
 * @since 0.1.0
 */
public class IdentifierException extends BibliographicConversionException {

    /**
     * Constructs identifier exception with error message.
     *
     * @param message identifier error description
     */
    public IdentifierException(String message) {
        super(message);
    }

    /**
     * Constructs identifier exception with message and cause.
     *
     * @param message identifier error description
     * @param cause underlying exception
     */
    public IdentifierException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs identifier exception with field-specific details.
     *
     * @param message identifier error description
     * @param fieldName name of invalid field
     * @param fieldValue invalid value
     */
    public IdentifierException(String message, String fieldName, Object fieldValue) {
        super(message, fieldName, fieldValue);
    }
}
