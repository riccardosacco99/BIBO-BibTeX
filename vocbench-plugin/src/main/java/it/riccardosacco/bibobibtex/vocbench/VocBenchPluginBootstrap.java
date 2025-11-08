package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.jbibtex.BibTeXEntry;

/**
 * Minimal bootstrap component for integrating the core converter inside a VocBench plugin.
 *
 * <p>This class hides the concrete converter implementation and exposes operations that fit the
 * lifecycle of a VocBench import/export plugin. Configuration is loaded from system properties
 * or can be provided programmatically.</p>
 *
 * <p>Updated in Sprint 01 - US-07: Added configuration support.</p>
 */
public class VocBenchPluginBootstrap {
    private final BibTeXBibliographicConverter converter;
    private final VocBenchPluginConfiguration configuration;

    public VocBenchPluginBootstrap() {
        this(new BibTeXBibliographicConverter(), VocBenchPluginConfiguration.fromSystemProperties());
    }

    public VocBenchPluginBootstrap(BibTeXBibliographicConverter converter) {
        this(converter, VocBenchPluginConfiguration.fromSystemProperties());
    }

    public VocBenchPluginBootstrap(BibTeXBibliographicConverter converter, VocBenchPluginConfiguration configuration) {
        this.converter = converter;
        this.configuration = configuration;
    }

    public Optional<BiboDocument> importBibTeXEntry(BibTeXEntry entry) {
        return converter.convertToBibo(entry);
    }

    public Optional<BibTeXEntry> exportDocument(BiboDocument document) {
        return converter.convertFromBibo(document);
    }

    public Model projectModel(BiboDocument document) {
        return document.rdfModel();
    }

    public VocBenchPluginConfiguration getConfiguration() {
        return configuration;
    }
}
