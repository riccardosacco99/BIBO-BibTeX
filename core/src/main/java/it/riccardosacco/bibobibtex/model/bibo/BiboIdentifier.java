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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BiboIdentifier that = (BiboIdentifier) o;
        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
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
