package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Optional;
import org.jbibtex.BibTeXEntry;

public final class VocBenchPluginLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(VocBenchPluginLifecycle.class);

    private final VocBenchPluginBootstrap bootstrap;
    private final VocBenchRepositoryGateway repositoryGateway;

    public VocBenchPluginLifecycle(
            VocBenchPluginBootstrap bootstrap, VocBenchRepositoryGateway repositoryGateway) {
        this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
        this.repositoryGateway = Objects.requireNonNull(repositoryGateway, "repositoryGateway");
        logger.info("VocBench plugin lifecycle initialized");
        logger.debug("Bootstrap: {}, Repository gateway: {}", bootstrap.getClass().getSimpleName(),
            repositoryGateway.getClass().getSimpleName());
    }

    public Optional<BiboDocument> importEntry(BibTeXEntry entry) {
        logger.info("Importing BibTeX entry into VocBench repository");
        String citationKey = entry.getKey() != null ? entry.getKey().getValue() : "unknown";
        logger.debug("Processing entry with citation key: {}", citationKey);

        Optional<BiboDocument> document = bootstrap.importBibTeXEntry(entry);
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
}
