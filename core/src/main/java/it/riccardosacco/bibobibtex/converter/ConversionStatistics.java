package it.riccardosacco.bibobibtex.converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot of conversion statistics for a batch run.
 */
public final class ConversionStatistics {
    private final int totalEntries;
    private final int successfulConversions;
    private final int failedConversions;
    private final List<String> warningMessages;
    private final Map<String, Integer> fieldStatistics;
    private final long conversionTimeMs;

    public ConversionStatistics(
            int totalEntries,
            int successfulConversions,
            int failedConversions,
            List<String> warningMessages,
            Map<String, Integer> fieldStatistics,
            long conversionTimeMs) {

        this.totalEntries = totalEntries;
        this.successfulConversions = successfulConversions;
        this.failedConversions = failedConversions;
        this.warningMessages = List.copyOf(Objects.requireNonNull(warningMessages, "warningMessages"));
        this.fieldStatistics = Collections.unmodifiableMap(
                Objects.requireNonNull(fieldStatistics, "fieldStatistics"));
        this.conversionTimeMs = conversionTimeMs;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getSuccessfulConversions() {
        return successfulConversions;
    }

    public int getFailedConversions() {
        return failedConversions;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public Map<String, Integer> getFieldStatistics() {
        return fieldStatistics;
    }

    public long getConversionTimeMs() {
        return conversionTimeMs;
    }

    /**
     * Renders a human-friendly text report for CLI usage.
     */
    public String toTextReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Conversion Statistics ===\n");
        report.append("Total entries: %d%n".formatted(totalEntries));
        report.append("Successful: %d%n".formatted(successfulConversions));
        report.append("Failed: %d%n".formatted(failedConversions));
        report.append("Time: %d ms%n".formatted(conversionTimeMs));

        if (!warningMessages.isEmpty()) {
            report.append("\nWarnings:\n");
            warningMessages.forEach(warning -> report.append("  - ").append(warning).append('\n'));
        }

        if (!fieldStatistics.isEmpty()) {
            report.append("\nField Statistics:\n");
            fieldStatistics.forEach((field, count) ->
                    report.append("  %s: %d%n".formatted(field, count)));
        }

        return report.toString();
    }

    /**
     * Lightweight JSON representation without external dependencies.
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append('{');
        json.append("\"total\":").append(totalEntries).append(',');
        json.append("\"successful\":").append(successfulConversions).append(',');
        json.append("\"failed\":").append(failedConversions).append(',');
        json.append("\"timeMs\":").append(conversionTimeMs);

        if (!warningMessages.isEmpty()) {
            json.append(",\"warnings\":[");
            for (int i = 0; i < warningMessages.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append('"')
                        .append(escapeJson(warningMessages.get(i)))
                        .append('"');
            }
            json.append(']');
        }

        if (!fieldStatistics.isEmpty()) {
            json.append(",\"fields\":{");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : fieldStatistics.entrySet()) {
                if (!first) {
                    json.append(',');
                }
                json.append('"')
                        .append(escapeJson(entry.getKey()))
                        .append("\":")
                        .append(entry.getValue());
                first = false;
            }
            json.append('}');
        }

        json.append('}');
        return json.toString();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
