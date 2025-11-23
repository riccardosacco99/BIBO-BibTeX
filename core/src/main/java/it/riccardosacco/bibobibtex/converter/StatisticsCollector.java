package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects statistics during batch conversion and builds an immutable snapshot.
 */
public final class StatisticsCollector {
    private int totalEntries;
    private int successfulConversions;
    private int failedConversions;
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, Integer> fieldCounts = new HashMap<>();
    private long startTimeMs;

    public void startTracking() {
        startTimeMs = System.currentTimeMillis();
    }

    public void recordEntry() {
        totalEntries++;
    }

    public void recordSuccess(BiboDocument doc) {
        successfulConversions++;
        if (doc == null) {
            return;
        }
        incrementField("title");
        if (!doc.contributors().isEmpty()) {
            incrementField("contributors");
        }
        doc.publicationDate().ifPresent(date -> {
            incrementField("date");
            date.month().ifPresent(month -> incrementField("date-month"));
            date.day().ifPresent(day -> incrementField("date-day"));
        });
        doc.publisher().ifPresent(p -> incrementField("publisher"));
        doc.placeOfPublication().ifPresent(p -> incrementField("place"));
        doc.conferenceLocation().ifPresent(p -> incrementField("conferenceLocation"));
        doc.conferenceOrganizer().ifPresent(p -> incrementField("conferenceOrganizer"));
        doc.degreeType().ifPresent(p -> incrementField("degreeType"));
        doc.containerTitle().ifPresent(p -> incrementField("containerTitle"));
        doc.volume().ifPresent(p -> incrementField("volume"));
        doc.issue().ifPresent(p -> incrementField("issue"));
        doc.pages().ifPresent(p -> incrementField("pages"));
        if (!doc.identifiers().isEmpty()) {
            incrementField("identifiers");
        }
        doc.url().ifPresent(p -> incrementField("url"));
        doc.language().ifPresent(p -> incrementField("language"));
        doc.abstractText().ifPresent(p -> incrementField("abstract"));
        doc.notes().ifPresent(p -> incrementField("notes"));
        doc.series().ifPresent(p -> incrementField("series"));
        doc.edition().ifPresent(p -> incrementField("edition"));
        if (!doc.keywords().isEmpty()) {
            incrementField("keywords");
        }
    }

    public void recordFailure(Exception e) {
        failedConversions++;
        if (e != null && e.getMessage() != null && !e.getMessage().isBlank()) {
            warnings.add(e.getMessage());
        }
    }

    public void recordWarning(String message) {
        if (message != null && !message.isBlank()) {
            warnings.add(message.trim());
        }
    }

    public ConversionStatistics build() {
        long endTime = System.currentTimeMillis();
        return new ConversionStatistics(
                totalEntries,
                successfulConversions,
                failedConversions,
                warnings,
                fieldCounts,
                endTime - startTimeMs);
    }

    private void incrementField(String field) {
        fieldCounts.compute(field, (key, current) -> current == null ? 1 : current + 1);
    }
}
