package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.LifterContext;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.LiftingException;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.RDFLifter;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ClosableFormattedResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RDF Lifter implementation that converts BibTeX files to BIBO RDF.
 */
public class BibTeXLifter implements RDFLifter {
    private static final Logger logger = LoggerFactory.getLogger(BibTeXLifter.class);
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Override
    public void lift(ClosableFormattedResource source, String format, RDFHandler handler, LifterContext context)
            throws LiftingException, IOException {

        logger.info("Starting BibTeX to BIBO lifting");
        try (InputStream is = source.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase database = parser.parse(reader);

            Model combinedModel = database.getEntries().values().stream()
                .map(converter::convertToBibo)
                .flatMap(Optional::stream)
                .map(BiboDocument::rdfModel)
                .reduce(new LinkedHashModel(), (m1, m2) -> {
                    m1.addAll(m2);
                    return m1;
                });

            handler.startRDF();
            for (Statement st : combinedModel) {
                handler.handleStatement(st);
            }
            handler.endRDF();

            logger.info("Lifting complete: {} triples generated", combinedModel.size());

        } catch (ParseException | TokenMgrException | RDFHandlerException e) {
            logger.error("Failed to lift BibTeX content", e);
            throw new LiftingException("BibTeX parsing or RDF generation failed", e);
        }
    }
}
