package it.riccardosacco.bibobibtex.vocbench;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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

    /**
     * Creates gateway with existing repository.
     *
     * @param repository RDF4J repository
     */
    public RDF4JRepositoryGateway(Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.managedLifecycle = false;

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
    public Optional<it.riccardosacco.bibobibtex.model.bibo.BiboDocument> fetchByIdentifier(String identifier) {
        Objects.requireNonNull(identifier, "identifier");

        logger.info("Fetching document by identifier: {}", identifier);

        // TODO: Full RDF -> BiboDocument conversion will be implemented in Phase 7.B
        // For now, this is a placeholder that verifies the document exists

        String sparqlQuery = "ASK WHERE { ?doc <%s> ?id . FILTER(str(?id) = \"%s\") }".formatted(
                DCTERMS.IDENTIFIER,
                escapeSparqlString(identifier)
        );

        try (RepositoryConnection conn = repository.getConnection()) {
            boolean exists = conn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();

            if (!exists) {
                logger.warn("No document found with identifier: {}", identifier);
                return Optional.empty();
            }

            logger.info("Document found (placeholder returned)");
            logger.warn("Full RDF->BiboDocument conversion not yet implemented (Phase 7.B)");
            return Optional.empty(); // Placeholder until Phase 7.B
        } catch (Exception e) {
            logger.error("Failed to fetch document by identifier: {}", identifier, e);
            throw new RepositoryException("Failed to fetch document: " + identifier, e);
        }
    }

    @Override
    public List<it.riccardosacco.bibobibtex.model.bibo.BiboDocument> listAll() {
        logger.info("Listing all documents in repository");

        // TODO: Full RDF -> BiboDocument conversion will be implemented in Phase 7.B
        // For now, return empty list

        String sparqlQuery = "SELECT (COUNT(DISTINCT ?doc) AS ?count) WHERE { ?doc <%s> ?type }".formatted(
                RDF.TYPE
        );

        try (RepositoryConnection conn = repository.getConnection()) {
            long count = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
                .evaluate()
                .stream()
                .findFirst()
                .map(bindings -> bindings.getValue("count"))
                .map(value -> Long.parseLong(value.stringValue()))
                .orElse(0L);

            logger.info("Found {} documents in repository (placeholder returned empty list)", count);
            logger.warn("Full RDF->BiboDocument conversion not yet implemented (Phase 7.B)");
            return List.of(); // Placeholder until Phase 7.B
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
