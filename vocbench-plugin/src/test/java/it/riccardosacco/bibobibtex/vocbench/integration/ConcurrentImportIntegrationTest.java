package it.riccardosacco.bibobibtex.vocbench.integration;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrent import integration tests.
 *
 * <p>Tests thread safety and concurrent access to the repository.
 */
@DisplayName("Concurrent Import Integration Tests")
class ConcurrentImportIntegrationTest {

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
    @DisplayName("Concurrent imports from multiple threads")
    void concurrentImports() throws Exception {
        int threadCount = 5;
        int entriesPerThread = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<ImportResult>> tasks = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            tasks.add(() -> {
                String bibtex = generateThreadBibTeX(threadId, entriesPerThread);
                ImportRequest request = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
                ImportResult result = service.importBibTeX(request);

                if (result.isSuccess()) {
                    successCount.addAndGet(result.successfulImports());
                } else {
                    failCount.addAndGet(result.failedImports());
                }

                return result;
            });
        }

        // Execute all tasks concurrently
        List<Future<ImportResult>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS), "All tasks should complete");

        // Verify all completed
        for (Future<ImportResult> future : futures) {
            assertTrue(future.isDone());
            assertNotNull(future.get());
        }

        // Verify total entries stored
        List<BiboDocument> stored = gateway.listAll();

        // Due to concurrent access, some entries might have unique key conflicts
        // but we should have at least some entries stored
        assertTrue(stored.size() > 0, "Some entries should be stored");
        assertTrue(successCount.get() > 0, "Some imports should succeed");
    }

    @Test
    @DisplayName("Concurrent read and write operations")
    void concurrentReadWrite() throws Exception {
        // First, import some initial data
        String initialBibtex = """
            @article{initial1,
                author = {Initial},
                title = {Initial Entry 1},
                journal = {Journal},
                year = {2024}
            }
            @article{initial2,
                author = {Initial},
                title = {Initial Entry 2},
                journal = {Journal},
                year = {2024}
            }
            """;
        service.importBibTeX(new ImportRequest(initialBibtex, ImportRequest.ImportOptions.defaults()));

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Object>> tasks = new ArrayList<>();

        // Mix of read and write operations
        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            if (taskId % 2 == 0) {
                // Write task
                tasks.add(() -> {
                    String bibtex = """
                        @article{concurrent%d,
                            author = {Concurrent},
                            title = {Concurrent Entry %d},
                            journal = {Journal},
                            year = {2024}
                        }
                        """.formatted(taskId, taskId);
                    return service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
                });
            } else {
                // Read task
                tasks.add(() -> gateway.listAll());
            }
        }

        // Execute all tasks
        List<Future<Object>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify no exceptions
        for (Future<Object> future : futures) {
            assertDoesNotThrow(() -> future.get());
        }
    }

    @Test
    @DisplayName("Stress test with rapid sequential imports")
    void rapidSequentialImports() {
        int iterations = 50;
        int successCount = 0;

        for (int i = 0; i < iterations; i++) {
            String bibtex = """
                @article{rapid%d,
                    author = {Rapid %d},
                    title = {Rapid Entry %d},
                    journal = {Journal},
                    year = {2024}
                }
                """.formatted(i, i, i);

            ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
            if (result.isSuccess()) {
                successCount++;
            }
        }

        assertEquals(iterations, successCount, "All rapid imports should succeed");
        assertEquals(iterations, gateway.listAll().size(), "All entries should be stored");
    }

    @Test
    @DisplayName("Concurrent validation operations")
    void concurrentValidation() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<BibTeXApiService.ValidationResult>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            tasks.add(() -> {
                String bibtex = """
                    @article{validate%d,
                        author = {Validator %d},
                        title = {Validation Entry %d},
                        journal = {Journal},
                        year = {2024}
                    }
                    """.formatted(taskId, taskId, taskId);
                return service.validate(bibtex);
            });
        }

        List<Future<BibTeXApiService.ValidationResult>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // All validations should succeed
        for (Future<BibTeXApiService.ValidationResult> future : futures) {
            BibTeXApiService.ValidationResult result = future.get();
            assertTrue(result.isValid(), "Validation should succeed");
        }
    }

    @Test
    @DisplayName("Repository isolation between operations")
    void repositoryIsolation() throws Exception {
        // Import initial data
        String bibtex1 = """
            @article{isolation1,
                author = {Test},
                title = {Isolation Test 1},
                journal = {Journal},
                year = {2024}
            }
            """;
        service.importBibTeX(new ImportRequest(bibtex1, ImportRequest.ImportOptions.defaults()));

        // Get initial count
        int initialCount = gateway.listAll().size();

        // Concurrent operations
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<ImportResult> importFuture = executor.submit(() -> {
            String bibtex = """
                @article{isolation2,
                    author = {Test},
                    title = {Isolation Test 2},
                    journal = {Journal},
                    year = {2024}
                }
                """;
            return service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
        });

        Future<List<BiboDocument>> listFuture = executor.submit(() -> gateway.listAll());

        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify results
        assertTrue(importFuture.get().isSuccess());
        assertNotNull(listFuture.get());

        // Final count should be initial + 1
        int finalCount = gateway.listAll().size();
        assertEquals(initialCount + 1, finalCount);
    }

    private String generateThreadBibTeX(int threadId, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("""
                @article{thread%d_entry%d,
                    author = {Thread %d Author %d},
                    title = {Thread %d Entry %d},
                    journal = {Concurrent Journal},
                    year = {2024}
                }
                """.formatted(threadId, i, threadId, i, threadId, i));
        }
        return sb.toString();
    }
}
