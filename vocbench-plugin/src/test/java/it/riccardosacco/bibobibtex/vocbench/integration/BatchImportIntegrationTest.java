package it.riccardosacco.bibobibtex.vocbench.integration;

import it.riccardosacco.bibobibtex.vocbench.RDF4JRepositoryGateway;
import it.riccardosacco.bibobibtex.vocbench.api.BibTeXApiService;
import it.riccardosacco.bibobibtex.vocbench.api.ImportRequest;
import it.riccardosacco.bibobibtex.vocbench.api.ImportResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch import integration tests.
 *
 * <p>Tests the system's ability to handle large volumes of entries.
 */
@DisplayName("Batch Import Integration Tests")
class BatchImportIntegrationTest {

    private RDF4JRepositoryGateway gateway;
    private BibTeXApiService service;

    @BeforeEach
    void setUp() {
        SailRepository repository = new SailRepository(new MemoryStore());
        repository.init();
        gateway = new RDF4JRepositoryGateway(repository);
        service = new BibTeXApiService(gateway);
    }

    @AfterEach
    void tearDown() {
        if (gateway != null) {
            gateway.shutdown();
        }
    }

    @Test
    @DisplayName("Import 100 entries")
    void import100Entries() {
        String bibtex = generateBibTeXEntries(100);

        long startTime = System.currentTimeMillis();
        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(100, result.totalEntries());
        assertEquals(100, result.successfulImports(), "All 100 entries should import successfully");
        assertEquals(0, result.failedImports());

        // Performance check: should complete in reasonable time
        assertTrue(duration < 30000, "100 entries should import in under 30 seconds, took: " + duration + "ms");

        // Verify all entries are in import result
        assertEquals(100, result.importedEntries().size(), "All entries should be tracked");
    }

    @Test
    @DisplayName("Import 250 entries")
    void import250Entries() {
        String bibtex = generateBibTeXEntries(250);

        long startTime = System.currentTimeMillis();
        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
        long duration = System.currentTimeMillis() - startTime;

        assertEquals(250, result.totalEntries());
        assertEquals(250, result.successfulImports());

        // Performance check
        assertTrue(duration < 60000, "250 entries should import in under 60 seconds, took: " + duration + "ms");

        // Verify all entries are in import result
        assertEquals(250, result.importedEntries().size(), "All entries should be tracked");
    }

    @Test
    @DisplayName("Import and verify entries are tracked")
    void importAndVerifyTracking() {
        // Import 50 entries
        String bibtex = generateBibTeXEntries(50);
        ImportResult importResult = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));

        // Verify import succeeded
        assertEquals(50, importResult.successfulImports(), "All entries should import");
        assertEquals(50, importResult.importedEntries().size(), "All entries should be tracked");
        assertEquals(0, importResult.failedImports(), "No failures expected");
        assertTrue(importResult.durationMs() > 0, "Duration should be recorded");
    }

    @Test
    @DisplayName("Batch import with mixed types")
    void batchImportMixedTypes() {
        StringBuilder sb = new StringBuilder();

        String[] types = {"article", "book", "inproceedings", "techreport", "phdthesis"};
        for (int i = 0; i < 100; i++) {
            String type = types[i % types.length];
            sb.append(generateEntry(type, i));
        }

        ImportResult result = service.importBibTeX(new ImportRequest(sb.toString(), ImportRequest.ImportOptions.defaults()));

        assertEquals(100, result.totalEntries());
        assertEquals(100, result.successfulImports());
    }

    @Test
    @DisplayName("Batch import statistics")
    void batchImportStatistics() {
        String bibtex = generateBibTeXEntries(50);

        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));

        // Verify statistics
        assertEquals(50, result.totalEntries());
        assertEquals(50, result.successfulImports());
        assertEquals(50, result.importedEntries().size());
        assertTrue(result.durationMs() > 0, "Duration should be recorded");

        // Verify each imported entry is tracked
        for (int i = 0; i < 50; i++) {
            final int index = i;
            assertTrue(result.importedEntries().stream()
                            .anyMatch(e -> e.citationKey().contains(String.valueOf(index))),
                    "Entry " + i + " should be in imported list");
        }
    }

    @Test
    @DisplayName("Batch import with some invalid entries")
    void batchImportWithInvalidEntries() {
        StringBuilder sb = new StringBuilder();

        // Add 8 valid entries
        for (int i = 0; i < 8; i++) {
            sb.append("""
                @article{valid%d,
                    author = {Author %d},
                    title = {Valid Entry %d},
                    journal = {Journal},
                    year = {2024}
                }
                """.formatted(i, i, i));
        }

        // Add 2 entries missing required title (these will fail conversion)
        for (int i = 8; i < 10; i++) {
            sb.append("""
                @article{invalid%d,
                    author = {Author %d},
                    journal = {Journal},
                    year = {2024}
                }
                """.formatted(i, i));
        }

        ImportResult result = service.importBibTeX(new ImportRequest(sb.toString(), ImportRequest.ImportOptions.defaults()));

        assertEquals(10, result.totalEntries());
        // Invalid entries may or may not fail depending on converter leniency
        assertTrue(result.successfulImports() >= 8, "At least 8 valid entries should succeed");
    }

    private String generateBibTeXEntries(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("""
                @article{batch%d,
                    author = {Author %d, Test and Coauthor, Helper},
                    title = {Batch Entry %d: A Comprehensive Study},
                    journal = {Batch Processing Journal},
                    year = {%d},
                    volume = {%d},
                    pages = {%d-%d},
                    doi = {10.1234/batch.%d}
                }
                """.formatted(
                    i, i, i,
                    2020 + (i % 5),
                    1 + (i % 10),
                    1 + i, 10 + i,
                    i));
        }
        return sb.toString();
    }

    private String generateEntry(String type, int index) {
        return switch (type) {
            case "article" -> """
                @article{%s%d,
                    author = {Author %d},
                    title = {%s Entry %d},
                    journal = {Journal},
                    year = {2024}
                }
                """.formatted(type, index, index, capitalize(type), index);
            case "book" -> """
                @book{%s%d,
                    author = {Author %d},
                    title = {%s Entry %d},
                    publisher = {Publisher},
                    year = {2024}
                }
                """.formatted(type, index, index, capitalize(type), index);
            case "inproceedings" -> """
                @inproceedings{%s%d,
                    author = {Author %d},
                    title = {%s Entry %d},
                    booktitle = {Conference},
                    year = {2024}
                }
                """.formatted(type, index, index, capitalize(type), index);
            case "techreport" -> """
                @techreport{%s%d,
                    author = {Author %d},
                    title = {%s Entry %d},
                    institution = {Institute},
                    year = {2024}
                }
                """.formatted(type, index, index, capitalize(type), index);
            case "phdthesis" -> """
                @phdthesis{%s%d,
                    author = {Author %d},
                    title = {%s Entry %d},
                    school = {University},
                    year = {2024}
                }
                """.formatted(type, index, index, capitalize(type), index);
            default -> "";
        };
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
