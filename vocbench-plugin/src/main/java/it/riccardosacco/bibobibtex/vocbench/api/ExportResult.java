package it.riccardosacco.bibobibtex.vocbench.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a BibTeX export operation.
 */
public final class ExportResult {
    private final String content;
    private final ExportRequest.ExportFormat format;
    private final int entryCount;
    private final List<String> exportedIds;
    private final List<ExportWarning> warnings;
    private final long durationMs;

    private ExportResult(Builder builder) {
        this.content = builder.content;
        this.format = builder.format;
        this.entryCount = builder.entryCount;
        this.exportedIds = Collections.unmodifiableList(new ArrayList<>(builder.exportedIds));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(builder.warnings));
        this.durationMs = builder.durationMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String content() {
        return content;
    }

    public ExportRequest.ExportFormat format() {
        return format;
    }

    public int entryCount() {
        return entryCount;
    }

    public List<String> exportedIds() {
        return exportedIds;
    }

    public List<ExportWarning> warnings() {
        return warnings;
    }

    public long durationMs() {
        return durationMs;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Warning about potential data loss during export.
     */
    public record ExportWarning(
            String documentId,
            String field,
            String message
    ) {}

    public static final class Builder {
        private String content = "";
        private ExportRequest.ExportFormat format = ExportRequest.ExportFormat.BIBTEX;
        private int entryCount;
        private final List<String> exportedIds = new ArrayList<>();
        private final List<ExportWarning> warnings = new ArrayList<>();
        private long durationMs;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder format(ExportRequest.ExportFormat format) {
            this.format = format;
            return this;
        }

        public Builder entryCount(int count) {
            this.entryCount = count;
            return this;
        }

        public Builder addExportedId(String id) {
            this.exportedIds.add(id);
            return this;
        }

        public Builder addWarning(ExportWarning warning) {
            this.warnings.add(warning);
            return this;
        }

        public Builder durationMs(long duration) {
            this.durationMs = duration;
            return this;
        }

        public ExportResult build() {
            return new ExportResult(this);
        }
    }
}
