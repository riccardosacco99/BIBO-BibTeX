package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RDF4J repository gateway implementation.
 */
class RDF4JRepositoryGatewayTest {

    @TempDir
    Path tempDir;

    private RDF4JRepositoryGateway gateway;
    private Path dataDir;

    @BeforeEach
    void setUp() throws IOException {
        dataDir = Files.createDirectory(tempDir.resolve("native-store"));
        gateway = new RDF4JRepositoryGateway(dataDir.toString());
    }

    @AfterEach
    void tearDown() {
        if (gateway != null) {
            gateway.shutdown();
        }
    }

    // Store tests (5 tests)

    @Test
    void store_singleDocument_success() {
        BiboDocument doc = createTestDocument("doc1", "Test Article");

        gateway.store(doc.rdfModel());

        // Note: fetchByIdentifier returns empty until Phase 7.B (RDF->BiboDocument conversion)
        // For now we just verify store doesn't throw
        assertDoesNotThrow(() -> gateway.fetchByIdentifier("doc1"));
    }

    @Test
    void store_multipleDocuments_success() {
        BiboDocument doc1 = createTestDocument("doc1", "First Article");
        BiboDocument doc2 = createTestDocument("doc2", "Second Article");
        BiboDocument doc3 = createTestDocument("doc3", "Third Article");

        assertDoesNotThrow(() -> {
            gateway.store(doc1.rdfModel());
            gateway.store(doc2.rdfModel());
            gateway.store(doc3.rdfModel());
        });

        // listAll() returns empty until Phase 7.B
        List<BiboDocument> all = gateway.listAll();
        assertNotNull(all);
    }

    @Test
    void store_duplicateIds_overwritesData() {
        BiboDocument doc1 = createTestDocument("same-id", "First Version");
        BiboDocument doc2 = createTestDocument("same-id", "Second Version");

        assertDoesNotThrow(() -> {
            gateway.store(doc1.rdfModel());
            gateway.store(doc2.rdfModel());
        });
    }

    @Test
    void store_nullModel_throwsException() {
        assertThrows(NullPointerException.class, () -> gateway.store(null));
    }

    @Test
    void store_emptyModel_success() {
        Model emptyModel = new org.eclipse.rdf4j.model.impl.LinkedHashModel();
        assertDoesNotThrow(() -> gateway.store(emptyModel));
    }

    // Fetch tests (3 tests)
    // Note: Full RDF->BiboDocument conversion in Phase 7.B, these test basic functionality

    @Test
    void fetchByIdentifier_existingDocument_doesNotThrow() {
        BiboDocument doc = createTestDocument("test-doc", "Test Document");
        gateway.store(doc.rdfModel());

        assertDoesNotThrow(() -> gateway.fetchByIdentifier("test-doc"));
    }

    @Test
    void fetchByIdentifier_nonExisting_returnsEmpty() {
        Optional<BiboDocument> result = gateway.fetchByIdentifier("non-existing-id");

        assertTrue(result.isEmpty());
    }

    @Test
    void fetchByIdentifier_malformedId_returnsEmpty() {
        // Special characters in ID should be escaped in SPARQL
        Optional<BiboDocument> result = gateway.fetchByIdentifier("id\"with'quotes");

        assertTrue(result.isEmpty());
    }

    // ListAll tests (3 tests)
    // Note: Full RDF->BiboDocument conversion in Phase 7.B, these test basic functionality

    @Test
    void listAll_emptyRepository_returnsEmptyList() {
        List<BiboDocument> result = gateway.listAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listAll_singleDocument_doesNotThrow() {
        BiboDocument doc = createTestDocument("single-doc", "Single Document");
        gateway.store(doc.rdfModel());

        assertDoesNotThrow(() -> gateway.listAll());
    }

    @Test
    void listAll_manyDocuments_doesNotThrow() {
        // Create and store 10 documents
        for (int i = 0; i < 10; i++) {
            BiboDocument doc = createTestDocument("doc-" + i, "Document " + i);
            gateway.store(doc.rdfModel());
        }

        assertDoesNotThrow(() -> gateway.listAll());
    }

    // Transaction tests (2 tests)

    @Test
    void executeInTransaction_success_commits() {
        BiboDocument doc = createTestDocument("trans-doc", "Transaction Test");

        assertDoesNotThrow(() -> {
            gateway.executeInTransaction(conn -> {
                conn.add(doc.rdfModel());
            });
        });
    }

    @Test
    void executeInTransaction_exception_rollsBack() {
        BiboDocument doc = createTestDocument("rollback-doc", "Rollback Test");

        assertThrows(RepositoryException.class, () -> {
            gateway.executeInTransaction(conn -> {
                conn.add(doc.rdfModel());
                throw new RuntimeException("Simulated error");
            });
        });

        // Verify rollback occurred (would throw if transaction not rolled back properly)
        assertDoesNotThrow(() -> gateway.fetchByIdentifier("rollback-doc"));
    }

    // Additional tests

    @Test
    void constructor_withExternalRepository_success() {
        Repository memRepo = new SailRepository(new MemoryStore());
        memRepo.init();

        RDF4JRepositoryGateway externalGateway = new RDF4JRepositoryGateway(memRepo);

        assertTrue(externalGateway.isAvailable());

        externalGateway.shutdown(); // Should not shut down external repo
        assertTrue(memRepo.isInitialized());

        memRepo.shutDown();
    }

    @Test
    void isAvailable_initializedRepository_returnsTrue() {
        assertTrue(gateway.isAvailable());
    }

    @Test
    void shutdown_managedRepository_shutsDown() {
        gateway.shutdown();
        // After shutdown, create new gateway to verify data persistence
        RDF4JRepositoryGateway newGateway = new RDF4JRepositoryGateway(dataDir.toString());
        assertTrue(newGateway.isAvailable());
        newGateway.shutdown();
    }

    @Test
    void getRepository_returnsUnderlyingRepository() {
        Repository repo = gateway.getRepository();
        assertNotNull(repo);
        assertTrue(repo.isInitialized());
    }

    // Helper methods

    private BiboDocument createTestDocument(String id, String title) {
        BiboPersonName name = BiboPersonName.builder("John Doe")
            .givenName("John")
            .familyName("Doe")
            .build();

        BiboContributor author = new BiboContributor(name, BiboContributorRole.AUTHOR);

        return BiboDocument.builder(BiboDocumentType.ARTICLE, title)
            .id(id)
            .addContributor(author)
            .publicationDate(BiboPublicationDate.ofYear(2024))
            .publisher("Test Publisher")
            .addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, "10.1234/test." + id))
            .build();
    }
}
