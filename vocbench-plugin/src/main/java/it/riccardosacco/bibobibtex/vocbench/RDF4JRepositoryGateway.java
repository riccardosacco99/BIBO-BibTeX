package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * RDF4J-based implementation of VocBench repository gateway.
 *
 * Provides persistent storage using Native Store with transaction support,
 * error handling, and logging.
 */
public class RDF4JRepositoryGateway implements VocBenchRepositoryGateway {
    private static final Logger logger = LoggerFactory.getLogger(RDF4JRepositoryGateway.class);

    private final Repository repository;
    private final boolean managedLifecycle;
    private final BibTeXBibliographicConverter converter;

    /**
     * Creates gateway with existing repository.
     *
     * @param repository RDF4J repository
     */
    public RDF4JRepositoryGateway(Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.managedLifecycle = false;
        this.converter = new BibTeXBibliographicConverter();

        if (!repository.isInitialized()) {
            repository.init();
        }

        logger.info("RDF4J repository gateway initialized with external repository");
        logger.debug("Repository type: {}", repository.getClass().getSimpleName());
    }

    /**
     * Creates gateway with Native Store at specified directory.
     *
     * @param dataDir directory for Native Store data
     */
    public RDF4JRepositoryGateway(String dataDir) {
        Objects.requireNonNull(dataDir, "dataDir");

        File dataDirFile = new File(dataDir);
        logger.info("Creating Native Store repository at: {}", dataDirFile.getAbsolutePath());

        NativeStore nativeStore = new NativeStore(dataDirFile);
        this.repository = new SailRepository(nativeStore);
        this.managedLifecycle = true;
        this.converter = new BibTeXBibliographicConverter();

        try {
            repository.init();
            logger.info("Native Store repository initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Native Store repository", e);
            throw new RepositoryException("Failed to initialize repository at " + dataDir, e);
        }
    }

    @Override
    public void store(Model model) {
        Objects.requireNonNull(model, "model");

        logger.info("Storing RDF model with {} statements", model.size());
        logger.debug("Model subjects: {}", model.subjects().size());

        try {
            executeInTransaction(conn -> {
                conn.add(model);
                logger.debug("Model added to repository successfully");
            });
            logger.info("RDF model stored successfully");
        } catch (Exception e) {
            logger.error("Failed to store RDF model", e);
            throw new RepositoryException("Failed to store model", e);
        }
    }

    @Override
    public Optional<BiboDocument> fetchByIdentifier(String identifier) {
        Objects.requireNonNull(identifier, "identifier");

        logger.info("Fetching document by identifier: {}", identifier);

        // SPARQL CONSTRUCT query to extract all triples related to the document
        String sparqlQuery = """
            PREFIX dcterms: <http://purl.org/dc/terms/>
            CONSTRUCT {
                ?doc ?p ?o .
                ?related ?rp ?ro .
            }
            WHERE {
                ?doc dcterms:identifier ?id .
                FILTER(str(?id) = "%s")
                ?doc ?p ?o .
                OPTIONAL {
                    ?doc ?rel ?related .
                    FILTER(isBlank(?related) || (isIRI(?related) && ?rel = dcterms:isPartOf))
                    ?related ?rp ?ro .
                }
            }
            """.formatted(escapeSparqlString(identifier));

        try (RepositoryConnection conn = repository.getConnection()) {
            Model model = new LinkedHashModel();
            try (GraphQueryResult result = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate()) {
                while (result.hasNext()) {
                    model.add(result.next());
                }
            }

            if (model.isEmpty()) {
                logger.warn("No document found with identifier: {}", identifier);
                return Optional.empty();
            }

            logger.debug("Retrieved {} statements for document: {}", model.size(), identifier);

            List<BiboDocument> documents = converter.convertAllFromRDF(model);
            if (documents.isEmpty()) {
                logger.warn("Could not convert RDF to BiboDocument for identifier: {}", identifier);
                return Optional.empty();
            }

            logger.info("Successfully fetched and converted document: {}", identifier);
            return Optional.of(documents.getFirst());
        } catch (Exception e) {
            logger.error("Failed to fetch document by identifier: {}", identifier, e);
            throw new RepositoryException("Failed to fetch document: " + identifier, e);
        }
    }

    @Override
    public List<BiboDocument> listAll() {
        logger.info("Listing all documents in repository");

        // SPARQL CONSTRUCT query to extract all BIBO documents with their related data
        String sparqlQuery = """
            PREFIX bibo: <http://purl.org/ontology/bibo/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX dcterms: <http://purl.org/dc/terms/>
            CONSTRUCT {
                ?doc ?p ?o .
                ?related ?rp ?ro .
            }
            WHERE {
                ?doc rdf:type ?type .
                FILTER(STRSTARTS(STR(?type), STR(bibo:)))
                ?doc ?p ?o .
                OPTIONAL {
                    ?doc ?rel ?related .
                    FILTER(isBlank(?related) || (isIRI(?related) && ?rel = dcterms:isPartOf))
                    ?related ?rp ?ro .
                }
            }
            """;

        try (RepositoryConnection conn = repository.getConnection()) {
            Model model = new LinkedHashModel();
            try (GraphQueryResult result = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate()) {
                while (result.hasNext()) {
                    model.add(result.next());
                }
            }

            if (model.isEmpty()) {
                logger.info("No documents found in repository");
                return List.of();
            }

            logger.debug("Retrieved {} statements from repository", model.size());

            List<BiboDocument> documents = converter.convertAllFromRDF(model);
            logger.info("Successfully converted {} documents from repository", documents.size());
            return documents;
        } catch (Exception e) {
            logger.error("Failed to list all documents", e);
            throw new RepositoryException("Failed to list documents", e);
        }
    }

    /**
     * Executes operation in transaction with auto-rollback on failure.
     *
     * @param operation operation to execute
     * @throws RepositoryException if transaction fails
     */
    public void executeInTransaction(Consumer<RepositoryConnection> operation) {
        Objects.requireNonNull(operation, "operation");

        logger.debug("Starting transaction");

        try (RepositoryConnection conn = repository.getConnection()) {
            try {
                conn.begin();
                logger.debug("Transaction started");

                operation.accept(conn);

                conn.commit();
                logger.debug("Transaction committed successfully");
            } catch (Exception e) {
                if (conn.isActive()) {
                    conn.rollback();
                    logger.warn("Transaction rolled back due to error", e);
                }
                throw e;
            }
        } catch (Exception e) {
            logger.error("Transaction failed", e);
            throw new RepositoryException("Transaction failed", e);
        }
    }

    /**
     * Shuts down repository if lifecycle is managed.
     */
    public void shutdown() {
        if (managedLifecycle && repository.isInitialized()) {
            logger.info("Shutting down managed repository");
            repository.shutDown();
            logger.info("Repository shutdown complete");
        }
    }

    /**
     * Returns underlying repository for advanced operations.
     *
     * @return RDF4J repository
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Checks if repository is initialized and accessible.
     *
     * @return true if repository is ready
     */
    public boolean isAvailable() {
        try {
            return repository.isInitialized();
        } catch (Exception e) {
            logger.warn("Repository availability check failed", e);
            return false;
        }
    }

    /**
     * Escapes a string for safe use in SPARQL queries.
     * Handles backslashes, quotes, newlines, and other special characters.
     *
     * @param value the string to escape
     * @return the escaped string safe for SPARQL
     */
    private static String escapeSparqlString(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
