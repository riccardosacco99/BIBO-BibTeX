package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Detects duplicate bibliographic documents based on various matching strategies.
 *
 * <p>Supports multiple matching strategies:
 * <ul>
 *   <li><b>Exact match</b>: DOI, ISBN, ISSN, Handle</li>
 *   <li><b>Fuzzy match</b>: Title + author similarity using Jaccard coefficient</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * DuplicateDetector detector = new DuplicateDetector();
 * List<DuplicateCandidate> duplicates = detector.findDuplicates(newDoc, existingDocs);
 * }</pre>
 */
public class DuplicateDetector {
    private static final Logger logger = LoggerFactory.getLogger(DuplicateDetector.class);

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.85;

    private final double similarityThreshold;

    public DuplicateDetector() {
        this(DEFAULT_SIMILARITY_THRESHOLD);
    }

    public DuplicateDetector(double similarityThreshold) {
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
        }
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * Finds potential duplicates of a document within a collection.
     *
     * @param document  the document to check
     * @param existing  collection of existing documents to compare against
     * @return list of duplicate candidates with similarity scores
     */
    public List<DuplicateCandidate> findDuplicates(BiboDocument document, Collection<BiboDocument> existing) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(existing, "existing");

        List<DuplicateCandidate> candidates = new ArrayList<>();

        for (BiboDocument other : existing) {
            if (isSameDocument(document, other)) {
                continue; // Skip self-comparison
            }

            Optional<DuplicateCandidate> candidate = checkForDuplicate(document, other);
            candidate.ifPresent(candidates::add);
        }

        // Sort by similarity score (highest first)
        candidates.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));

        logger.info("Found {} duplicate candidates for document: {}",
                candidates.size(), document.title());

        return candidates;
    }

    /**
     * Checks if a document has any exact duplicates (by identifier).
     *
     * @param document  the document to check
     * @param existing  collection of existing documents
     * @return the first exact duplicate found, or empty
     */
    public Optional<BiboDocument> findExactDuplicate(BiboDocument document, Collection<BiboDocument> existing) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(existing, "existing");

        for (BiboDocument other : existing) {
            if (isSameDocument(document, other)) {
                continue;
            }

            if (isExactMatch(document, other)) {
                logger.info("Found exact duplicate by identifier: {} matches {}",
                        document.title(), other.title());
                return Optional.of(other);
            }
        }

        return Optional.empty();
    }

    private Optional<DuplicateCandidate> checkForDuplicate(BiboDocument doc, BiboDocument other) {
        // Check exact matches first (identifier-based)
        if (isExactMatch(doc, other)) {
            return Optional.of(new DuplicateCandidate(
                    other,
                    1.0,
                    MatchType.EXACT_IDENTIFIER));
        }

        // Check fuzzy match (title + author)
        double similarity = calculateSimilarity(doc, other);
        if (similarity >= similarityThreshold) {
            return Optional.of(new DuplicateCandidate(
                    other,
                    similarity,
                    MatchType.FUZZY_TITLE_AUTHOR));
        }

        return Optional.empty();
    }

    private boolean isExactMatch(BiboDocument doc, BiboDocument other) {
        // Check DOI match
        if (matchByIdentifier(doc, other, BiboIdentifierType.DOI)) {
            return true;
        }

        // Check ISBN match
        if (matchByIdentifier(doc, other, BiboIdentifierType.ISBN_13) ||
                matchByIdentifier(doc, other, BiboIdentifierType.ISBN_10)) {
            return true;
        }

        // Check ISSN match
        if (matchByIdentifier(doc, other, BiboIdentifierType.ISSN)) {
            return true;
        }

        // Check Handle match
        if (matchByIdentifier(doc, other, BiboIdentifierType.HANDLE)) {
            return true;
        }

        return false;
    }

    private boolean matchByIdentifier(BiboDocument doc, BiboDocument other, BiboIdentifierType type) {
        Optional<String> docId = findIdentifier(doc, type);
        Optional<String> otherId = findIdentifier(other, type);

        if (docId.isPresent() && otherId.isPresent()) {
            return normalizeIdentifier(docId.get()).equals(normalizeIdentifier(otherId.get()));
        }

        return false;
    }

    private Optional<String> findIdentifier(BiboDocument doc, BiboIdentifierType type) {
        return doc.identifiers().stream()
                .filter(id -> id.type() == type)
                .map(BiboIdentifier::value)
                .findFirst();
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("-", "");
    }

    private double calculateSimilarity(BiboDocument doc, BiboDocument other) {
        // Title similarity (weight: 0.6)
        double titleSimilarity = jaccardSimilarity(
                tokenize(doc.title()),
                tokenize(other.title()));

        // Author similarity (weight: 0.4)
        double authorSimilarity = calculateAuthorSimilarity(doc, other);

        return (titleSimilarity * 0.6) + (authorSimilarity * 0.4);
    }

    private double calculateAuthorSimilarity(BiboDocument doc, BiboDocument other) {
        if (doc.authors().isEmpty() || other.authors().isEmpty()) {
            return 0.0;
        }

        // Get author names as a combined string for comparison
        String docAuthors = doc.authors().stream()
                .map(c -> c.name().fullName())
                .reduce("", (a, b) -> a + " " + b);

        String otherAuthors = other.authors().stream()
                .map(c -> c.name().fullName())
                .reduce("", (a, b) -> a + " " + b);

        return jaccardSimilarity(tokenize(docAuthors), tokenize(otherAuthors));
    }

    private double jaccardSimilarity(List<String> tokens1, List<String> tokens2) {
        if (tokens1.isEmpty() && tokens2.isEmpty()) {
            return 1.0;
        }
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        // Calculate intersection size
        long intersection = tokens1.stream()
                .filter(tokens2::contains)
                .count();

        // Calculate union size (avoiding duplicates)
        long union = tokens1.size() + tokens2.size() - intersection;

        return (double) intersection / union;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return List.of(text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+"));
    }

    private boolean isSameDocument(BiboDocument doc1, BiboDocument doc2) {
        // Check if they have the same internal ID
        if (doc1.id().isPresent() && doc2.id().isPresent()) {
            return doc1.id().get().equals(doc2.id().get());
        }
        return doc1 == doc2;
    }

    /**
     * Represents a duplicate candidate with similarity information.
     */
    public record DuplicateCandidate(
            BiboDocument document,
            double similarity,
            MatchType matchType
    ) {}

    /**
     * Type of match that identified the duplicate.
     */
    public enum MatchType {
        /** Exact match by identifier (DOI, ISBN, etc.) */
        EXACT_IDENTIFIER,
        /** Fuzzy match by title and author similarity */
        FUZZY_TITLE_AUTHOR
    }
}
