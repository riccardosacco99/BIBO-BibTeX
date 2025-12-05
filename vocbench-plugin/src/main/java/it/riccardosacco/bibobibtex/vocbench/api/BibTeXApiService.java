package it.riccardosacco.bibobibtex.vocbench.api;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.vocbench.VocBenchRepositoryGateway;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service providing BibTeX import/export operations for the VocBench API.
 *
 * <p>This service handles:
 * <ul>
 *   <li>BibTeX file parsing and validation</li>
 *   <li>Conversion between BibTeX and BIBO format</li>
 *   <li>Repository storage and retrieval</li>
 *   <li>Import/export statistics and error reporting</li>
 * </ul>
 */
public class BibTeXApiService {
    private static final Logger logger = LoggerFactory.getLogger(BibTeXApiService.class);

    private final VocBenchRepositoryGateway repository;
    private final BibTeXBibliographicConverter converter;

    public BibTeXApiService(VocBenchRepositoryGateway repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.converter = new BibTeXBibliographicConverter();
    }

    public BibTeXApiService(VocBenchRepositoryGateway repository, BibTeXBibliographicConverter converter) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.converter = Objects.requireNonNull(converter, "converter");
    }

    /**
     * Imports BibTeX content into the repository.
     *
     * @param request the import request containing BibTeX content and options
     * @return the import result with statistics and any errors
     */
    public ImportResult importBibTeX(ImportRequest request) {
        Objects.requireNonNull(request, "request");
        long startTime = System.currentTimeMillis();

        logger.info("Starting BibTeX import");

        ImportResult.Builder resultBuilder = ImportResult.builder();
        List<BibTeXEntry> entries;

        try {
            entries = parseBibTeX(request.bibtexContent());
            resultBuilder.totalEntries(entries.size());
            logger.info("Parsed {} BibTeX entries", entries.size());
        } catch (ParseException e) {
            logger.error("Failed to parse BibTeX content", e);
            resultBuilder.totalEntries(0)
                    .failedImports(1)
                    .addError(new ImportResult.ImportError(
                            null,
                            "Failed to parse BibTeX: " + e.getMessage(),
                            "PARSE_ERROR"));
            return resultBuilder.durationMs(System.currentTimeMillis() - startTime).build();
        }

        int successful = 0;
        int failed = 0;
        int skipped = 0;

        for (BibTeXEntry entry : entries) {
            String citationKey = entry.getKey() != null ? entry.getKey().getValue() : "unknown";

            try {
                Optional<BiboDocument> documentOpt = converter.convertToBibo(entry);

                if (documentOpt.isEmpty()) {
                    failed++;
                    resultBuilder.addError(new ImportResult.ImportError(
                            citationKey,
                            "Conversion returned empty result",
                            "CONVERSION_ERROR"));
                    continue;
                }

                BiboDocument document = documentOpt.get();

                // Check for duplicates if enabled
                if (request.options().detectDuplicates()) {
                    Optional<BiboDocument> existing = findDuplicate(document);
                    if (existing.isPresent()) {
                        switch (request.options().duplicateStrategy()) {
                            case SKIP:
                                skipped++;
                                resultBuilder.addWarning("Skipped duplicate: " + citationKey);
                                continue;
                            case ERROR:
                                failed++;
                                resultBuilder.addError(new ImportResult.ImportError(
                                        citationKey,
                                        "Duplicate entry found",
                                        "DUPLICATE_ERROR"));
                                continue;
                            case MERGE:
                                document = mergeDocuments(existing.get(), document);
                                resultBuilder.addWarning("Merged with existing: " + citationKey);
                                break;
                            case CREATE_NEW:
                                // Continue with import
                                break;
                        }
                    }
                }

                // Store in repository
                repository.store(document.rdfModel());
                successful++;

                resultBuilder.addImportedEntry(new ImportResult.ImportedEntry(
                        citationKey,
                        document.id().orElse(citationKey),
                        document.title(),
                        document.type().name()));

            } catch (Exception e) {
                failed++;
                logger.warn("Failed to import entry: {}", citationKey, e);
                resultBuilder.addError(new ImportResult.ImportError(
                        citationKey,
                        e.getMessage(),
                        e.getClass().getSimpleName()));
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Import completed: {} successful, {} failed, {} skipped in {}ms",
                successful, failed, skipped, duration);

        return resultBuilder
                .successfulImports(successful)
                .failedImports(failed)
                .skippedDuplicates(skipped)
                .durationMs(duration)
                .build();
    }

    /**
     * Exports documents from the repository as BibTeX.
     *
     * @param request the export request specifying documents and format
     * @return the export result containing the BibTeX content
     */
    public ExportResult exportBibTeX(ExportRequest request) {
        Objects.requireNonNull(request, "request");
        long startTime = System.currentTimeMillis();

        logger.info("Starting BibTeX export");

        ExportResult.Builder resultBuilder = ExportResult.builder()
                .format(request.format());

        List<BiboDocument> documents;

        if (request.isExportAll()) {
            documents = repository.listAll();
            logger.info("Exporting all {} documents", documents.size());
        } else {
            documents = new ArrayList<>();
            for (String id : request.documentIds()) {
                repository.fetchByIdentifier(id).ifPresentOrElse(
                        documents::add,
                        () -> resultBuilder.addWarning(new ExportResult.ExportWarning(
                                id, null, "Document not found")));
            }
            logger.info("Exporting {} of {} requested documents",
                    documents.size(), request.documentIds().size());
        }

        BibTeXDatabase database = new BibTeXDatabase();

        for (BiboDocument document : documents) {
            try {
                Optional<BibTeXEntry> entryOpt = converter.convertFromBibo(document);
                if (entryOpt.isPresent()) {
                    database.addObject(entryOpt.get());
                    resultBuilder.addExportedId(document.id().orElse("unknown"));
                } else {
                    resultBuilder.addWarning(new ExportResult.ExportWarning(
                            document.id().orElse("unknown"),
                            null,
                            "Conversion returned empty result"));
                }
            } catch (Exception e) {
                logger.warn("Failed to export document: {}", document.id(), e);
                resultBuilder.addWarning(new ExportResult.ExportWarning(
                        document.id().orElse("unknown"),
                        null,
                        e.getMessage()));
            }
        }

        String bibtexContent = formatBibTeX(database);
        long duration = System.currentTimeMillis() - startTime;

        logger.info("Export completed: {} entries in {}ms", database.getEntries().size(), duration);

        return resultBuilder
                .content(bibtexContent)
                .entryCount(database.getEntries().size())
                .durationMs(duration)
                .build();
    }

    /**
     * Validates BibTeX content without importing.
     *
     * @param bibtexContent the BibTeX content to validate
     * @return validation result with any errors or warnings
     */
    public ValidationResult validate(String bibtexContent) {
        Objects.requireNonNull(bibtexContent, "bibtexContent");

        ValidationResult.Builder resultBuilder = ValidationResult.builder();

        try {
            List<BibTeXEntry> entries = parseBibTeX(bibtexContent);
            resultBuilder.entryCount(entries.size());

            for (BibTeXEntry entry : entries) {
                String citationKey = entry.getKey() != null ? entry.getKey().getValue() : "unknown";

                try {
                    Optional<BiboDocument> documentOpt = converter.convertToBibo(entry);
                    if (documentOpt.isPresent()) {
                        resultBuilder.addValidEntry(citationKey);
                    } else {
                        resultBuilder.addError(citationKey, "Conversion failed");
                    }
                } catch (Exception e) {
                    resultBuilder.addError(citationKey, e.getMessage());
                }
            }

            resultBuilder.valid(resultBuilder.errorCount() == 0);

        } catch (ParseException e) {
            resultBuilder.valid(false)
                    .addError(null, "Parse error: " + e.getMessage());
        }

        return resultBuilder.build();
    }

    /**
     * Previews the conversion of BibTeX content without storing.
     *
     * @param bibtexContent the BibTeX content to preview
     * @return list of preview entries showing the conversion result
     */
    public List<ConversionPreview> preview(String bibtexContent) {
        Objects.requireNonNull(bibtexContent, "bibtexContent");

        List<ConversionPreview> previews = new ArrayList<>();

        try {
            List<BibTeXEntry> entries = parseBibTeX(bibtexContent);

            for (BibTeXEntry entry : entries) {
                String citationKey = entry.getKey() != null ? entry.getKey().getValue() : "unknown";

                try {
                    Optional<BiboDocument> documentOpt = converter.convertToBibo(entry);

                    if (documentOpt.isPresent()) {
                        BiboDocument doc = documentOpt.get();
                        previews.add(new ConversionPreview(
                                citationKey,
                                doc.title(),
                                doc.type().name(),
                                doc.authors().size(),
                                doc.publicationDate().map(d -> String.valueOf(d.year())).orElse(null),
                                doc.identifiers().size(),
                                null,
                                true));
                    } else {
                        previews.add(new ConversionPreview(
                                citationKey, null, null, 0, null, 0,
                                "Conversion failed", false));
                    }
                } catch (Exception e) {
                    previews.add(new ConversionPreview(
                            citationKey, null, null, 0, null, 0,
                            e.getMessage(), false));
                }
            }

        } catch (ParseException e) {
            previews.add(new ConversionPreview(
                    null, null, null, 0, null, 0,
                    "Parse error: " + e.getMessage(), false));
        }

        return previews;
    }

    private List<BibTeXEntry> parseBibTeX(String content) throws ParseException {
        BibTeXParser parser = new BibTeXParser();
        BibTeXDatabase database = parser.parse(new StringReader(content));
        return new ArrayList<>(database.getEntries().values());
    }

    private String formatBibTeX(BibTeXDatabase database) {
        try {
            StringWriter writer = new StringWriter();
            new BibTeXFormatter().format(database, writer);
            return writer.toString();
        } catch (IOException e) {
            logger.error("Failed to format BibTeX", e);
            return "";
        }
    }

    private Optional<BiboDocument> findDuplicate(BiboDocument document) {
        // Check by DOI first
        for (var identifier : document.identifiers()) {
            if (identifier.type().name().contains("DOI")) {
                Optional<BiboDocument> existing = repository.fetchByIdentifier(identifier.value());
                if (existing.isPresent()) {
                    return existing;
                }
            }
        }

        // Check by citation key/id
        if (document.id().isPresent()) {
            return repository.fetchByIdentifier(document.id().get());
        }

        return Optional.empty();
    }

    private BiboDocument mergeDocuments(BiboDocument existing, BiboDocument incoming) {
        // Simple merge: prefer non-empty values from incoming
        BiboDocument.Builder builder = BiboDocument.builder(
                incoming.type(),
                incoming.title());

        builder.id(existing.id().orElse(incoming.id().orElse(null)));
        incoming.subtitle().or(existing::subtitle).ifPresent(builder::subtitle);
        builder.contributors(incoming.contributors().isEmpty() ?
                existing.contributors() : incoming.contributors());
        incoming.publicationDate().or(existing::publicationDate).ifPresent(builder::publicationDate);
        incoming.publisher().or(existing::publisher).ifPresent(builder::publisher);
        incoming.containerTitle().or(existing::containerTitle).ifPresent(builder::containerTitle);
        incoming.volume().or(existing::volume).ifPresent(builder::volume);
        incoming.issue().or(existing::issue).ifPresent(builder::issue);
        incoming.pages().or(existing::pages).ifPresent(builder::pages);
        builder.identifiers(incoming.identifiers().isEmpty() ?
                existing.identifiers() : incoming.identifiers());
        incoming.url().or(existing::url).ifPresent(builder::url);
        incoming.abstractText().or(existing::abstractText).ifPresent(builder::abstractText);

        return builder.build();
    }

    /**
     * Preview of a single conversion.
     */
    public record ConversionPreview(
            String citationKey,
            String title,
            String type,
            int authorCount,
            String year,
            int identifierCount,
            String error,
            boolean valid
    ) {}

    /**
     * Validation result for BibTeX content.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final int entryCount;
        private final List<String> validEntries;
        private final List<ValidationError> errors;

        private ValidationResult(Builder builder) {
            this.valid = builder.valid;
            this.entryCount = builder.entryCount;
            this.validEntries = List.copyOf(builder.validEntries);
            this.errors = List.copyOf(builder.errors);
        }

        public static Builder builder() {
            return new Builder();
        }

        public boolean isValid() {
            return valid;
        }

        public int entryCount() {
            return entryCount;
        }

        public List<String> validEntries() {
            return validEntries;
        }

        public List<ValidationError> errors() {
            return errors;
        }

        public record ValidationError(String citationKey, String message) {}

        public static final class Builder {
            private boolean valid = true;
            private int entryCount;
            private final List<String> validEntries = new ArrayList<>();
            private final List<ValidationError> errors = new ArrayList<>();

            public Builder valid(boolean valid) {
                this.valid = valid;
                return this;
            }

            public Builder entryCount(int count) {
                this.entryCount = count;
                return this;
            }

            public Builder addValidEntry(String citationKey) {
                this.validEntries.add(citationKey);
                return this;
            }

            public Builder addError(String citationKey, String message) {
                this.errors.add(new ValidationError(citationKey, message));
                return this;
            }

            public int errorCount() {
                return errors.size();
            }

            public ValidationResult build() {
                return new ValidationResult(this);
            }
        }
    }
}
