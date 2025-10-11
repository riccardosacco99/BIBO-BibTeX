package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.Objects;
import java.util.Optional;
import org.jbibtex.BibTeXEntry;

public final class VocBenchPluginLifecycle {
    private final VocBenchPluginBootstrap bootstrap;
    private final VocBenchRepositoryGateway repositoryGateway;

    public VocBenchPluginLifecycle(
            VocBenchPluginBootstrap bootstrap, VocBenchRepositoryGateway repositoryGateway) {
        this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
        this.repositoryGateway = Objects.requireNonNull(repositoryGateway, "repositoryGateway");
    }

    public Optional<BiboDocument> importEntry(BibTeXEntry entry) {
        Optional<BiboDocument> document = bootstrap.importBibTeXEntry(entry);
        document.map(BiboDocument::rdfModel).ifPresent(repositoryGateway::store);
        return document;
    }

    public Optional<BibTeXEntry> exportDocument(String identifier) {
        return repositoryGateway.fetchByIdentifier(identifier).flatMap(bootstrap::exportDocument);
    }
}
