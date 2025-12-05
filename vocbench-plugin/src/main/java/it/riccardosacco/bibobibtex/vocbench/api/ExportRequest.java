package it.riccardosacco.bibobibtex.vocbench.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request for BibTeX export operation.
 */
public final class ExportRequest {
    private final List<String> documentIds;
    private final ExportFormat format;
    private final ExportOptions options;

    private ExportRequest(Builder builder) {
        this.documentIds = Collections.unmodifiableList(new ArrayList<>(builder.documentIds));
        this.format = builder.format;
        this.options = builder.options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ExportRequest forDocument(String documentId) {
        return builder().addDocumentId(documentId).build();
    }

    public static ExportRequest forAllDocuments() {
        return builder().build();
    }

    public List<String> documentIds() {
        return documentIds;
    }

    public boolean isExportAll() {
        return documentIds.isEmpty();
    }

    public ExportFormat format() {
        return format;
    }

    public ExportOptions options() {
        return options;
    }

    /**
     * Export format options.
     */
    public enum ExportFormat {
        BIBTEX,
        BIBLATEX,
        RIS,
        ENDNOTE
    }

    /**
     * Export options for controlling the output.
     */
    public static final class ExportOptions {
        private final boolean includeAbstract;
        private final boolean includeKeywords;
        private final boolean prettyPrint;

        private ExportOptions(boolean includeAbstract, boolean includeKeywords, boolean prettyPrint) {
            this.includeAbstract = includeAbstract;
            this.includeKeywords = includeKeywords;
            this.prettyPrint = prettyPrint;
        }

        public static ExportOptions defaults() {
            return new ExportOptions(true, true, true);
        }

        public boolean includeAbstract() {
            return includeAbstract;
        }

        public boolean includeKeywords() {
            return includeKeywords;
        }

        public boolean prettyPrint() {
            return prettyPrint;
        }
    }

    public static final class Builder {
        private final List<String> documentIds = new ArrayList<>();
        private ExportFormat format = ExportFormat.BIBTEX;
        private ExportOptions options = ExportOptions.defaults();

        public Builder addDocumentId(String id) {
            this.documentIds.add(Objects.requireNonNull(id, "id"));
            return this;
        }

        public Builder documentIds(List<String> ids) {
            if (ids != null) {
                this.documentIds.addAll(ids);
            }
            return this;
        }

        public Builder format(ExportFormat format) {
            this.format = Objects.requireNonNull(format, "format");
            return this;
        }

        public Builder options(ExportOptions options) {
            this.options = options != null ? options : ExportOptions.defaults();
            return this;
        }

        public ExportRequest build() {
            return new ExportRequest(this);
        }
    }
}
