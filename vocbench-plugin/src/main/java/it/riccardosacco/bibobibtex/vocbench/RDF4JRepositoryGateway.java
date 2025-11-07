package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboVocabulary;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * RDF4J-based implementation of the repository gateway for storing and retrieving BIBO documents.
 *
 * <p>This implementation uses an RDF4J Repository to persist and query bibliographic data.
 * It supports in-memory and persistent repositories.</p>
 *
 * @since Sprint 01 - US-01
 */
public class RDF4JRepositoryGateway implements VocBenchRepositoryGateway {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private final Repository repository;

    /**
     * Creates a new gateway with the specified RDF4J repository.
     *
     * @param repository the RDF4J repository to use for storage
     * @throws NullPointerException if repository is null
     */
    public RDF4JRepositoryGateway(Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    /**
     * Stores an RDF model in the repository within a transaction.
     *
     * @param model the RDF model to store
     * @throws RepositoryException if the store operation fails
     * @throws NullPointerException if model is null
     */
    @Override
    public void store(Model model) {
        Objects.requireNonNull(model, "model cannot be null");

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.begin();
            try {
                connection.add(model);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RepositoryException("Failed to store model in repository", e);
            }
        }
    }

    /**
     * Fetches a BIBO document by its identifier (dcterms:identifier).
     *
     * <p>This method constructs a SPARQL query to retrieve all statements about the document
     * with the given identifier.</p>
     *
     * @param identifier the document identifier to search for
     * @return an Optional containing the document if found, empty otherwise
     * @throws RepositoryException if the fetch operation fails
     */
    @Override
    public Optional<BiboDocument> fetchByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            // Find the subject(s) with this identifier
            List<Resource> subjects = new ArrayList<>();

            connection.getStatements(null, DCTERMS.IDENTIFIER, VF.createLiteral(identifier))
                    .forEach(statement -> subjects.add(statement.getSubject()));

            if (subjects.isEmpty()) {
                return Optional.empty();
            }

            // Get all statements about the first matching subject
            Resource subject = subjects.get(0);
            Model documentModel = QueryResults.asModel(
                    connection.getStatements(subject, null, null));

            // Also get statements about any blank nodes referenced (for nested structures)
            expandBlankNodes(connection, documentModel);

            if (documentModel.isEmpty()) {
                return Optional.empty();
            }

            // For this MVP, we return the raw model wrapped
            // In a full implementation, we would reconstruct a BiboDocument from the model
            // For now, we return empty as the model-to-BiboDocument conversion is not yet implemented
            return Optional.empty(); // TODO: Implement model-to-BiboDocument conversion
        }
    }

    /**
     * Lists all BIBO documents in the repository.
     *
     * <p>This method finds all resources typed as bibo:Document and returns them as BiboDocuments.</p>
     *
     * @return a list of all documents (may be empty)
     * @throws RepositoryException if the query fails
     */
    @Override
    public List<BiboDocument> listAll() {
        List<BiboDocument> documents = new ArrayList<>();

        try (RepositoryConnection connection = repository.getConnection()) {
            // Find all resources typed as bibo:Document
            connection.getStatements(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BiboVocabulary.DOCUMENT)
                    .forEach(statement -> {
                        Resource subject = statement.getSubject();
                        Model documentModel = QueryResults.asModel(
                                connection.getStatements(subject, null, null));

                        expandBlankNodes(connection, documentModel);

                        // TODO: Convert model to BiboDocument
                        // For now, we skip since model-to-BiboDocument conversion is not yet implemented
                    });
        }

        return documents; // Currently returns empty list
    }

    /**
     * Recursively expands blank nodes referenced in the model.
     *
     * <p>This is necessary for retrieving nested structures like author lists.</p>
     *
     * @param connection the repository connection
     * @param model the model to expand
     */
    private void expandBlankNodes(RepositoryConnection connection, Model model) {
        // Find all blank node objects in the current model
        model.objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .filter(Resource::isBNode)
                .forEach(blankNode -> {
                    // Add all statements about this blank node
                    connection.getStatements(blankNode, null, null)
                            .forEach(model::add);
                });
    }

    /**
     * Closes the repository connection.
     * This should be called when the gateway is no longer needed.
     */
    public void shutdown() {
        if (repository != null) {
            repository.shutDown();
        }
    }
}
