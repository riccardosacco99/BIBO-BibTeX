package it.riccardosacco.bibobibtex.vocbench;

/**
 * Exception thrown when repository operations fail.
 *
 * Indicates errors during RDF repository interactions such as:
 * - Connection failures
 * - Query execution errors
 * - Transaction failures
 * - Constraint violations
 */
public class RepositoryException extends RuntimeException {

    /**
     * Constructs exception with error message.
     *
     * @param message error description
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message error description
     * @param cause underlying exception
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs exception with cause only.
     *
     * @param cause underlying exception
     */
    public RepositoryException(Throwable cause) {
        super(cause);
    }
}
