package it.riccardosacco.bibobibtex.vocbench.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a BibTeX import operation.
 */
public final class ImportResult {
    private final int totalEntries;
    private final int successfulImports;
    private final int failedImports;
    private final int skippedDuplicates;
    private final List<ImportedEntry> importedEntries;
    private final List<ImportError> errors;
    private final List<String> warnings;
    private final long durationMs;

    private ImportResult(Builder builder) {
        this.totalEntries = builder.totalEntries;
        this.successfulImports = builder.successfulImports;
        this.failedImports = builder.failedImports;
        this.skippedDuplicates = builder.skippedDuplicates;
        this.importedEntries = Collections.unmodifiableList(new ArrayList<>(builder.importedEntries));
        this.errors = Collections.unmodifiableList(new ArrayList<>(builder.errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.durationMs = builder.durationMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int totalEntries() {
        return totalEntries;
    }

    public int successfulImports() {
        return successfulImports;
    }

    public int failedImports() {
        return failedImports;
    }

    public int skippedDuplicates() {
        return skippedDuplicates;
    }

    public List<ImportedEntry> importedEntries() {
        return importedEntries;
    }

    public List<ImportError> errors() {
        return errors;
    }

    public List<String> warnings() {
        return warnings;
    }

    public long durationMs() {
        return durationMs;
    }

    public boolean isSuccess() {
        return failedImports == 0;
    }

    /**
     * Record of a successfully imported entry.
     */
    public record ImportedEntry(
            String citationKey,
            String documentId,
            String title,
            String type
    ) {}

    /**
     * Record of an import error.
     */
    public record ImportError(
            String citationKey,
            String errorMessage,
            String errorType
    ) {}

    public static final class Builder {
        private int totalEntries;
        private int successfulImports;
        private int failedImports;
        private int skippedDuplicates;
        private final List<ImportedEntry> importedEntries = new ArrayList<>();
        private final List<ImportError> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private long durationMs;

        public Builder totalEntries(int count) {
            this.totalEntries = count;
            return this;
        }

        public Builder successfulImports(int count) {
            this.successfulImports = count;
            return this;
        }

        public Builder failedImports(int count) {
            this.failedImports = count;
            return this;
        }

        public Builder skippedDuplicates(int count) {
            this.skippedDuplicates = count;
            return this;
        }

        public Builder addImportedEntry(ImportedEntry entry) {
            this.importedEntries.add(entry);
            return this;
        }

        public Builder addError(ImportError error) {
            this.errors.add(error);
            return this;
        }

        public Builder addWarning(String warning) {
            this.warnings.add(warning);
            return this;
        }

        public Builder durationMs(long duration) {
            this.durationMs = duration;
            return this;
        }

        public ImportResult build() {
            return new ImportResult(this);
        }
    }
}
