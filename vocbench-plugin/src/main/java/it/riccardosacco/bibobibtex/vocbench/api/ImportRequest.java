package it.riccardosacco.bibobibtex.vocbench.api;

import java.util.Objects;

/**
 * Request for BibTeX import operation.
 */
public final class ImportRequest {
    private final String bibtexContent;
    private final ImportOptions options;

    public ImportRequest(String bibtexContent, ImportOptions options) {
        this.bibtexContent = Objects.requireNonNull(bibtexContent, "bibtexContent");
        this.options = options != null ? options : ImportOptions.defaults();
    }

    public String bibtexContent() {
        return bibtexContent;
    }

    public ImportOptions options() {
        return options;
    }

    /**
     * Import options for controlling the conversion behavior.
     */
    public static final class ImportOptions {
        private final boolean validateIdentifiers;
        private final boolean detectDuplicates;
        private final DuplicateStrategy duplicateStrategy;
        private final String namespacePrefix;

        private ImportOptions(Builder builder) {
            this.validateIdentifiers = builder.validateIdentifiers;
            this.detectDuplicates = builder.detectDuplicates;
            this.duplicateStrategy = builder.duplicateStrategy;
            this.namespacePrefix = builder.namespacePrefix;
        }

        public static ImportOptions defaults() {
            return new Builder().build();
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean validateIdentifiers() {
            return validateIdentifiers;
        }

        public boolean detectDuplicates() {
            return detectDuplicates;
        }

        public DuplicateStrategy duplicateStrategy() {
            return duplicateStrategy;
        }

        public String namespacePrefix() {
            return namespacePrefix;
        }

        public static final class Builder {
            private boolean validateIdentifiers = true;
            private boolean detectDuplicates = true;
            private DuplicateStrategy duplicateStrategy = DuplicateStrategy.SKIP;
            private String namespacePrefix = "http://example.org/bibo/";

            public Builder validateIdentifiers(boolean validate) {
                this.validateIdentifiers = validate;
                return this;
            }

            public Builder detectDuplicates(boolean detect) {
                this.detectDuplicates = detect;
                return this;
            }

            public Builder duplicateStrategy(DuplicateStrategy strategy) {
                this.duplicateStrategy = strategy;
                return this;
            }

            public Builder namespacePrefix(String prefix) {
                this.namespacePrefix = prefix;
                return this;
            }

            public ImportOptions build() {
                return new ImportOptions(this);
            }
        }
    }

    /**
     * Strategy for handling duplicate entries during import.
     */
    public enum DuplicateStrategy {
        /** Skip duplicate entries */
        SKIP,
        /** Merge with existing entry */
        MERGE,
        /** Create new entry anyway */
        CREATE_NEW,
        /** Return error and abort */
        ERROR
    }
}
