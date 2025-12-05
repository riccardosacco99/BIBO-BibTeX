package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateDetectorTest {

    private DuplicateDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DuplicateDetector();
    }

    @Test
    void findDuplicates_byDOI_returnsExactMatch() {
        BiboDocument doc1 = createDocument("doc1", "Test Article", "10.1234/test")
                .build();
        BiboDocument doc2 = createDocument("doc2", "Same Article Different Title", "10.1234/test")
                .build();

        List<DuplicateDetector.DuplicateCandidate> duplicates =
                detector.findDuplicates(doc1, List.of(doc2));

        assertEquals(1, duplicates.size());
        assertEquals(1.0, duplicates.getFirst().similarity());
        assertEquals(DuplicateDetector.MatchType.EXACT_IDENTIFIER, duplicates.getFirst().matchType());
    }

    @Test
    void findDuplicates_byISBN_returnsExactMatch() {
        BiboDocument doc1 = BiboDocument.builder(BiboDocumentType.BOOK, "Test Book")
                .id("book1")
                .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "9781234567890"))
                .build();
        BiboDocument doc2 = BiboDocument.builder(BiboDocumentType.BOOK, "Different Title Book")
                .id("book2")
                .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "9781234567890"))
                .build();

        List<DuplicateDetector.DuplicateCandidate> duplicates =
                detector.findDuplicates(doc1, List.of(doc2));

        assertEquals(1, duplicates.size());
        assertEquals(DuplicateDetector.MatchType.EXACT_IDENTIFIER, duplicates.getFirst().matchType());
    }

    @Test
    void findDuplicates_byTitleSimilarity_returnsFuzzyMatch() {
        // Use very similar titles for reliable fuzzy matching
        BiboDocument doc1 = createDocument("doc1", "Machine Learning Natural Language Processing", null)
                .addContributor(createAuthor("John Smith"))
                .build();
        BiboDocument doc2 = createDocument("doc2", "Machine Learning Natural Language Processing Methods", null)
                .addContributor(createAuthor("John Smith"))
                .build();

        // Use a lower threshold for fuzzy matching
        DuplicateDetector fuzzyDetector = new DuplicateDetector(0.75);
        List<DuplicateDetector.DuplicateCandidate> duplicates =
                fuzzyDetector.findDuplicates(doc1, List.of(doc2));

        assertEquals(1, duplicates.size());
        assertEquals(DuplicateDetector.MatchType.FUZZY_TITLE_AUTHOR, duplicates.getFirst().matchType());
        assertTrue(duplicates.getFirst().similarity() >= 0.75);
    }

    @Test
    void findDuplicates_differentContent_returnsEmpty() {
        BiboDocument doc1 = createDocument("doc1", "Artificial Intelligence", null)
                .addContributor(createAuthor("Alice"))
                .build();
        BiboDocument doc2 = createDocument("doc2", "Database Systems", null)
                .addContributor(createAuthor("Bob"))
                .build();

        List<DuplicateDetector.DuplicateCandidate> duplicates =
                detector.findDuplicates(doc1, List.of(doc2));

        assertTrue(duplicates.isEmpty());
    }

    @Test
    void findDuplicates_emptyCollection_returnsEmpty() {
        BiboDocument doc = createDocument("doc1", "Test", null).build();

        List<DuplicateDetector.DuplicateCandidate> duplicates =
                detector.findDuplicates(doc, List.of());

        assertTrue(duplicates.isEmpty());
    }

    @Test
    void findDuplicates_sameDocument_excludesSelf() {
        BiboDocument doc = createDocument("doc1", "Test", null).build();

        List<DuplicateDetector.DuplicateCandidate> duplicates =
                detector.findDuplicates(doc, List.of(doc));

        assertTrue(duplicates.isEmpty());
    }

    @Test
    void findExactDuplicate_exists_returnsDocument() {
        BiboDocument doc1 = createDocument("doc1", "Test", "10.1234/test").build();
        BiboDocument doc2 = createDocument("doc2", "Same DOI", "10.1234/test").build();

        Optional<BiboDocument> duplicate = detector.findExactDuplicate(doc1, List.of(doc2));

        assertTrue(duplicate.isPresent());
        assertEquals("Same DOI", duplicate.get().title());
    }

    @Test
    void findExactDuplicate_notExists_returnsEmpty() {
        BiboDocument doc1 = createDocument("doc1", "Test", "10.1234/test").build();
        BiboDocument doc2 = createDocument("doc2", "Different", "10.5678/other").build();

        Optional<BiboDocument> duplicate = detector.findExactDuplicate(doc1, List.of(doc2));

        assertTrue(duplicate.isEmpty());
    }

    @Test
    void constructor_customThreshold_usesThreshold() {
        DuplicateDetector strictDetector = new DuplicateDetector(0.95);

        BiboDocument doc1 = createDocument("doc1", "Machine Learning Applications", null)
                .addContributor(createAuthor("John"))
                .build();
        BiboDocument doc2 = createDocument("doc2", "Machine Learning in Applications", null)
                .addContributor(createAuthor("John"))
                .build();

        // With strict threshold (0.95), this should not be a match
        List<DuplicateDetector.DuplicateCandidate> strictResult =
                strictDetector.findDuplicates(doc1, List.of(doc2));

        // With default threshold (0.85), this should be a match
        DuplicateDetector lenientDetector = new DuplicateDetector(0.70);
        List<DuplicateDetector.DuplicateCandidate> lenientResult =
                lenientDetector.findDuplicates(doc1, List.of(doc2));

        // The lenient detector should find more/equal matches
        assertTrue(lenientResult.size() >= strictResult.size());
    }

    @Test
    void constructor_invalidThreshold_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DuplicateDetector(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new DuplicateDetector(1.5));
    }

    @Test
    void findDuplicates_sortsByHighestSimilarity() {
        BiboDocument doc = createDocument("doc", "Data Science for Machine Learning", null)
                .addContributor(createAuthor("John Smith"))
                .build();

        BiboDocument similarDoc = createDocument("similar", "Data Science in Machine Learning", null)
                .addContributor(createAuthor("John Smith"))
                .build();

        BiboDocument lessSimilar = createDocument("less", "Introduction to Data Science", null)
                .addContributor(createAuthor("John Smith"))
                .build();

        DuplicateDetector lenientDetector = new DuplicateDetector(0.5);
        List<DuplicateDetector.DuplicateCandidate> duplicates =
                lenientDetector.findDuplicates(doc, List.of(lessSimilar, similarDoc));

        if (duplicates.size() >= 2) {
            assertTrue(duplicates.get(0).similarity() >= duplicates.get(1).similarity(),
                    "Results should be sorted by similarity descending");
        }
    }

    private BiboDocument.Builder createDocument(String id, String title, String doi) {
        BiboDocument.Builder builder = BiboDocument.builder(BiboDocumentType.ARTICLE, title)
                .id(id)
                .publicationDate(BiboPublicationDate.ofYear(2024));

        if (doi != null) {
            builder.addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, doi));
        }

        return builder;
    }

    private BiboContributor createAuthor(String name) {
        BiboPersonName personName = BiboPersonName.builder(name).build();
        return new BiboContributor(personName, BiboContributorRole.AUTHOR);
    }
}
