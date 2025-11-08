package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import it.riccardosacco.bibobibtex.model.bibo.BiboVocabulary;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
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

            // Convert RDF Model to BiboDocument
            return modelToBiboDocument(documentModel, subject);
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
            connection.getStatements(null, RDF.TYPE, BiboVocabulary.DOCUMENT)
                    .forEach(statement -> {
                        Resource subject = statement.getSubject();
                        Model documentModel = QueryResults.asModel(
                                connection.getStatements(subject, null, null));

                        expandBlankNodes(connection, documentModel);

                        // Convert model to BiboDocument
                        modelToBiboDocument(documentModel, subject).ifPresent(documents::add);
                    });
        }

        return documents;
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
     * Converts an RDF Model to a BiboDocument.
     *
     * @param model the RDF model containing document data
     * @param subject the subject resource representing the document
     * @return an Optional containing the BiboDocument if conversion succeeds
     */
    private Optional<BiboDocument> modelToBiboDocument(Model model, Resource subject) {
        try {
            // Determine document type
            BiboDocumentType type = extractDocumentType(model, subject);

            // Extract title (required)
            Optional<String> title = getLiteralValue(model, subject, DCTERMS.TITLE);
            if (title.isEmpty()) {
                return Optional.empty(); // Title is required
            }

            // Create builder
            BiboDocument.Builder builder = BiboDocument.builder(type, title.get());

            // Extract optional fields
            getLiteralValue(model, subject, DCTERMS.IDENTIFIER).ifPresent(builder::id);
            getLiteralValue(model, subject, BiboVocabulary.SUBTITLE).ifPresent(builder::subtitle);

            // Extract contributors from RDF Lists
            extractContributorsFromList(model, subject, BiboVocabulary.AUTHOR_LIST, BiboContributorRole.AUTHOR)
                    .forEach(builder::addContributor);
            extractContributorsFromList(model, subject, BiboVocabulary.EDITOR_LIST, BiboContributorRole.EDITOR)
                    .forEach(builder::addContributor);

            // Extract publication date
            extractPublicationDate(model, subject).ifPresent(builder::publicationDate);

            // Extract publication info
            getLiteralValue(model, subject, DCTERMS.PUBLISHER).ifPresent(builder::publisher);
            getLiteralValue(model, subject, DCTERMS.SPATIAL).ifPresent(builder::placeOfPublication);

            // Extract container title
            extractContainerTitle(model, subject).ifPresent(builder::containerTitle);

            // Extract volume/issue/pages
            getLiteralValue(model, subject, BiboVocabulary.VOLUME).ifPresent(builder::volume);
            getLiteralValue(model, subject, BiboVocabulary.ISSUE).ifPresent(builder::issue);
            getLiteralValue(model, subject, BiboVocabulary.PAGES).ifPresent(builder::pages);

            // Extract identifiers
            extractIdentifiers(model, subject).forEach(builder::addIdentifier);

            // Extract URL
            getIRIValue(model, subject, FOAF.PAGE).ifPresent(iri -> builder.url(iri.toString()));

            // Extract language, abstract, notes
            getLiteralValue(model, subject, DCTERMS.LANGUAGE).ifPresent(builder::language);
            getLiteralValue(model, subject, DCTERMS.ABSTRACT).ifPresent(builder::abstractText);
            getLiteralValue(model, subject, RDFS.COMMENT).ifPresent(builder::notes);

            // Extract extended fields (Sprint 01)
            getLiteralValue(model, subject, BiboVocabulary.SERIES).ifPresent(builder::series);
            getLiteralValue(model, subject, BiboVocabulary.EDITION).ifPresent(builder::edition);
            extractKeywords(model, subject).forEach(builder::addKeyword);
            getLiteralValue(model, subject, BiboVocabulary.CONTRIBUTOR).ifPresent(builder::organization);
            getLiteralValue(model, subject, DCTERMS.DESCRIPTION).ifPresent(builder::howPublished);

            // Extract degree type (Sprint 02)
            getLiteralValue(model, subject, BiboVocabulary.DEGREE).ifPresent(builder::degreeType);

            return Optional.of(builder.build());

        } catch (Exception e) {
            // Log error and return empty
            return Optional.empty();
        }
    }

    private BiboDocumentType extractDocumentType(Model model, Resource subject) {
        // Find the specific type (not just bibo:Document)
        for (Value typeValue : model.filter(subject, RDF.TYPE, null).objects()) {
            if (typeValue instanceof IRI) {
                BiboDocumentType type = BiboDocumentType.fromIri((IRI) typeValue);
                if (type != BiboDocumentType.OTHER) {
                    return type;
                }
            }
        }
        return BiboDocumentType.OTHER;
    }

    private Optional<String> getLiteralValue(Model model, Resource subject, IRI predicate) {
        for (Value value : model.filter(subject, predicate, null).objects()) {
            if (value instanceof Literal) {
                return Optional.of(((Literal) value).stringValue());
            }
        }
        return Optional.empty();
    }

    private Optional<IRI> getIRIValue(Model model, Resource subject, IRI predicate) {
        for (Value value : model.filter(subject, predicate, null).objects()) {
            if (value instanceof IRI) {
                return Optional.of((IRI) value);
            }
        }
        return Optional.empty();
    }

    private List<BiboContributor> extractContributorsFromList(Model model, Resource subject,
                                                               IRI listPredicate, BiboContributorRole role) {
        List<BiboContributor> contributors = new ArrayList<>();

        // Find the list head
        for (Value listHead : model.filter(subject, listPredicate, null).objects()) {
            if (listHead instanceof Resource) {
                extractListElements(model, (Resource) listHead, contributors, role);
            }
        }

        return contributors;
    }

    private void extractListElements(Model model, Resource listNode,
                                     List<BiboContributor> contributors, BiboContributorRole role) {
        // Get rdf:first (the contributor)
        for (Value first : model.filter(listNode, RDF.FIRST, null).objects()) {
            if (first instanceof Resource) {
                extractContributor(model, (Resource) first, role).ifPresent(contributors::add);
            }
        }

        // Get rdf:rest (next list node)
        for (Value rest : model.filter(listNode, RDF.REST, null).objects()) {
            if (rest instanceof Resource && !RDF.NIL.equals(rest)) {
                extractListElements(model, (Resource) rest, contributors, role);
            }
        }
    }

    private Optional<BiboContributor> extractContributor(Model model, Resource contributorNode, BiboContributorRole role) {
        Optional<String> name = getLiteralValue(model, contributorNode, FOAF.NAME);
        if (name.isEmpty()) {
            return Optional.empty();
        }

        BiboPersonName personName = BiboPersonName.builder(name.get()).build();
        return Optional.of(new BiboContributor(personName, role));
    }

    private Optional<BiboPublicationDate> extractPublicationDate(Model model, Resource subject) {
        Optional<String> dateStr = getLiteralValue(model, subject, DCTERMS.ISSUED);
        if (dateStr.isEmpty()) {
            return Optional.empty();
        }

        // Parse date string (format: "YYYY" or "YYYY-MM" or "YYYY-MM-DD")
        String[] parts = dateStr.get().split("-");
        try {
            int year = Integer.parseInt(parts[0]);
            if (parts.length == 1) {
                return Optional.of(BiboPublicationDate.ofYear(year));
            } else if (parts.length == 2) {
                int month = Integer.parseInt(parts[1]);
                return Optional.of(BiboPublicationDate.ofYearMonth(year, month));
            } else if (parts.length == 3) {
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                return Optional.of(BiboPublicationDate.ofFullDate(year, month, day));
            }
        } catch (NumberFormatException e) {
            // Invalid date format
        }

        return Optional.empty();
    }

    private Optional<String> extractContainerTitle(Model model, Resource subject) {
        // Find the container resource
        for (Value container : model.filter(subject, DCTERMS.IS_PART_OF, null).objects()) {
            if (container instanceof Resource) {
                return getLiteralValue(model, (Resource) container, DCTERMS.TITLE);
            }
        }
        return Optional.empty();
    }

    private List<BiboIdentifier> extractIdentifiers(Model model, Resource subject) {
        List<BiboIdentifier> identifiers = new ArrayList<>();

        // DOI
        getLiteralValue(model, subject, BiboVocabulary.DOI)
                .map(doi -> new BiboIdentifier(BiboIdentifierType.DOI, doi))
                .ifPresent(identifiers::add);

        // ISBN-10 and ISBN-13
        getLiteralValue(model, subject, BiboVocabulary.ISBN10)
                .map(isbn -> new BiboIdentifier(BiboIdentifierType.ISBN_10, isbn))
                .ifPresent(identifiers::add);
        getLiteralValue(model, subject, BiboVocabulary.ISBN13)
                .map(isbn -> new BiboIdentifier(BiboIdentifierType.ISBN_13, isbn))
                .ifPresent(identifiers::add);

        // ISSN
        getLiteralValue(model, subject, BiboVocabulary.ISSN)
                .map(issn -> new BiboIdentifier(BiboIdentifierType.ISSN, issn))
                .ifPresent(identifiers::add);

        // Handle
        getLiteralValue(model, subject, BiboVocabulary.HANDLE)
                .map(handle -> new BiboIdentifier(BiboIdentifierType.HANDLE, handle))
                .ifPresent(identifiers::add);

        // URI
        getIRIValue(model, subject, BiboVocabulary.URI)
                .map(uri -> new BiboIdentifier(BiboIdentifierType.URI, uri.toString()))
                .ifPresent(identifiers::add);

        return identifiers;
    }

    private List<String> extractKeywords(Model model, Resource subject) {
        List<String> keywords = new ArrayList<>();
        for (Value value : model.filter(subject, DCTERMS.SUBJECT, null).objects()) {
            if (value instanceof Literal) {
                keywords.add(((Literal) value).stringValue());
            }
        }
        return keywords;
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
