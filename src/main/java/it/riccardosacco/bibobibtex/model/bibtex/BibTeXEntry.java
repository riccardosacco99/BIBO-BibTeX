package it.riccardosacco.bibobibtex.model.bibtex;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BibTeXEntry {
    private final BibTeXEntryType type;
    private final String citationKey;
    private final Map<String, String> fields;

    private BibTeXEntry(Builder builder) {
        this.type = builder.type;
        this.citationKey = builder.citationKey;
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.fields));
    }

    public static Builder builder(BibTeXEntryType type, String citationKey) {
        return new Builder(type, citationKey);
    }

    public BibTeXEntryType type() {
        return type;
    }

    public String citationKey() {
        return citationKey;
    }

    public Map<String, String> fields() {
        return fields;
    }

    public Optional<String> field(String name) {
        return Optional.ofNullable(fields.get(normalizeFieldName(name)));
    }

    public Optional<String> field(String name, String fallbackName) {
        return field(name).or(() -> field(fallbackName));
    }

    public static final class Builder {
        private final BibTeXEntryType type;
        private final String citationKey;
        private final Map<String, String> fields = new LinkedHashMap<>();

        private Builder(BibTeXEntryType type, String citationKey) {
            this.type = Objects.requireNonNull(type, "type");
            this.citationKey = normalizeRequired(citationKey, "citationKey");
        }

        public Builder field(String name, String value) {
            String normalizedName = normalizeFieldName(name);
            if (normalizedName == null) {
                return this;
            }
            String normalizedValue = normalizeOptional(value);
            if (normalizedValue != null) {
                fields.put(normalizedName, normalizedValue);
            }
            return this;
        }

        public Builder appendField(String name, String value, String delimiter) {
            String normalizedName = normalizeFieldName(name);
            if (normalizedName == null) {
                return this;
            }
            String normalizedValue = normalizeOptional(value);
            if (normalizedValue == null) {
                return this;
            }
            fields.merge(
                    normalizedName,
                    normalizedValue,
                    (existing, incoming) -> existing + Objects.requireNonNullElse(delimiter, ", ") + incoming);
            return this;
        }

        public Builder fields(Map<String, String> fields) {
            if (fields != null) {
                fields.forEach(this::field);
            }
            return this;
        }

        public BibTeXEntry build() {
            return new BibTeXEntry(this);
        }
    }

    private static String normalizeRequired(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return trimmed;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeFieldName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.strip().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
