package it.riccardosacco.bibobibtex.vocbench;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ClosableFormattedResource;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ExporterContext;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ReformattingException;
import it.uniroma2.art.semanticturkey.extension.extpts.reformattingexporter.ReformattingExporter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reformatting Exporter implementation that converts BIBO RDF to BibTeX format.
 */
public class BibTeXExporter implements ReformattingExporter {
    private static final Logger logger = LoggerFactory.getLogger(BibTeXExporter.class);
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Override
    public ClosableFormattedResource export(RepositoryConnection conn, IRI[] graphs, String format,
            ExporterContext context) throws ReformattingException, IOException {

        logger.info("Starting BIBO to BibTeX export");
        File tempFile = Files.createTempFile("export", ".bib").toFile();

        try {
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
}
