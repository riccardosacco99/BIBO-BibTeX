package it.riccardosacco.bibobibtex.sparql;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CustomSearchesTest {

    private Repository repository;
    private RepositoryConnection connection;
    private Path projectRoot;

    @BeforeEach
    public void setUp() throws IOException {
        // Initialize in-memory repository
        repository = new SailRepository(new MemoryStore());
        repository.init();
        connection = repository.getConnection();

        // Determine project root to locate config files
        // Assuming test runs in 'core' module directory, so root is one level up
        Path moduleRoot = Paths.get("").toAbsolutePath();
        if (moduleRoot.endsWith("core")) {
            projectRoot = moduleRoot.getParent();
        } else {
            projectRoot = moduleRoot; // Running from root
        }

        // Load test data
        Path testDataPath = projectRoot.resolve("test-data/sparql-test/bad-data.ttl");
        if (!Files.exists(testDataPath)) {
            fail("Test data not found at: " + testDataPath);
        }
        connection.add(testDataPath.toFile(), "http://example.org/", RDFFormat.TURTLE);
    }

    @AfterEach
    public void tearDown() {
        connection.close();
        repository.shutDown();
    }

    private String readQuery(String filename) throws IOException {
        Path queryPath = projectRoot.resolve("vocbench-config/custom-searches/" + filename);
        if (!Files.exists(queryPath)) {
            fail("Query file not found at: " + queryPath);
        }
        return Files.readString(queryPath);
    }

    @Test
    public void testDocumentsWithoutIdentifier() throws IOException {
        String query = readQuery("documents_without_identifier.rq");
        List<BindingSet> results = executeQuery(query);

        assertEquals(2, results.size(), "Should find exactly 2 documents without identifier");
        
        List<String> titles = results.stream()
                .map(bs -> bs.getValue("title").stringValue())
                .toList();
        
        assertTrue(titles.contains("Document Without ID"));
        assertTrue(titles.contains("Document With Note Overflow"));
    }

    @Test
    public void testDocumentsWithoutAuthors() throws IOException {
        String query = readQuery("documents_without_authors.rq");
        List<BindingSet> results = executeQuery(query);

        assertEquals(3, results.size(), "Should find exactly 3 documents without authors");
        
        List<String> titles = results.stream()
                .map(bs -> bs.getValue("title").stringValue())
                .toList();
        
        assertTrue(titles.contains("Document Without Authors"));
        assertTrue(titles.contains("Document Without ID"));
        assertTrue(titles.contains("Document With Note Overflow"));
    }

    @Test
    public void testIncompletePersons() throws IOException {
        String query = readQuery("incomplete_persons.rq");
        List<BindingSet> results = executeQuery(query);

        assertEquals(1, results.size(), "Should find exactly 1 incomplete person");
        String name = results.get(0).getValue("name").stringValue();
        assertEquals("Incomplete Person Name", name);
    }

    @Test
    public void testDocumentsWithNoteOverflow() throws IOException {
        String query = readQuery("documents_with_note_overflow.rq");
        List<BindingSet> results = executeQuery(query);

        assertEquals(1, results.size(), "Should find exactly 1 document with note overflow");
        String title = results.get(0).getValue("title").stringValue();
        assertEquals("Document With Note Overflow", title);
        
        String note = results.get(0).getValue("note").stringValue();
        assertTrue(note.contains("Series:"));
    }

    private List<BindingSet> executeQuery(String queryString) {
        TupleQuery query = connection.prepareTupleQuery(queryString);
        List<BindingSet> resultList = new ArrayList<>();
        try (TupleQueryResult result = query.evaluate()) {
            while (result.hasNext()) {
                resultList.add(result.next());
            }
        }
        return resultList;
    }
}
