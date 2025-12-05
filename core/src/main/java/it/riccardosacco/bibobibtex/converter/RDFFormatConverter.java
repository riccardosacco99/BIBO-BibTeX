package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility for converting BIBO documents to and from various RDF formats.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>Turtle (.ttl)</li>
 *   <li>JSON-LD (.jsonld)</li>
 *   <li>RDF/XML (.rdf, .xml)</li>
 *   <li>N-Triples (.nt)</li>
 *   <li>N-Quads (.nq)</li>
 * </ul>
 */
public final class RDFFormatConverter {

    private final BibTeXBibliographicConverter converter;

    public RDFFormatConverter() {
        this.converter = new BibTeXBibliographicConverter();
    }

    public RDFFormatConverter(BibTeXBibliographicConverter converter) {
        this.converter = Objects.requireNonNull(converter, "converter");
    }

    /**
     * Exports a document in the specified RDF format.
     *
     * @param document the document to export
     * @param format   the RDF format to use
     * @return the serialized RDF content
     */
    public String exportAs(BiboDocument document, RDFFormat format) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(format, "format");

        StringWriter writer = new StringWriter();
        document.write(writer, format);
        return writer.toString();
    }

    /**
     * Exports multiple documents in the specified RDF format.
     *
     * @param documents the documents to export
     * @param format    the RDF format to use
     * @return the serialized RDF content containing all documents
     */
    public String exportAllAs(List<BiboDocument> documents, RDFFormat format) {
        Objects.requireNonNull(documents, "documents");
        Objects.requireNonNull(format, "format");

        // Combine all models
        Model combinedModel = new org.eclipse.rdf4j.model.impl.LinkedHashModel();
        for (BiboDocument doc : documents) {
            combinedModel.addAll(doc.rdfModel());
        }

        StringWriter writer = new StringWriter();
        Rio.write(combinedModel, writer, format);
        return writer.toString();
    }

    /**
     * Imports documents from RDF content.
     *
     * @param rdfContent the RDF content to parse
     * @param format     the RDF format of the content
     * @return list of parsed documents
     * @throws it.riccardosacco.bibobibtex.exception.BibliographicConversionException if parsing fails
     */
    public List<BiboDocument> importFrom(String rdfContent, RDFFormat format) {
        Objects.requireNonNull(rdfContent, "rdfContent");
        Objects.requireNonNull(format, "format");

        try {
            Model model = Rio.parse(new StringReader(rdfContent), "", format);
            return converter.convertAllFromRDF(model);
        } catch (Exception e) {
            throw new it.riccardosacco.bibobibtex.exception.BibliographicConversionException(
                    "Failed to parse RDF content", e);
        }
    }

    /**
     * Detects the RDF format from a filename.
     *
     * @param filename the filename to analyze
     * @return the detected format, or empty if unknown
     */
    public static Optional<RDFFormat> detectFormat(String filename) {
        if (filename == null || filename.isBlank()) {
            return Optional.empty();
        }

        String lower = filename.toLowerCase();

        if (lower.endsWith(".ttl") || lower.endsWith(".turtle")) {
            return Optional.of(RDFFormat.TURTLE);
        } else if (lower.endsWith(".jsonld") || lower.endsWith(".json-ld")) {
            return Optional.of(RDFFormat.JSONLD);
        } else if (lower.endsWith(".rdf") || lower.endsWith(".rdfxml") || lower.endsWith(".xml")) {
            return Optional.of(RDFFormat.RDFXML);
        } else if (lower.endsWith(".nt") || lower.endsWith(".ntriples")) {
            return Optional.of(RDFFormat.NTRIPLES);
        } else if (lower.endsWith(".nq") || lower.endsWith(".nquads")) {
            return Optional.of(RDFFormat.NQUADS);
        } else if (lower.endsWith(".trig")) {
            return Optional.of(RDFFormat.TRIG);
        }

        return Optional.empty();
    }

    /**
     * Gets the file extension for an RDF format.
     *
     * @param format the RDF format
     * @return the primary file extension (without dot)
     */
    public static String getExtension(RDFFormat format) {
        Objects.requireNonNull(format, "format");
        return format.getDefaultFileExtension();
    }

    /**
     * Gets the MIME type for an RDF format.
     *
     * @param format the RDF format
     * @return the primary MIME type
     */
    public static String getMimeType(RDFFormat format) {
        Objects.requireNonNull(format, "format");
        return format.getDefaultMIMEType();
    }

    /**
     * Converts RDF content from one format to another.
     *
     * @param content      the source RDF content
     * @param sourceFormat the format of the source content
     * @param targetFormat the desired target format
     * @return the content in the target format
     * @throws it.riccardosacco.bibobibtex.exception.BibliographicConversionException if conversion fails
     */
    public String convert(String content, RDFFormat sourceFormat, RDFFormat targetFormat) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(sourceFormat, "sourceFormat");
        Objects.requireNonNull(targetFormat, "targetFormat");

        try {
            Model model = Rio.parse(new StringReader(content), "", sourceFormat);
            StringWriter writer = new StringWriter();
            Rio.write(model, writer, targetFormat);
            return writer.toString();
        } catch (Exception e) {
            throw new it.riccardosacco.bibobibtex.exception.BibliographicConversionException(
                    "Failed to convert RDF format", e);
        }
    }
}
