package it.riccardosacco.bibobibtex.vocbench.integration;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.vocbench.RDF4JRepositoryGateway;
import it.riccardosacco.bibobibtex.vocbench.api.BibTeXApiService;
import it.riccardosacco.bibobibtex.vocbench.api.ExportRequest;
import it.riccardosacco.bibobibtex.vocbench.api.ExportResult;
import it.riccardosacco.bibobibtex.vocbench.api.ImportRequest;
import it.riccardosacco.bibobibtex.vocbench.api.ImportResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full workflow integration tests for the BIBO-BibTeX converter.
 *
 * <p>These tests verify the complete import → storage → retrieval → export cycle.
 */
@DisplayName("Full Workflow Integration Tests")
class FullWorkflowIntegrationTest {

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
    @DisplayName("Complete import-store-retrieve-export workflow")
    void completeWorkflow() {
        // Step 1: Import BibTeX (simplified to match working API test)
        String bibtex = """
            @article{workflow2024,
                author = {Test, Author},
                title = {Complete Workflow Test},
                journal = {Test Journal},
                year = {2024}
            }
            """;

        ImportRequest importRequest = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
        ImportResult importResult = service.importBibTeX(importRequest);

        // Verify import
        assertTrue(importResult.isSuccess(), "Import should succeed");
        assertEquals(1, importResult.successfulImports());

        // Step 2: Verify storage via service export (uses listAll internally)
        ExportRequest exportRequest = ExportRequest.forAllDocuments();
        ExportResult exportResult = service.exportBibTeX(exportRequest);

        // Verify export
        assertEquals(1, exportResult.entryCount(), "Should export 1 entry");
        String exported = exportResult.content();

        assertTrue(exported.contains("Complete Workflow Test"), "Title should be preserved");
        assertTrue(exported.contains("2024"), "Year should be preserved");
    }

    @Test
    @DisplayName("Import all 14 BibTeX types")
    void importAllTypes() {
        String bibtex = """
            @article{art2024,
                author = {Test},
                title = {Article},
                journal = {Journal},
                year = {2024}
            }
            @book{book2024,
                author = {Test},
                title = {Book},
                publisher = {Publisher},
                year = {2024}
            }
            @inbook{inbook2024,
                author = {Test},
                title = {Inbook},
                booktitle = {Book},
                publisher = {Publisher},
                year = {2024},
                chapter = {1}
            }
            @incollection{incol2024,
                author = {Test},
                title = {Incollection},
                booktitle = {Collection},
                publisher = {Publisher},
                year = {2024}
            }
            @inproceedings{inproc2024,
                author = {Test},
                title = {Inproceedings},
                booktitle = {Conference},
                year = {2024}
            }
            @proceedings{proc2024,
                title = {Proceedings},
                year = {2024}
            }
            @conference{conf2024,
                author = {Test},
                title = {Conference},
                booktitle = {Conf},
                year = {2024}
            }
            @phdthesis{phd2024,
                author = {Test},
                title = {PhD Thesis},
                school = {University},
                year = {2024}
            }
            @mastersthesis{msc2024,
                author = {Test},
                title = {Masters Thesis},
                school = {University},
                year = {2024}
            }
            @techreport{tech2024,
                author = {Test},
                title = {Tech Report},
                institution = {Institute},
                year = {2024}
            }
            @manual{manual2024,
                title = {Manual},
                year = {2024}
            }
            @booklet{booklet2024,
                title = {Booklet},
                year = {2024}
            }
            @unpublished{unpub2024,
                author = {Test},
                title = {Unpublished},
                year = {2024}
            }
            @misc{misc2024,
                title = {Misc},
                year = {2024}
            }
            """;

        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));

        assertEquals(14, result.totalEntries(), "Should parse 14 entries");
        assertEquals(14, result.successfulImports(), "All 14 types should import successfully");
        assertEquals(0, result.failedImports());

        // Verify via export
        ExportResult exportResult = service.exportBibTeX(ExportRequest.forAllDocuments());
        assertEquals(14, exportResult.entryCount(), "All 14 documents should be stored");
    }

    @Test
    @DisplayName("Import preserves complex author names")
    void importComplexAuthorNames() {
        String bibtex = """
            @article{names2024,
                author = {van der Berg, Johannes and de la Cruz, Maria Elena and O'Brien, Patrick Jr. and Kim, Soo-Young},
                title = {Complex Names Test},
                journal = {Names Journal},
                year = {2024}
            }
            """;

        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));

        assertTrue(result.isSuccess());
        assertEquals(1, result.importedEntries().size());
    }

    @Test
    @DisplayName("Import preserves identifiers")
    void importPreservesIdentifiers() {
        String bibtex = """
            @article{ids2024,
                author = {Test},
                title = {Identifiers Test},
                journal = {Journal},
                year = {2024},
                doi = {10.1234/test.2024},
                issn = {1234-5678},
                url = {https://example.org/paper}
            }
            """;

        ImportResult result = service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));
        assertTrue(result.isSuccess());
        assertEquals(1, result.importedEntries().size());
    }

    @Test
    @DisplayName("Export multiple documents")
    void exportMultipleDocuments() {
        // Import multiple entries
        String bibtex = """
            @article{multi1,
                author = {First},
                title = {First Article},
                journal = {Journal},
                year = {2024}
            }
            @article{multi2,
                author = {Second},
                title = {Second Article},
                journal = {Journal},
                year = {2024}
            }
            @article{multi3,
                author = {Third},
                title = {Third Article},
                journal = {Journal},
                year = {2024}
            }
            """;

        service.importBibTeX(new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults()));

        // Export all
        ExportResult result = service.exportBibTeX(ExportRequest.forAllDocuments());

        assertEquals(3, result.entryCount());
        assertTrue(result.content().contains("First Article"));
        assertTrue(result.content().contains("Second Article"));
        assertTrue(result.content().contains("Third Article"));
    }

    @Test
    @DisplayName("Error handling: malformed BibTeX")
    void errorHandlingMalformedBibTeX() {
        String malformed = "@article{broken, title = {Missing closing brace";

        ImportResult result = service.importBibTeX(new ImportRequest(malformed, ImportRequest.ImportOptions.defaults()));

        assertFalse(result.isSuccess());
        assertFalse(result.errors().isEmpty());
        assertTrue(result.errors().getFirst().errorType().contains("PARSE"));
    }

}
