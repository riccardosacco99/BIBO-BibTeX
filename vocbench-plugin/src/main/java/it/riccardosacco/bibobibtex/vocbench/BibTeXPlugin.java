package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.LifterContext;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.LiftingException;
import it.uniroma2.art.semanticturkey.extension.extpts.rdflifter.RDFLifter;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ClosableFormattedResource;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ExporterContext;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ReformattingException;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ReformattingExporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the VocBench BIBO-BibTeX Plugin (PF4J version).
 */
public class BibTeXPlugin extends Plugin {

    public BibTeXPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class BibTeXLifter implements RDFLifter {
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

    @Extension
    public static class BibTeXExporter implements ReformattingExporter {
        private static final Logger logger = LoggerFactory.getLogger(BibTeXExporter.class);
        private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

        @Override
        public ClosableFormattedResource export(RepositoryConnection conn, IRI[] graphs, String format, ExporterContext context)
                throws ReformattingException, IOException {
             
            logger.info("Starting BIBO to BibTeX export");
            File tempFile = Files.createTempFile("export", ".bib").toFile();
            
            try {
                // Extract model from repository
                // We fetch all statements from the specified graphs (or all if graphs is empty)
                // Note: This loads the entire graph into memory. For very large datasets, pagination might be needed,
                // but BIBO conversion logic currently requires object traversal which is hard to paginate without object mapping.
                
                Model model;
                try (RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, graphs)) {
                    model = QueryResults.asModel(statements);
                }

                List<BiboDocument> documents = converter.convertAllFromRDF(model);
                List<BibTeXEntry> entries = documents.stream()
                        .map(converter::convertFromBibo)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());

                BibTeXDatabase database = new BibTeXDatabase();
                entries.forEach(database::addObject);

                try (Writer writer = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8)) {
                    BibTeXFormatter formatter = new BibTeXFormatter();
                    formatter.format(database, writer);
                }
                
                logger.info("Exported {} documents to BibTeX", entries.size());
                
                return new ClosableFormattedResource(
                        tempFile, 
                        "application/x-bibtex", 
                        "bib", 
                        StandardCharsets.UTF_8, 
                        "export.bib"
                );
                
            } catch (Exception e) {
                 logger.error("Failed to export to BibTeX", e);
                 throw new ReformattingException("BibTeX export failed", e);
            }
        }
        
        // Note: getOutputFormats() is not in the interface, it's defined in the extension configuration usually.
        // If the interface required it, the compiler would have complained. 
        // javap didn't show it, so I remove it.
    }
}