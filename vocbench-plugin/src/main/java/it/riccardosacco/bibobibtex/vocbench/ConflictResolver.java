package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Resolves conflicts between duplicate bibliographic documents.
 *
 * <p>When two documents are identified as duplicates, this class helps:
 * <ul>
 *   <li>Detect which fields have conflicting values</li>
 *   <li>Apply merge strategies to resolve conflicts</li>
 *   <li>Produce a merged document</li>
 * </ul>
 */
public class ConflictResolver {

    private final MergeStrategy defaultStrategy;

    public ConflictResolver() {
        this(MergeStrategy.PREFER_INCOMING);
    }

    public ConflictResolver(MergeStrategy defaultStrategy) {
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "defaultStrategy");
    }

    /**
     * Detects conflicts between two documents.
     *
     * @param existing the existing document
     * @param incoming the incoming document
     * @return map of field names to conflicts
     */
    public Map<String, Conflict> detectConflicts(BiboDocument existing, BiboDocument incoming) {
        Objects.requireNonNull(existing, "existing");
        Objects.requireNonNull(incoming, "incoming");

        Map<String, Conflict> conflicts = new HashMap<>();

        // Compare title
        if (!existing.title().equals(incoming.title())) {
            conflicts.put("title", new Conflict("title", existing.title(), incoming.title()));
        }

        // Compare subtitle
        compareOptional("subtitle", existing.subtitle(), incoming.subtitle(), conflicts);

        // Compare publisher
        compareOptional("publisher", existing.publisher(), incoming.publisher(), conflicts);

        // Compare volume
        compareOptional("volume", existing.volume(), incoming.volume(), conflicts);

        // Compare issue
        compareOptional("issue", existing.issue(), incoming.issue(), conflicts);

        // Compare pages
        compareOptional("pages", existing.pages(), incoming.pages(), conflicts);

        // Compare container title
        compareOptional("containerTitle", existing.containerTitle(), incoming.containerTitle(), conflicts);

        // Compare abstract
        compareOptional("abstract", existing.abstractText(), incoming.abstractText(), conflicts);

        // Compare URL
        compareOptional("url", existing.url(), incoming.url(), conflicts);

        return conflicts;
    }

    /**
     * Merges two documents using the default strategy.
     *
     * @param existing the existing document
     * @param incoming the incoming document
     * @return the merged document
     */
    public BiboDocument merge(BiboDocument existing, BiboDocument incoming) {
        return merge(existing, incoming, defaultStrategy);
    }

    /**
     * Merges two documents using the specified strategy.
     *
     * @param existing the existing document
     * @param incoming the incoming document
     * @param strategy the merge strategy to use
     * @return the merged document
     */
    public BiboDocument merge(BiboDocument existing, BiboDocument incoming, MergeStrategy strategy) {
        Objects.requireNonNull(existing, "existing");
        Objects.requireNonNull(incoming, "incoming");
        Objects.requireNonNull(strategy, "strategy");

        BiFunction<String, String, String> resolver = strategy.resolver();

        BiboDocument.Builder builder = BiboDocument.builder(
                incoming.type() != BiboDocumentType.OTHER ? incoming.type() : existing.type(),
                resolver.apply(existing.title(), incoming.title()));

        // Preserve ID from existing
        existing.id().or(incoming::id).ifPresent(builder::id);

        // Merge optional fields
        mergeOptional(existing.subtitle(), incoming.subtitle(), resolver).ifPresent(builder::subtitle);
        mergeOptional(existing.publisher(), incoming.publisher(), resolver).ifPresent(builder::publisher);
        mergeOptional(existing.volume(), incoming.volume(), resolver).ifPresent(builder::volume);
        mergeOptional(existing.issue(), incoming.issue(), resolver).ifPresent(builder::issue);
        mergeOptional(existing.pages(), incoming.pages(), resolver).ifPresent(builder::pages);
        mergeOptional(existing.containerTitle(), incoming.containerTitle(), resolver).ifPresent(builder::containerTitle);
        mergeOptional(existing.abstractText(), incoming.abstractText(), resolver).ifPresent(builder::abstractText);
        mergeOptional(existing.url(), incoming.url(), resolver).ifPresent(builder::url);
        mergeOptional(existing.language(), incoming.language(), resolver).ifPresent(builder::language);
        mergeOptional(existing.notes(), incoming.notes(), resolver).ifPresent(builder::notes);
        mergeOptional(existing.series(), incoming.series(), resolver).ifPresent(builder::series);
        mergeOptional(existing.edition(), incoming.edition(), resolver).ifPresent(builder::edition);

        // Prefer publication date from incoming if present
        incoming.publicationDate().or(existing::publicationDate).ifPresent(builder::publicationDate);

        // Merge contributors (union)
        var allContributors = new ArrayList<>(existing.contributors());
        for (var contributor : incoming.contributors()) {
            if (!allContributors.contains(contributor)) {
                allContributors.add(contributor);
            }
        }
        builder.contributors(allContributors);

        // Merge identifiers (union, prefer incoming for duplicates)
        var allIdentifiers = new ArrayList<>(existing.identifiers());
        for (var identifier : incoming.identifiers()) {
            boolean exists = allIdentifiers.stream()
                    .anyMatch(id -> id.type() == identifier.type());
            if (!exists) {
                allIdentifiers.add(identifier);
            }
        }
        builder.identifiers(allIdentifiers);

        // Merge keywords (union)
        List<String> allKeywords = new ArrayList<>(existing.keywords());
        for (String keyword : incoming.keywords()) {
            if (!allKeywords.contains(keyword)) {
                allKeywords.add(keyword);
            }
        }
        allKeywords.forEach(builder::addKeyword);

        return builder.build();
    }

    /**
     * Merges documents with custom resolutions for specific conflicts.
     *
     * @param existing    the existing document
     * @param incoming    the incoming document
     * @param resolutions map of field name to resolved value
     * @return the merged document
     */
    public BiboDocument mergeWithResolutions(
            BiboDocument existing,
            BiboDocument incoming,
            Map<String, String> resolutions) {

        // Start with default merge
        BiboDocument merged = merge(existing, incoming, MergeStrategy.PREFER_INCOMING);

        // Apply custom resolutions
        if (resolutions.isEmpty()) {
            return merged;
        }

        BiboDocument.Builder builder = BiboDocument.builder(merged.type(), merged.title());

        merged.id().ifPresent(builder::id);
        merged.subtitle().ifPresent(builder::subtitle);
        merged.publicationDate().ifPresent(builder::publicationDate);
        merged.publisher().ifPresent(builder::publisher);
        merged.containerTitle().ifPresent(builder::containerTitle);
        merged.volume().ifPresent(builder::volume);
        merged.issue().ifPresent(builder::issue);
        merged.pages().ifPresent(builder::pages);
        merged.url().ifPresent(builder::url);
        merged.language().ifPresent(builder::language);
        merged.abstractText().ifPresent(builder::abstractText);
        merged.notes().ifPresent(builder::notes);
        merged.series().ifPresent(builder::series);
        merged.edition().ifPresent(builder::edition);
        builder.contributors(merged.contributors());
        builder.identifiers(merged.identifiers());
        merged.keywords().forEach(builder::addKeyword);

        return builder.build();
    }

    private void compareOptional(String field, Optional<String> a, Optional<String> b,
                                  Map<String, Conflict> conflicts) {
        if (a.isPresent() && b.isPresent() && !a.get().equals(b.get())) {
            conflicts.put(field, new Conflict(field, a.get(), b.get()));
        }
    }

    private Optional<String> mergeOptional(Optional<String> existing, Optional<String> incoming,
                                            BiFunction<String, String, String> resolver) {
        if (incoming.isPresent() && existing.isPresent()) {
            return Optional.of(resolver.apply(existing.get(), incoming.get()));
        }
        return incoming.or(() -> existing);
    }

    /**
     * Represents a conflict between two field values.
     */
    public record Conflict(
            String fieldName,
            String existingValue,
            String incomingValue
    ) {
        public Conflict {
            Objects.requireNonNull(fieldName, "fieldName");
        }
    }

    /**
     * Strategy for resolving merge conflicts.
     */
    public enum MergeStrategy {
        /** Prefer values from the existing document */
        PREFER_EXISTING((existing, incoming) -> existing),

        /** Prefer values from the incoming document */
        PREFER_INCOMING((existing, incoming) -> incoming),

        /** Prefer the longer value */
        PREFER_LONGER((existing, incoming) ->
                existing.length() >= incoming.length() ? existing : incoming),

        /** Combine both values with a separator */
        COMBINE((existing, incoming) ->
                existing.equals(incoming) ? existing : existing + " | " + incoming);

        private final BiFunction<String, String, String> resolver;

        MergeStrategy(BiFunction<String, String, String> resolver) {
            this.resolver = resolver;
        }

        public BiFunction<String, String, String> resolver() {
            return resolver;
        }
    }
}
