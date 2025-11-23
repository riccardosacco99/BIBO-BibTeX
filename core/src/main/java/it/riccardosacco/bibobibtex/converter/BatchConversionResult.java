package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper for batch conversion output paired with statistics.
 */
public final class BatchConversionResult {
    private final List<BiboDocument> documents;
    private final ConversionStatistics statistics;

    public BatchConversionResult(List<BiboDocument> documents, ConversionStatistics statistics) {
        this.documents = List.copyOf(Objects.requireNonNull(documents, "documents"));
        this.statistics = Objects.requireNonNull(statistics, "statistics");
    }

    public List<BiboDocument> getDocuments() {
        return documents;
    }

    public ConversionStatistics getStatistics() {
        return statistics;
    }
}
