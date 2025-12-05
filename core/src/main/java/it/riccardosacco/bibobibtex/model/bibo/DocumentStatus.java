package it.riccardosacco.bibobibtex.model.bibo;

/**
 * Publication status of a bibliographic document.
 */
public enum DocumentStatus {
    /** Document is in draft state, not yet published */
    DRAFT("draft"),

    /** Document has been submitted for review */
    SUBMITTED("submitted"),

    /** Document is under peer review */
    UNDER_REVIEW("under_review"),

    /** Document has been accepted but not yet published */
    ACCEPTED("accepted"),

    /** Document is available as a preprint */
    PREPRINT("preprint"),

    /** Document has been published (default for most references) */
    PUBLISHED("published"),

    /** Document has been retracted */
    RETRACTED("retracted"),

    /** Document status is unknown */
    UNKNOWN("unknown");

    private final String value;

    DocumentStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * Parses a status string to a DocumentStatus.
     *
     * @param value the status string
     * @return the matching status, or UNKNOWN if not recognized
     */
    public static DocumentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        String normalized = value.toLowerCase().trim().replace("-", "_").replace(" ", "_");

        for (DocumentStatus status : values()) {
            if (status.value.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        return UNKNOWN;
    }
}
