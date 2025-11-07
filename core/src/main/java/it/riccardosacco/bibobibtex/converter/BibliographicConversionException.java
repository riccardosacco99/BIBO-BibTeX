package it.riccardosacco.bibobibtex.converter;

/**
 * Exception thrown when a bibliographic conversion operation fails due to invalid input,
 * missing required fields, or other conversion errors.
 *
 * <p>This exception provides detailed error messages indicating which field is invalid
 * and why the conversion failed, helping developers quickly identify and fix data issues.</p>
 *
 * @since Sprint 01
 */
public class BibliographicConversionException extends RuntimeException {

    /**
     * Constructs a new conversion exception with the specified detail message.
     *
     * @param message the detail message explaining why the conversion failed
     */
    public BibliographicConversionException(String message) {
        super(message);
    }

    /**
     * Constructs a new conversion exception with the specified detail message and cause.
     *
     * @param message the detail message explaining why the conversion failed
     * @param cause the underlying cause of the conversion failure
     */
    public BibliographicConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception for a null input.
     *
     * @param inputType the type of input that was null (e.g., "BibTeX entry", "BIBO document")
     * @return a new exception with an appropriate message
     */
    public static BibliographicConversionException nullInput(String inputType) {
        return new BibliographicConversionException(
                "Cannot convert null " + inputType + " - input must not be null");
    }

    /**
     * Creates an exception for a missing required field.
     *
     * @param fieldName the name of the missing required field
     * @return a new exception with an appropriate message
     */
    public static BibliographicConversionException missingRequiredField(String fieldName) {
        return new BibliographicConversionException(
                "Required field '" + fieldName + "' is missing or blank");
    }

    /**
     * Creates an exception for an invalid field value.
     *
     * @param fieldName the name of the invalid field
     * @param value the invalid value
     * @param reason why the value is invalid
     * @return a new exception with an appropriate message
     */
    public static BibliographicConversionException invalidFieldValue(
            String fieldName, String value, String reason) {
        return new BibliographicConversionException(
                String.format("Invalid value for field '%s': '%s' - %s",
                        fieldName, value, reason));
    }

    /**
     * Creates an exception for an invalid identifier format.
     *
     * @param identifierType the type of identifier (e.g., "ISBN", "DOI")
     * @param value the invalid identifier value
     * @param expectedFormat the expected format description
     * @return a new exception with an appropriate message
     */
    public static BibliographicConversionException invalidIdentifier(
            String identifierType, String value, String expectedFormat) {
        return new BibliographicConversionException(
                String.format("Invalid %s identifier '%s' - expected format: %s",
                        identifierType, value, expectedFormat));
    }
}
