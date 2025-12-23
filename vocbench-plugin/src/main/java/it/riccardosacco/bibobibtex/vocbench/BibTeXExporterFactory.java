package it.riccardosacco.bibobibtex.vocbench;

import it.uniroma2.art.semanticturkey.config.Configuration;
import it.uniroma2.art.semanticturkey.extension.ConfigurableExtensionFactory;
import it.uniroma2.art.semanticturkey.extension.extpts.commons.io.FormatCapabilityProvider;
import it.uniroma2.art.semanticturkey.resources.DataFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Factory for creating BibTeXExporter instances.
 * This factory is discovered by Spring's component scanning in the Semantic Turkey framework.
 */
@Component
public class BibTeXExporterFactory implements
        ConfigurableExtensionFactory<BibTeXExporter, BibTeXExporterFactory.EmptyConfiguration>,
        FormatCapabilityProvider {

    /**
     * Empty configuration class for the exporter.
     */
    public static class EmptyConfiguration implements Configuration {
        @Override
        public String getShortName() {
            return "BibTeX Exporter Configuration";
        }
    }

    public static class MessageKeys {
        public static final String keyBase = "it.riccardosacco.bibobibtex.vocbench.BibTeXExporterFactory";
        private static final String name = keyBase + ".name";
        private static final String description = keyBase + ".description";
    }

    @Override
    public String getName() {
        return "BibTeX Exporter";
    }

    @Override
    public String getDescription() {
        return "Exports BIBO RDF to BibTeX format";
    }

    @Override
    public BibTeXExporter createInstance(EmptyConfiguration config) {
        return new BibTeXExporter();
    }

    @Override
    public Collection<EmptyConfiguration> getConfigurations() {
        return Collections.singletonList(new EmptyConfiguration());
    }

    @Override
    public List<DataFormat> getFormats() {
        return Arrays.asList(new DataFormat("BibTeX", "application/x-bibtex", "bib"));
    }
}
