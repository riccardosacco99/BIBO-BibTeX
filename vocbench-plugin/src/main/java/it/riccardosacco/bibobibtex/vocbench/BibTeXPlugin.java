package it.riccardosacco.bibobibtex.vocbench;

import it.uniroma2.art.semanticturkey.pf4j.STPlugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the VocBench BIBO-BibTeX Plugin.
 *
 * <p>This plugin provides bidirectional conversion between BibTeX and BIBO RDF.
 * Extensions are discovered via Spring component scanning of the Factory classes:
 * <ul>
 *   <li>{@link BibTeXLifterFactory} - RDF Lifter for importing BibTeX files</li>
 *   <li>{@link BibTeXExporterFactory} - Reformatting Exporter for exporting to BibTeX</li>
 * </ul>
 */
public class BibTeXPlugin extends STPlugin {

    private static final Logger logger = LoggerFactory.getLogger(BibTeXPlugin.class);

    public BibTeXPlugin(PluginWrapper wrapper) {
        super(wrapper);
        logger.info("BIBO-BibTeX Plugin initialized");
    }
}
