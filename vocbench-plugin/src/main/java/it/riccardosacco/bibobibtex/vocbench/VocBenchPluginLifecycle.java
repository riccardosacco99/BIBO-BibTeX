package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.jbibtex.BibTeXEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VocBenchPluginLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(VocBenchPluginLifecycle.class);

    private final VocBenchPluginBootstrap bootstrap;
    private final VocBenchRepositoryGateway repositoryGateway;
    private final VocBenchPluginConfiguration configuration;

    public VocBenchPluginLifecycle(
            VocBenchPluginBootstrap bootstrap, VocBenchRepositoryGateway repositoryGateway) {
        this(bootstrap, repositoryGateway, null);
    }

    public VocBenchPluginLifecycle(
            VocBenchPluginBootstrap bootstrap,
            VocBenchRepositoryGateway repositoryGateway,
            VocBenchPluginConfiguration configuration) {
        this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
        this.repositoryGateway = Objects.requireNonNull(repositoryGateway, "repositoryGateway");
        this.configuration = configuration;
        logger.info("VocBench plugin lifecycle initialized");
        logger.debug("Bootstrap: {}, Repository gateway: {}", bootstrap.getClass().getSimpleName(),
            repositoryGateway.getClass().getSimpleName());
    }

    public static VocBenchPluginLifecycle createDefault() {
        VocBenchPluginConfiguration configuration = new VocBenchPluginConfiguration();
        VocBenchRepositoryGateway gateway = createGateway(configuration);
        return new VocBenchPluginLifecycle(new VocBenchPluginBootstrap(), gateway, configuration);
    }

    private static VocBenchRepositoryGateway createGateway(VocBenchPluginConfiguration configuration) {
        return switch (configuration.getRepositoryType()) {
            case NATIVE -> new RDF4JRepositoryGateway(configuration.getRepositoryDataDir());
            case MEMORY -> {
                SailRepository repository = new SailRepository(new MemoryStore());
                yield new RDF4JRepositoryGateway(repository);
            }
        };
    }

    public Optional<BiboDocument> importEntry(BibTeXEntry entry) {
        logger.info("Importing BibTeX entry into VocBench repository");
        String citationKey = entry.getKey() != null ? entry.getKey().getValue() : "unknown";
        logger.debug("Processing entry with citation key: {}", citationKey);

        Optional<BiboDocument> document = bootstrap.importBibTeXEntry(entry).map(this::applyNamespacePrefix);
        if (document.isPresent()) {
            logger.debug("Successfully converted BibTeX entry, storing in repository");
            document.map(BiboDocument::rdfModel).ifPresent(model -> {
                repositoryGateway.store(model);
                logger.info("Document stored in VocBench repository: {}", document.get().title());
            });
        } else {
            logger.warn("Failed to convert BibTeX entry: {}", citationKey);
        }
        return document;
    }

    public Optional<BibTeXEntry> exportDocument(String identifier) {
        logger.info("Exporting document from VocBench repository: {}", identifier);
        logger.debug("Fetching document by identifier");

        Optional<BibTeXEntry> result = repositoryGateway.fetchByIdentifier(identifier)
            .flatMap(model -> {
                logger.debug("Document found, converting to BibTeX");
                return bootstrap.exportDocument(model);
            });

        if (result.isPresent()) {
            logger.info("Successfully exported document: {}", identifier);
        } else {
            logger.warn("Document not found or export failed: {}", identifier);
        }

        return result;
    }

    private BiboDocument applyNamespacePrefix(BiboDocument document) {
        if (configuration == null) {
            return document;
        }
        String namespace = configuration.getNamespacePrefix();
        if (namespace == null || namespace.isBlank()) {
            return document;
        }

        Optional<String> existingId = document.id();
        if (existingId.isPresent() && isAbsoluteIri(existingId.get())) {
            return document;
        }

        String localId = existingId.filter(id -> !id.isBlank()).orElseGet(() -> slugify(document.title()));
        String identifier = namespace + localId;
        if (existingId.isPresent() && existingId.get().equals(identifier)) {
            return document;
        }

        return cloneDocumentWithId(document, identifier);
    }

    private static boolean isAbsoluteIri(String identifier) {
        try {
            URI uri = URI.create(identifier);
            return uri.isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }

    private static String slugify(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "document" : normalized;
    }

    private static BiboDocument cloneDocumentWithId(BiboDocument original, String identifier) {
        BiboDocument.Builder builder = BiboDocument.builder(original.type(), original.title()).id(identifier);
        original.subtitle().ifPresent(builder::subtitle);
        builder.contributors(original.contributors());
        original.publicationDate().ifPresent(builder::publicationDate);
        original.publisher().ifPresent(builder::publisher);
        original.placeOfPublication().ifPresent(builder::placeOfPublication);
        original.conferenceLocation().ifPresent(builder::conferenceLocation);
        original.conferenceOrganizer().ifPresent(builder::conferenceOrganizer);
        original.degreeType().ifPresent(builder::degreeType);
        original.containerTitle().ifPresent(builder::containerTitle);
        original.volume().ifPresent(builder::volume);
        original.issue().ifPresent(builder::issue);
        original.pages().ifPresent(builder::pages);
        original.identifiers().forEach(builder::addIdentifier);
        original.url().ifPresent(builder::url);
        original.language().ifPresent(builder::language);
        original.abstractText().ifPresent(builder::abstractText);
        original.notes().ifPresent(builder::notes);
        original.series().ifPresent(builder::series);
        original.edition().ifPresent(builder::edition);
        if (!original.keywords().isEmpty()) {
            builder.keywords(original.keywords());
        }
        return builder.build();
    }
}
