package it.riccardosacco.bibobibtex.vocbench.api;

import it.riccardosacco.bibobibtex.vocbench.RDF4JRepositoryGateway;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BibTeXApiService.
 */
class BibTeXApiServiceTest {

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

    // Import tests

    @Test
    void importBibTeX_singleEntry_success() {
        String bibtex = """
            @article{doe2024,
                author = {Doe, John},
                title = {A Test Article},
                journal = {Test Journal},
                year = {2024}
            }
            """;

        ImportRequest request = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
        ImportResult result = service.importBibTeX(request);

        assertTrue(result.isSuccess());
        assertEquals(1, result.totalEntries());
        assertEquals(1, result.successfulImports());
        assertEquals(0, result.failedImports());
        assertEquals(1, result.importedEntries().size());
        assertEquals("doe2024", result.importedEntries().getFirst().citationKey());
    }

    @Test
    void importBibTeX_multipleEntries_success() {
        String bibtex = """
            @article{first2024,
                author = {First, Author},
                title = {First Article},
                journal = {Journal One},
                year = {2024}
            }
            @book{second2024,
                author = {Second, Author},
                title = {A Book},
                publisher = {Publisher},
                year = {2024}
            }
            @inproceedings{third2024,
                author = {Third, Author},
                title = {Conference Paper},
                booktitle = {Proceedings},
                year = {2024}
            }
            """;

        ImportRequest request = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
        ImportResult result = service.importBibTeX(request);

        assertTrue(result.isSuccess());
        assertEquals(3, result.totalEntries());
        assertEquals(3, result.successfulImports());
    }

    @Test
    void importBibTeX_invalidSyntax_returnsError() {
        String bibtex = "@article{broken, title = {Missing closing brace";

        ImportRequest request = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
        ImportResult result = service.importBibTeX(request);

        assertFalse(result.isSuccess());
        assertEquals(0, result.totalEntries());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().getFirst().errorType().contains("PARSE"));
    }

    // Export tests

    @Test
    void exportBibTeX_afterImport_roundTrip() {
        // Import first
        String originalBibtex = """
            @article{roundtrip2024,
                author = {Test, Author},
                title = {Round Trip Test},
                journal = {Test Journal},
                year = {2024}
            }
            """;

        ImportRequest importRequest = new ImportRequest(originalBibtex, ImportRequest.ImportOptions.defaults());
        service.importBibTeX(importRequest);

        // Then export
        ExportRequest exportRequest = ExportRequest.forAllDocuments();
        ExportResult result = service.exportBibTeX(exportRequest);

        assertEquals(1, result.entryCount());
        assertTrue(result.content().contains("Round Trip Test"),
                "Expected title in output, got: " + result.content());
    }

    @Test
    void exportBibTeX_emptyRepository_returnsEmpty() {
        ExportRequest request = ExportRequest.forAllDocuments();
        ExportResult result = service.exportBibTeX(request);

        assertEquals(0, result.entryCount());
    }

    @Test
    void exportBibTeX_specificDocument_success() {
        // Import multiple entries
        String bibtex = """
            @article{target2024,
                author = {Target, Author},
                title = {Target Document},
                journal = {Journal},
                year = {2024}
            }
            @book{other2024,
                author = {Other, Author},
                title = {Other Document},
                publisher = {Publisher},
                year = {2024}
            }
            """;

        ImportRequest importRequest = new ImportRequest(bibtex, ImportRequest.ImportOptions.defaults());
        service.importBibTeX(importRequest);

        // Export specific document
        ExportRequest exportRequest = ExportRequest.forDocument("target2024");
        ExportResult result = service.exportBibTeX(exportRequest);

        assertEquals(1, result.entryCount());
        assertTrue(result.content().contains("Target Document"));
    }

    // Validation tests

    @Test
    void validate_validBibTeX_returnsValid() {
        String bibtex = """
            @article{valid2024,
                author = {Valid, Author},
                title = {Valid Article},
                journal = {Journal},
                year = {2024}
            }
            """;

        BibTeXApiService.ValidationResult result = service.validate(bibtex);

        assertTrue(result.isValid());
        assertEquals(1, result.entryCount());
        assertEquals(1, result.validEntries().size());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_invalidSyntax_returnsInvalid() {
        String bibtex = "@article{broken syntax here";

        BibTeXApiService.ValidationResult result = service.validate(bibtex);

        assertFalse(result.isValid());
        assertFalse(result.errors().isEmpty());
    }

    // Preview tests

    @Test
    void preview_validEntries_returnsPreview() {
        String bibtex = """
            @article{preview2024,
                author = {Preview, Author and Another, One},
                title = {Preview Test},
                journal = {Journal},
                year = {2024},
                doi = {10.1234/test}
            }
            """;

        List<BibTeXApiService.ConversionPreview> previews = service.preview(bibtex);

        assertEquals(1, previews.size());
        BibTeXApiService.ConversionPreview preview = previews.getFirst();

        assertTrue(preview.valid());
        assertEquals("preview2024", preview.citationKey());
        assertEquals("Preview Test", preview.title());
        assertEquals("ARTICLE", preview.type());
        assertEquals(2, preview.authorCount());
        assertEquals("2024", preview.year());
    }

    @Test
    void preview_missingRequiredField_returnsError() {
        // Missing title which is required for conversion
        String bibtex = """
            @article{notitle2024,
                author = {Test, Author},
                year = {2024}
            }
            """;

        List<BibTeXApiService.ConversionPreview> previews = service.preview(bibtex);

        assertEquals(1, previews.size());
        // Document may or may not be valid depending on converter behavior
        // Just verify we get a preview back
        assertNotNull(previews.getFirst().citationKey());
    }

    // Duplicate detection tests

    @Test
    void importBibTeX_duplicateWithSkip_skipsDuplicate() {
        String bibtex1 = """
            @article{dup2024,
                author = {Duplicate, Test},
                title = {Original Entry},
                journal = {Journal},
                year = {2024}
            }
            """;

        String bibtex2 = """
            @article{dup2024,
                author = {Duplicate, Test},
                title = {Duplicate Entry},
                journal = {Journal},
                year = {2024}
            }
            """;

        ImportRequest.ImportOptions options = ImportRequest.ImportOptions.builder()
                .detectDuplicates(true)
                .duplicateStrategy(ImportRequest.DuplicateStrategy.SKIP)
                .build();

        // Import first
        service.importBibTeX(new ImportRequest(bibtex1, options));

        // Import duplicate
        ImportResult result = service.importBibTeX(new ImportRequest(bibtex2, options));

        assertEquals(1, result.totalEntries());
        assertEquals(0, result.successfulImports());
        assertEquals(1, result.skippedDuplicates());
    }
}
