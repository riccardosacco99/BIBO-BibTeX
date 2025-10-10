package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Objects;

public final class BiboIdentifier {
    private final BiboIdentifierType type;
    private final String value;

    public BiboIdentifier(BiboIdentifierType type, String value) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = normalizeRequired(value, "value");
    }

    public BiboIdentifierType type() {
        return type;
    }

    public String value() {
        return value;
    }

    private static String normalizeRequired(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return trimmed;
    }
}
