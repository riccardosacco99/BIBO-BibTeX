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
 * lifecycle of a VocBench import/export plugin. The actual wiring toward VocBench services (project
 * repository access, UI actions, configuration) will be addressed in future iterations.</p>
 */
public class VocBenchPluginBootstrap {
    private final BibTeXBibliographicConverter converter;

    public VocBenchPluginBootstrap() {
        this(new BibTeXBibliographicConverter());
    }

    public VocBenchPluginBootstrap(BibTeXBibliographicConverter converter) {
        this.converter = converter;
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
}
