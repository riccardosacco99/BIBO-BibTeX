package it.riccardosacco.bibobibtex.exception;

/**
 * Base exception for bibliographic conversion errors.
 *
 * Thrown when conversion between BibTeX and BIBO fails due to invalid input,
 * missing required fields, or other conversion-related issues.
 */
public class BibliographicConversionException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs exception with error message.
     *
     * @param message error description
     */
    public BibliographicConversionException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message error description
     * @param cause underlying exception
     */
    public BibliographicConversionException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
    }

    /**
     * Constructs exception with field-specific error details.
     *
     * @param message error description
     * @param fieldName name of problematic field
     * @param fieldValue value that caused the error
     */
    public BibliographicConversionException(String message, String fieldName, Object fieldValue) {
        super("%s [field='%s', value='%s']".formatted(message, fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Returns name of field that caused the error.
     *
     * @return field name or null if not field-specific
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns value that caused the error.
     *
     * @return field value or null if not available
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}
