package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Objects;
import java.util.Optional;

public final class BiboPersonName {
    private final String fullName;
    private final String givenName;
    private final String familyName;
    private final String suffix;

    private BiboPersonName(Builder builder) {
        this.fullName = builder.fullName;
        this.givenName = builder.givenName;
        this.familyName = builder.familyName;
        this.suffix = builder.suffix;
    }

    public static Builder builder(String fullName) {
        return new Builder(fullName);
    }

    public String fullName() {
        return fullName;
    }

    public Optional<String> givenName() {
        return Optional.ofNullable(givenName);
    }

    public Optional<String> familyName() {
        return Optional.ofNullable(familyName);
    }

    public Optional<String> suffix() {
        return Optional.ofNullable(suffix);
    }

    public static final class Builder {
        private final String fullName;
        private String givenName;
        private String familyName;
        private String suffix;

        private Builder(String fullName) {
            this.fullName = normalizeRequired(fullName, "fullName");
        }

        public Builder givenName(String givenName) {
            this.givenName = normalizeOptional(givenName);
            return this;
        }

        public Builder familyName(String familyName) {
            this.familyName = normalizeOptional(familyName);
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = normalizeOptional(suffix);
            return this;
        }

        public BiboPersonName build() {
            return new BiboPersonName(this);
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
    }
}
