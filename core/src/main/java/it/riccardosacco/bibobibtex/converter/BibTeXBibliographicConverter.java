package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.exception.ValidationException;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import it.riccardosacco.bibobibtex.model.bibo.BiboVocabulary;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BibTeXBibliographicConverter implements BibliographicConverter<BibTeXEntry> {
    private static final Logger logger = LoggerFactory.getLogger(BibTeXBibliographicConverter.class);
    private static final Map<String, Integer> MONTH_ALIASES =
            Map.ofEntries(
                    Map.entry("jan", 1),
                    Map.entry("january", 1),
                    Map.entry("feb", 2),
                    Map.entry("february", 2),
                    Map.entry("mar", 3),
                    Map.entry("march", 3),
                    Map.entry("apr", 4),
                    Map.entry("april", 4),
                    Map.entry("may", 5),
                    Map.entry("jun", 6),
                    Map.entry("june", 6),
                    Map.entry("jul", 7),
                    Map.entry("july", 7),
                    Map.entry("aug", 8),
                    Map.entry("august", 8),
                    Map.entry("sep", 9),
                    Map.entry("sept", 9),
                    Map.entry("september", 9),
                    Map.entry("oct", 10),
                    Map.entry("october", 10),
                    Map.entry("nov", 11),
                    Map.entry("november", 11),
                    Map.entry("dec", 12),
                    Map.entry("december", 12));

    private static final int MIN_CITATION_KEY_LENGTH = 3;
    private static final int MAX_CITATION_KEY_LENGTH = 64;

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s+and\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTI_VALUE_SEPARATOR = Pattern.compile("[,;]");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern VALID_CITATION_KEY = Pattern.compile(
            "^[a-zA-Z0-9_-]{" + MIN_CITATION_KEY_LENGTH + "," + MAX_CITATION_KEY_LENGTH + "}$");
    private static final Set<String> PARTICLE_TOKENS =
            Set.of("von", "van", "der", "den", "de", "del", "della", "di", "da", "dos", "das", "du", "le", "la", "ter", "ibn", "bin", "al");
    private static final Set<String> SUFFIX_TOKENS = Set.of("jr", "sr", "iii", "iv", "ii", "v", "phd", "md", "esq");
    private static final Map<IRI, BiboContributorRole> CONTRIBUTOR_PREDICATES =
            Map.of(
                    DCTERMS.CREATOR, BiboContributorRole.AUTHOR,
                    BiboVocabulary.EDITOR, BiboContributorRole.EDITOR,
                    BiboVocabulary.TRANSLATOR, BiboContributorRole.TRANSLATOR,
                    BiboVocabulary.ADVISOR, BiboContributorRole.ADVISOR,
                    BiboVocabulary.REVIEWER, BiboContributorRole.REVIEWER,
                    DCTERMS.CONTRIBUTOR, BiboContributorRole.CONTRIBUTOR);

    private static final Map<IRI, BiboIdentifierType> IDENTIFIER_PREDICATES =
            Arrays.stream(BiboIdentifierType.values())
                    .filter(type -> type != BiboIdentifierType.URL)
                    .map(type -> type.predicate().map(predicate -> Map.entry(predicate, type)))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> a,
                            java.util.LinkedHashMap::new));

    private static final Key FIELD_SUBTITLE = new Key("subtitle");
    private static final Key FIELD_DAY = new Key("day");
    private static final Key FIELD_HANDLE = new Key("handle");
    private static final Key FIELD_ISBN = new Key("isbn");
    private static final Key FIELD_ISSN = new Key("issn");
    private static final Key FIELD_LANGUAGE = new Key("language");
    private static final Key FIELD_ABSTRACT = new Key("abstract");
    private static final Key FIELD_URI = new Key("uri");
    private static final Key FIELD_SERIES = new Key("series");
    private static final Key FIELD_EDITION = new Key("edition");
    private static final Key FIELD_KEYWORDS = new Key("keywords");
    private static final Key FIELD_ADVISOR = new Key("advisor");
    private static final Key TYPE_ONLINE = new Key("online");
    private static final Key TYPE_BOOKLET = new Key("booklet");
    private static final Key TYPE_MANUAL = new Key("manual");
    private static final Key TYPE_UNPUBLISHED = new Key("unpublished");
    private static final Key TYPE_CONFERENCE = new Key("conference");
    private final Set<String> usedCitationKeys = ConcurrentHashMap.newKeySet();
    private final KeyGenerationStrategy keyStrategy;

    public BibTeXBibliographicConverter() {
        this(KeyGenerationStrategy.AUTHOR_YEAR);
    }

    public BibTeXBibliographicConverter(KeyGenerationStrategy keyStrategy) {
        this.keyStrategy = Objects.requireNonNull(keyStrategy, "keyStrategy");
    }

    @Override
    public Optional<BiboDocument> convertToBibo(BibTeXEntry source) {
        logger.info("Starting BibTeX → BIBO conversion for entry: {}", citationKeyValue(source));

        // Validate input
        BibliographicValidator.validateBibTeXEntry(source);

        Optional<String> rawYear = fieldValue(source, BibTeXEntry.KEY_YEAR);
        boolean isCirca = rawYear.map(BibTeXBibliographicConverter::containsCircaToken).orElse(false);
        Optional<String> noteValue = fieldValue(source, BibTeXEntry.KEY_NOTE);

        String title = fieldValue(source, BibTeXEntry.KEY_TITLE).orElseGet(() -> citationKeyValue(source));
        if (title == null || title.isBlank()) {
            throw new ValidationException("Title is required", "title", title);
        }

        BiboDocumentType documentType = mapDocumentType(source.getType());
        logger.debug("Mapped BibTeX type {} to BIBO type {}", source.getType(), documentType);

        BiboDocument.Builder builder = BiboDocument.builder(documentType, title).id(citationKeyValue(source));

        fieldValue(source, FIELD_SUBTITLE).ifPresent(builder::subtitle);
        parseContributors(fieldValue(source, BibTeXEntry.KEY_AUTHOR), BiboContributorRole.AUTHOR)
                .forEach(builder::addContributor);
        parseContributors(fieldValue(source, BibTeXEntry.KEY_EDITOR), BiboContributorRole.EDITOR)
                .forEach(builder::addContributor);
        parseContributors(fieldValue(source, FIELD_ADVISOR), BiboContributorRole.ADVISOR)
                .forEach(builder::addContributor);

        parsePublicationDate(source).ifPresent(builder::publicationDate);

        fieldValue(source, BibTeXEntry.KEY_PUBLISHER)
                .or(() -> inferPublisher(source))
                .ifPresent(builder::publisher);

        // Context-aware address resolution (US-24)
        AddressResolution addressRes = resolveAddress(source);
        if (addressRes.address() != null) {
            if (addressRes.type() == AddressType.CONFERENCE_LOCATION) {
                builder.conferenceLocation(addressRes.address());
            } else {
                builder.placeOfPublication(addressRes.address());
            }
        }

        // Context-aware organization resolution (US-24)
        OrganizationResolution orgRes = resolveOrganization(source);
        if (orgRes.organization() != null && orgRes.type() == OrganizationType.CONFERENCE_ORGANIZER) {
            builder.conferenceOrganizer(orgRes.organization());
        }

        // Thesis degree type resolution (US-24)
        resolveDegreeType(source).ifPresent(builder::degreeType);

        fieldValue(source, BibTeXEntry.KEY_JOURNAL, BibTeXEntry.KEY_BOOKTITLE).ifPresent(builder::containerTitle);
        fieldValue(source, BibTeXEntry.KEY_VOLUME).ifPresent(builder::volume);
        fieldValue(source, BibTeXEntry.KEY_NUMBER).ifPresent(builder::issue);
        fieldValue(source, BibTeXEntry.KEY_PAGES).ifPresent(builder::pages);

        extractIdentifiers(source).forEach(builder::addIdentifier);
        fieldValue(source, BibTeXEntry.KEY_URL)
                .map(BibTeXBibliographicConverter::sanitizeUrl)
                .ifPresent(builder::url);
        fieldValue(source, FIELD_LANGUAGE).ifPresent(builder::language);
        fieldValue(source, FIELD_ABSTRACT).ifPresent(builder::abstractText);
        fieldValue(source, FIELD_SERIES).ifPresent(builder::series);
        fieldValue(source, FIELD_EDITION).ifPresent(builder::edition);
        fieldValue(source, FIELD_KEYWORDS).ifPresent(keywords -> {
            List<String> keywordList = Arrays.stream(MULTI_VALUE_SEPARATOR.split(keywords))
                    .map(String::strip)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            builder.keywords(keywordList);
        });

        String circaNote = isCirca ? formatCircaNote(rawYear.orElse(null)) : null;
        String combinedNote = combineNotes(noteValue.orElse(null), circaNote);
        if (combinedNote != null) {
            builder.notes(combinedNote);
        }

        BiboDocument result = builder.build();
        logger.info("Successfully converted BibTeX entry to BIBO document: {}", result.title());
        logger.debug("Document details: type={}, contributors={}, identifiers={}",
            result.type(), result.contributors().size(), result.identifiers().size());

        return Optional.of(result);
    }

    @Override
    public Optional<BibTeXEntry> convertFromBibo(BiboDocument source) {
        logger.info("Starting BIBO → BibTeX conversion for document: {}", source.title());

        // Validate input (lenient mode to allow roundtrip of malformed identifiers)
        BibliographicValidator.validateBiboDocument(source, true);

        // Determine entry type, with special handling for thesis
        Key entryType;
        if (source.type() == BiboDocumentType.THESIS && source.degreeType().isPresent()) {
            String degree = source.degreeType().get().toLowerCase();
            if (degree.contains("master")) {
                entryType = BibTeXEntry.TYPE_MASTERSTHESIS;
            } else if (degree.contains("phd") || degree.contains("doctor")) {
                entryType = BibTeXEntry.TYPE_PHDTHESIS;
            } else {
                entryType = BibTeXEntry.TYPE_PHDTHESIS;  // Default to PhD
            }
        } else {
            entryType = mapEntryType(source.type());
        }

        logger.debug("Mapped BIBO type {} to BibTeX type {}", source.type(), entryType);
        String citationKey = resolveCitationKey(source);

        BibTeXEntry entry = new BibTeXEntry(entryType, new Key(citationKey));
        putField(entry, BibTeXEntry.KEY_TITLE, source.title());
        source.subtitle().ifPresent(value -> putField(entry, FIELD_SUBTITLE, value));

        // Export degreeType for thesis entries
        if (source.type() == BiboDocumentType.THESIS) {
            source.degreeType().ifPresent(value -> putField(entry, BibTeXEntry.KEY_TYPE, value));
        }

        formatContributors(source.authors()).ifPresent(value -> putField(entry, BibTeXEntry.KEY_AUTHOR, value));
        formatContributors(source.editors()).ifPresent(value -> putField(entry, BibTeXEntry.KEY_EDITOR, value));
        formatContributors(source.contributorsByRole(BiboContributorRole.ADVISOR))
                .ifPresent(value -> putField(entry, FIELD_ADVISOR, value));

        source.publicationDate().ifPresent(date -> {
            putField(entry, BibTeXEntry.KEY_YEAR, Integer.toString(date.year()));
            date.month().ifPresent(month -> putField(entry, BibTeXEntry.KEY_MONTH, monthToBibTeX(month)));
            date.day().ifPresent(day -> putField(entry, FIELD_DAY, Integer.toString(day)));
        });

        source.publisher().ifPresent(value -> putField(entry, fieldForPublisher(entryType), value));

        // Context-aware address field (US-24): conference location vs publisher location
        if (BibTeXEntry.TYPE_INPROCEEDINGS.equals(entryType) || BibTeXEntry.TYPE_PROCEEDINGS.equals(entryType)) {
            source.conferenceLocation().ifPresent(value -> putField(entry, BibTeXEntry.KEY_ADDRESS, value));
        } else {
            source.placeOfPublication().ifPresent(value -> putField(entry, BibTeXEntry.KEY_ADDRESS, value));
        }

        // Context-aware organization field (US-24): conference organizer vs publisher
        if (BibTeXEntry.TYPE_PROCEEDINGS.equals(entryType) || BibTeXEntry.TYPE_INPROCEEDINGS.equals(entryType)) {
            source.conferenceOrganizer().ifPresent(value -> putField(entry, BibTeXEntry.KEY_ORGANIZATION, value));
        } else if (BibTeXEntry.TYPE_MANUAL.equals(entryType)) {
            // For @manual, organization is the publisher (already handled above, but can be explicit)
            if (entry.getField(BibTeXEntry.KEY_PUBLISHER) == null) {
                source.publisher().ifPresent(value -> putField(entry, BibTeXEntry.KEY_ORGANIZATION, value));
            }
        }

        // Thesis degree type field (US-24)
        if (BibTeXEntry.TYPE_PHDTHESIS.equals(entryType) || BibTeXEntry.TYPE_MASTERSTHESIS.equals(entryType)) {
            source.degreeType().ifPresent(value -> putField(entry, BibTeXEntry.KEY_TYPE, value));
        }

        source.containerTitle().ifPresent(value -> putField(entry, fieldForContainer(entryType), value));
        source.volume().ifPresent(value -> putField(entry, BibTeXEntry.KEY_VOLUME, value));
        source.issue().ifPresent(value -> putField(entry, BibTeXEntry.KEY_NUMBER, value));
        source.pages().ifPresent(value -> putField(entry, BibTeXEntry.KEY_PAGES, value));

        addIdentifierFields(entry, source.identifiers());

        source.url().ifPresent(value -> putField(entry, BibTeXEntry.KEY_URL, value));
        source.language().ifPresent(value -> putField(entry, FIELD_LANGUAGE, value));
        source.abstractText().ifPresent(value -> putField(entry, FIELD_ABSTRACT, value));
        source.notes().ifPresent(value -> putField(entry, BibTeXEntry.KEY_NOTE, value));
        source.series().ifPresent(value -> putField(entry, FIELD_SERIES, value));
        source.edition().ifPresent(value -> putField(entry, FIELD_EDITION, value));
        if (!source.keywords().isEmpty()) {
            String keywordsString = String.join(", ", source.keywords());
            putField(entry, FIELD_KEYWORDS, keywordsString);
        }

        logger.info("Successfully converted BIBO document to BibTeX entry: {}", citationKey);
        logger.debug("Entry type: {}, fields count: {}", entryType, entry.getFields().size());

        return Optional.of(entry);
    }

    /**
     * Converts a batch of {@link BiboDocument} instances to BibTeX entries while maintaining citation-key uniqueness.
     *
     * @param documents documents to convert
     * @return list of converted entries (skipping documents that fail validation)
     */
    public List<BibTeXEntry> convertFromBiboBatch(List<BiboDocument> documents) {
        Objects.requireNonNull(documents, "documents");
        List<BibTeXEntry> entries = new ArrayList<>();
        for (BiboDocument document : documents) {
            convertFromBibo(document).ifPresent(entries::add);
        }
        return entries;
    }

    /**
     * Converts every BIBO document contained in the provided RDF model.
     *
     * @param model RDF4J model containing BIBO resources
     * @return list of converted documents, skipping malformed resources
     */
    public List<BiboDocument> convertAllFromRDF(Model model) {
        Objects.requireNonNull(model, "model");
        List<BiboDocument> documents = new ArrayList<>();

        for (Resource resource : selectDocumentSubjects(model)) {
            try {
                documents.add(buildDocumentFromModel(model, resource));
            } catch (Exception ex) {
                logger.warn("Skipping RDF resource {}: {}", resource, ex.getMessage());
            }
        }
        return documents;
    }

    /**
     * Converts a single BIBO document identified by its URI inside the RDF model.
     *
     * @param model RDF4J model containing the document
     * @param documentUri resource IRI to convert
     * @return converted {@link BiboDocument}
     */
    public BiboDocument convertFromRDF(Model model, String documentUri) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(documentUri, "documentUri");

        Resource resource = createResource(documentUri);
        if (!model.contains(resource, null, null)) {
            throw new IllegalArgumentException("Document not found in RDF model: " + documentUri);
        }
        return buildDocumentFromModel(model, resource);
    }

    /**
     * Reads an RDF file (Turtle / RDFXML / JSON-LD) and converts all contained documents.
     *
     * @param file path to the RDF file
     * @return list of converted documents
     * @throws IOException if the file cannot be read or parsed
     */
    public List<BiboDocument> convertFromRDFFile(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file)) {
            throw new IOException("RDF file not found: " + file.toAbsolutePath());
        }
        RDFFormat format = Rio.getParserFormatForFileName(file.toString()).orElse(RDFFormat.TURTLE);
        try (InputStream stream = Files.newInputStream(file)) {
            Model model = Rio.parse(stream, "", format);
            return convertAllFromRDF(model);
        }
    }

    private String resolveCitationKey(BiboDocument source) {
        return providedCitationKey(source).orElseGet(() -> generateUniqueKey(source));
    }

    private Optional<String> providedCitationKey(BiboDocument source) {
        return source.id()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .filter(value -> {
                    if (!isValidCitationKey(value)) {
                        logger.warn("Invalid citation key '{}' for document '{}', regenerating.", value, source.title());
                        return false;
                    }
                    return true;
                })
                .map(this::registerProvidedKey);
    }

    private String registerProvidedKey(String key) {
        String candidate = key;
        int counter = 2;
        while (usedCitationKeys.contains(candidate)) {
            candidate = appendSuffix(key, counter++);
        }
        if (!candidate.equals(key)) {
            logger.warn("Citation key '{}' already used, assigned '{}'.", key, candidate);
        }
        usedCitationKeys.add(candidate);
        return candidate;
    }

    private static String appendSuffix(String base, int counter) {
        String suffix = "_" + counter;
        if (base.length() + suffix.length() > MAX_CITATION_KEY_LENGTH) {
            int maxBaseLength = Math.max(MIN_CITATION_KEY_LENGTH, MAX_CITATION_KEY_LENGTH - suffix.length());
            base = base.substring(0, Math.min(base.length(), maxBaseLength));
        }
        return base + suffix;
    }

    private String generateUniqueKey(BiboDocument document) {
        String base = baseKeyForStrategy(document).orElse(document.title());
        return registerGeneratedKey(base);
    }

    private String registerGeneratedKey(String rawBase) {
        String base = clampCitationKey(rawBase);
        String candidate = base;
        int counter = 2;
        while (usedCitationKeys.contains(candidate)) {
            candidate = clampCitationKey(appendSuffix(base, counter++));
        }
        usedCitationKeys.add(candidate);
        return candidate;
    }

    private Optional<String> baseKeyForStrategy(BiboDocument document) {
        return switch (keyStrategy) {
            case AUTHOR_YEAR -> authorYearKey(document);
            case AUTHOR_TITLE -> authorTitleKey(document);
            case HASH -> Optional.of(hashKey(document));
        };
    }

    private Optional<String> authorYearKey(BiboDocument document) {
        Optional<String> family = firstAuthorToken(document);
        Optional<Integer> year = document.publicationDate().map(BiboPublicationDate::year);
        if (family.isEmpty() && year.isEmpty()) {
            return Optional.empty();
        }
        String yearToken = year.map(Object::toString).orElse("nd");
        String authorToken = family.orElse(document.title());
        return Optional.of(authorToken + "_" + yearToken);
    }

    private Optional<String> authorTitleKey(BiboDocument document) {
        String titleToken = sanitizeForKey(document.title());
        if (titleToken.isEmpty()) {
            titleToken = "entry";
        }
        String firstWord = Arrays.stream(titleToken.split("_"))
                .filter(part -> !part.isBlank())
                .findFirst()
                .orElse(titleToken);
        String authorToken = firstAuthorToken(document).orElse(document.title());
        return Optional.of(authorToken + "_" + firstWord);
    }

    private static Optional<String> firstAuthorToken(BiboDocument document) {
        return document.authors().stream()
                .map(BiboContributor::name)
                .map(name -> name.familyName().orElse(name.fullName()))
                .findFirst();
    }

    private static String hashKey(BiboDocument document) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(document.title().getBytes(StandardCharsets.UTF_8));
            document.authors().forEach(contributor ->
                    digest.update(contributor.name().fullName().getBytes(StandardCharsets.UTF_8)));
            document.publicationDate().ifPresent(date ->
                    digest.update(Integer.toString(date.year()).getBytes(StandardCharsets.UTF_8)));
            digest.update(document.type().name().getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append("%02x".formatted(value));
                if (builder.length() >= 8) {
                    break;
                }
            }
            while (builder.length() < 8) {
                builder.append('0');
            }
            return builder.substring(0, 8);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("MD5 algorithm not available", ex);
        }
    }

    private static boolean isValidCitationKey(String key) {
        return key != null && VALID_CITATION_KEY.matcher(key).matches();
    }

    private static String clampCitationKey(String value) {
        String sanitized = sanitizeForKey(value);
        if (sanitized.length() > MAX_CITATION_KEY_LENGTH) {
            sanitized = sanitized.substring(0, MAX_CITATION_KEY_LENGTH);
        }
        while (sanitized.length() < MIN_CITATION_KEY_LENGTH) {
            sanitized = sanitized + "_x";
        }
        return sanitized;
    }

    private static String citationKeyValue(BibTeXEntry entry) {
        Key key = entry.getKey();
        return key != null ? key.getValue() : null;
    }

    private static Optional<String> fieldValue(BibTeXEntry entry, Key key) {
        if (key == null) {
            return Optional.empty();
        }
        org.jbibtex.Value value;
        try {
            value = entry.getField(key);
        } catch (ClassCastException e) {
            // This can happen when cross-references are not properly resolved
            // and the field contains a StringValue instead of a CrossReferenceValue
            return Optional.empty();
        }
        if (value == null) {
            return Optional.empty();
        }
        String text = value.toUserString().trim();
        // FIX-02: Convert LaTeX escape sequences to Unicode
        text = BibTeXUnicodeConverter.toUnicode(text);
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }

    private static Optional<String> fieldValue(BibTeXEntry entry, Key primary, Key fallback) {
        Optional<String> result = fieldValue(entry, primary);
        if (result.isEmpty() && fallback != null) {
            logger.debug("Field {} not found, using fallback {}", primary, fallback);
            result = fieldValue(entry, fallback);
        }
        return result;
    }

    private static Optional<String> inferPublisher(BibTeXEntry entry) {
        if (entry == null || entry.getType() == null) {
            return Optional.empty();
        }
        Key type = entry.getType();
        if (BibTeXEntry.TYPE_PHDTHESIS.equals(type) || BibTeXEntry.TYPE_MASTERSTHESIS.equals(type)) {
            return fieldValue(entry, BibTeXEntry.KEY_SCHOOL);
        }
        if (BibTeXEntry.TYPE_TECHREPORT.equals(type)) {
            return fieldValue(entry, BibTeXEntry.KEY_INSTITUTION);
        }
        // For @manual, organization field means publisher
        if (BibTeXEntry.TYPE_MANUAL.equals(type)) {
            return fieldValue(entry, BibTeXEntry.KEY_ORGANIZATION);
        }
        return Optional.empty();
    }

    /**
     * Resolves the address field based on BibTeX entry type context.
     * For @inproceedings/@proceedings: address = conference location
     * For @book/@article/@mastersthesis/@phdthesis: address = publisher/institution location
     *
     * @param entry the BibTeX entry
     * @return AddressResolution containing the resolved address and its semantic type
     */
    private static AddressResolution resolveAddress(BibTeXEntry entry) {
        Optional<String> address = fieldValue(entry, BibTeXEntry.KEY_ADDRESS);
        if (address.isEmpty()) {
            return new AddressResolution(null, AddressType.PUBLISHER_LOCATION);
        }

        Key type = entry.getType();
        if (BibTeXEntry.TYPE_INPROCEEDINGS.equals(type) || BibTeXEntry.TYPE_PROCEEDINGS.equals(type)) {
            return new AddressResolution(address.get(), AddressType.CONFERENCE_LOCATION);
        }
        // For all other types: book, article, thesis, techreport, etc.
        return new AddressResolution(address.get(), AddressType.PUBLISHER_LOCATION);
    }

    /**
     * Resolves the organization field based on BibTeX entry type context.
     * For @proceedings: organization = conference organizer/sponsor
     * For @manual: organization = publisher
     *
     * @param entry the BibTeX entry
     * @return OrganizationResolution containing the resolved organization and its semantic type
     */
    private static OrganizationResolution resolveOrganization(BibTeXEntry entry) {
        Optional<String> organization = fieldValue(entry, BibTeXEntry.KEY_ORGANIZATION);
        if (organization.isEmpty()) {
            return new OrganizationResolution(null, OrganizationType.PUBLISHER);
        }

        Key type = entry.getType();
        if (BibTeXEntry.TYPE_PROCEEDINGS.equals(type) || BibTeXEntry.TYPE_INPROCEEDINGS.equals(type)) {
            return new OrganizationResolution(organization.get(), OrganizationType.CONFERENCE_ORGANIZER);
        } else if (BibTeXEntry.TYPE_MANUAL.equals(type)) {
            return new OrganizationResolution(organization.get(), OrganizationType.PUBLISHER);
        }
        return new OrganizationResolution(organization.get(), OrganizationType.GENERIC);
    }

    /**
     * Resolves the type field for thesis entries.
     * Extracts degree type (e.g., "Master's thesis", "PhD dissertation").
     *
     * @param entry the BibTeX entry
     * @return the degree type if available
     */
    private static Optional<String> resolveDegreeType(BibTeXEntry entry) {
        if (entry == null || entry.getType() == null) {
            return Optional.empty();
        }

        // Check explicit 'type' field first
        Optional<String> explicitType = fieldValue(entry, BibTeXEntry.KEY_TYPE);
        if (explicitType.isPresent()) {
            return explicitType;
        }

        // Infer from entry type
        // Note: Only set for mastersthesis - phdthesis is the default and doesn't need explicit degreeType
        Key type = entry.getType();
        if (BibTeXEntry.TYPE_MASTERSTHESIS.equals(type)) {
            return Optional.of("Master's thesis");
        }
        return Optional.empty();
    }

    // Helper classes for semantic resolution
    private record AddressResolution(String address, AddressType type) {}

    private enum AddressType {
        CONFERENCE_LOCATION,
        PUBLISHER_LOCATION
    }

    private record OrganizationResolution(String organization, OrganizationType type) {}

    private enum OrganizationType {
        CONFERENCE_ORGANIZER,
        PUBLISHER,
        GENERIC
    }

    /**
     * Sanitizes URL by keeping only the first line and ensuring it is a fully qualified HTTP(S) URL.
     * Some BibTeX files contain multi-line URLs or omit the protocol, which leads to invalid RDF identifiers.
     */
    private static String sanitizeUrl(String url) {
        if (url == null) {
            return null;
        }
        String sanitized = url.strip();

        // Take only the first line if URL contains newlines or carriage returns
        int newlineIndex = sanitized.indexOf('\n');
        int crIndex = sanitized.indexOf('\r');

        // Find the first line break character
        int breakIndex = -1;
        if (newlineIndex >= 0 && crIndex >= 0) {
            breakIndex = Math.min(newlineIndex, crIndex);
        } else if (newlineIndex >= 0) {
            breakIndex = newlineIndex;
        } else if (crIndex >= 0) {
            breakIndex = crIndex;
        }

        if (breakIndex >= 0) {
            sanitized = sanitized.substring(0, breakIndex).trim();
        }

        if (sanitized.startsWith("//")) {
            sanitized = "https:" + sanitized;
        } else if (sanitized.regionMatches(true, 0, "http://", 0, 7)) {
            sanitized = "http://" + sanitized.substring(7);
        } else if (sanitized.regionMatches(true, 0, "https://", 0, 8)) {
            sanitized = "https://" + sanitized.substring(8);
        } else if (!sanitized.contains("://")) {
            sanitized = "https://" + sanitized;
        }

        return sanitized;
    }

    private static void putField(BibTeXEntry entry, Key key, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return;
        }
        // FIX-02: Convert Unicode characters to LaTeX escape sequences
        trimmed = BibTeXUnicodeConverter.fromUnicode(trimmed);
        entry.addField(key, new StringValue(trimmed, StringValue.Style.BRACED));
    }

    /**
     * Maps BibTeX entry type to BIBO document type.
     * Note: @inbook and @incollection both map to BOOK_SECTION as they represent
     * the same concept (chapter/section in edited collection) with identical field sets.
     * @conference is an alias for @inproceedings and maps to CONFERENCE_PAPER.
     *
     * @param type the BibTeX entry type
     * @return the corresponding BIBO document type
     */
    private static BiboDocumentType mapDocumentType(Key type) {
        if (type == null) {
            return BiboDocumentType.OTHER;
        } else if (BibTeXEntry.TYPE_ARTICLE.equals(type)) {
            return BiboDocumentType.ARTICLE;
        } else if (BibTeXEntry.TYPE_BOOK.equals(type)) {
            return BiboDocumentType.BOOK;
        } else if (BibTeXEntry.TYPE_INBOOK.equals(type) || BibTeXEntry.TYPE_INCOLLECTION.equals(type)) {
            // FIX-04: Both @inbook and @incollection map to BOOK_SECTION
            return BiboDocumentType.BOOK_SECTION;
        } else if (BibTeXEntry.TYPE_INPROCEEDINGS.equals(type) || TYPE_CONFERENCE.equals(type)) {
            // @conference is an alias for @inproceedings
            return BiboDocumentType.CONFERENCE_PAPER;
        } else if (BibTeXEntry.TYPE_PROCEEDINGS.equals(type)) {
            return BiboDocumentType.PROCEEDINGS;
        } else if (BibTeXEntry.TYPE_MASTERSTHESIS.equals(type) || BibTeXEntry.TYPE_PHDTHESIS.equals(type)) {
            return BiboDocumentType.THESIS;
        } else if (BibTeXEntry.TYPE_TECHREPORT.equals(type)) {
            return BiboDocumentType.REPORT;
        } else if (TYPE_ONLINE.equals(type)) {
            return BiboDocumentType.WEBPAGE;
        } else if (TYPE_BOOKLET.equals(type)) {
            return BiboDocumentType.BOOKLET;
        } else if (TYPE_MANUAL.equals(type)) {
            return BiboDocumentType.MANUAL;
        } else if (TYPE_UNPUBLISHED.equals(type)) {
            return BiboDocumentType.MANUSCRIPT;
        }
        return BiboDocumentType.OTHER;
    }

    /**
     * Maps BIBO document type to BibTeX entry type.
     * Note: BOOK_SECTION maps to @incollection (preferred over @inbook for
     * chapters in edited collections, as commonly used in academic databases).
     * THESIS mapping depends on degreeType field (checked separately).
     *
     * @param type the BIBO document type
     * @return the corresponding BibTeX entry type
     */
    private static Key mapEntryType(BiboDocumentType type) {
        if (type == null) {
            return BibTeXEntry.TYPE_MISC;
        }
        return switch (type) {
            case ARTICLE -> BibTeXEntry.TYPE_ARTICLE;
            case BOOK -> BibTeXEntry.TYPE_BOOK;
            case BOOK_SECTION -> BibTeXEntry.TYPE_INCOLLECTION;  // FIX-04: prefer @incollection
            case CONFERENCE_PAPER -> BibTeXEntry.TYPE_INPROCEEDINGS;
            case PROCEEDINGS -> BibTeXEntry.TYPE_PROCEEDINGS;
            case THESIS -> BibTeXEntry.TYPE_PHDTHESIS;  // Default, may be overridden by degreeType
            case REPORT -> BibTeXEntry.TYPE_TECHREPORT;
            case WEBPAGE -> TYPE_ONLINE;
            case BOOKLET -> TYPE_BOOKLET;
            case MANUAL -> TYPE_MANUAL;
            case MANUSCRIPT -> TYPE_UNPUBLISHED;
            default -> BibTeXEntry.TYPE_MISC;
        };
    }

    private static Key fieldForPublisher(Key type) {
        if (BibTeXEntry.TYPE_PHDTHESIS.equals(type) || BibTeXEntry.TYPE_MASTERSTHESIS.equals(type)) {
            return BibTeXEntry.KEY_SCHOOL;
        } else if (BibTeXEntry.TYPE_TECHREPORT.equals(type)) {
            return BibTeXEntry.KEY_INSTITUTION;
        }
        return BibTeXEntry.KEY_PUBLISHER;
    }

    private static Key fieldForContainer(Key type) {
        if (BibTeXEntry.TYPE_ARTICLE.equals(type)) {
            return BibTeXEntry.KEY_JOURNAL;
        } else if (BibTeXEntry.TYPE_INPROCEEDINGS.equals(type)
                || BibTeXEntry.TYPE_PROCEEDINGS.equals(type)
                || BibTeXEntry.TYPE_INBOOK.equals(type)
                || BibTeXEntry.TYPE_INCOLLECTION.equals(type)) {
            return BibTeXEntry.KEY_BOOKTITLE;
        }
        return BibTeXEntry.KEY_BOOKTITLE;
    }

    private static List<BiboContributor> parseContributors(
            Optional<String> rawNames, BiboContributorRole role) {
        if (rawNames.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(NAME_SEPARATOR.split(rawNames.get().trim()))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> new BiboContributor(parseName(name), role))
                .toList();
    }

    /**
     * Parses a BibTeX person name according to Patashnik's grammar:
     * <pre>
     *   First von Last
     *   von Last, Jr, First
     *   von Last, First
     * </pre>
     * Lowercase tokens before the family portion are treated as particles,
     * suffix tokens (e.g., Jr, PhD) are preserved, and braces are stripped before parsing.
     */
    private static BiboPersonName parseName(String rawName) {
        String normalized = normalizeBibTeXName(rawName);
        NameComponents components = parseAdvancedName(normalized);
        BiboPersonName.Builder builder = BiboPersonName.builder(normalized);
        if (components.givenName != null) {
            builder.givenName(components.givenName);
        }
        if (components.middleName != null) {
            builder.middleName(components.middleName);
        }
        if (components.nameParticle != null) {
            builder.nameParticle(components.nameParticle);
        }
        if (components.familyName != null) {
            builder.familyName(components.familyName);
        }
        if (components.suffix != null) {
            builder.suffix(components.suffix);
        }
        return builder.build();
    }

    private static String normalizeBibTeXName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String normalized = rawName.trim();
        while (normalized.startsWith("{") && normalized.endsWith("}") && normalized.length() > 2) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    /**
     * Splits BibTeX names into components by handling comma-separated segments first and
     * falling back to free-form parsing when commas are absent.
     */
    private static NameComponents parseAdvancedName(String normalized) {
        if (normalized.isEmpty()) {
            return new NameComponents(null, null, null, null, null);
        }
        String[] commaParts = normalized.split(",");
        if (commaParts.length == 1) {
            return parseFreeFormName(normalized);
        }

        if (commaParts.length == 2) {
            String firstSegment = commaParts[0].trim();
            String secondSegment = commaParts[1].trim();
            if (looksLikeSuffix(secondSegment)) {
                NameComponents base = parseFreeFormName(firstSegment);
                return base.withSuffix(secondSegment);
            }
            // Format: "von Last, First"
            NameParticlePair familyParts = extractParticleAndFamily(firstSegment);
            return new NameComponents(
                    secondSegment.isBlank() ? null : secondSegment,
                    null, // no middle name
                    familyParts.particle,
                    familyParts.family,
                    null);
        }

        // Three part format: von Last, Jr, First
        String familySegment = commaParts[0].trim();
        String suffixSegment = commaParts[1].trim();
        String givenSegment =
                Arrays.stream(commaParts).skip(2).map(String::trim).filter(token -> !token.isEmpty()).collect(Collectors.joining(" "));
        NameParticlePair familyParts = extractParticleAndFamily(familySegment);
        return new NameComponents(
                givenSegment.isBlank() ? null : givenSegment,
                null, // no middle name in this format
                familyParts.particle,
                familyParts.family,
                suffixSegment.isBlank() ? null : suffixSegment);
    }

    private static NameComponents parseFreeFormName(String value) {
        List<String> tokens = Arrays.stream(value.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));

        if (tokens.isEmpty()) {
            return new NameComponents(null, null, null, value, null);
        }

        // Single token = family name only
        if (tokens.size() == 1) {
            return new NameComponents(null, null, null, tokens.getFirst(), null);
        }

        // Extract family name + particle from the end
        List<String> particleTokens = new ArrayList<>();
        List<String> familyTokens = new ArrayList<>();
        int index = tokens.size() - 1;

        // Last token is always part of family name
        familyTokens.add(tokens.get(index));
        index--;

        // Collect particles (lowercase words before family name)
        while (index >= 0 && isParticleToken(tokens.get(index))) {
            particleTokens.addFirst(tokens.get(index));
            index--;
        }

        // Extract given name and middle name
        String givenName = null;
        String middleName = null;

        if (index >= 0) {
            List<String> givenTokens = tokens.subList(0, index + 1);
            if (givenTokens.size() == 1) {
                givenName = givenTokens.getFirst();
            } else if (givenTokens.size() >= 2) {
                // Last token of given tokens is middle name
                givenName = givenTokens.subList(0, givenTokens.size() - 1).stream()
                        .collect(Collectors.joining(" "));
                middleName = givenTokens.getLast();
            }
        }

        String particle = particleTokens.isEmpty() ? null : String.join(" ", particleTokens);
        String family = String.join(" ", familyTokens);

        return new NameComponents(
                givenName == null || givenName.isBlank() ? null : givenName.trim(),
                middleName == null || middleName.isBlank() ? null : middleName.trim(),
                particle == null || particle.isBlank() ? null : particle.trim(),
                family.isBlank() ? null : family.trim(),
                null);
    }

    private static boolean looksLikeSuffix(String segment) {
        if (segment == null || segment.isBlank()) {
            return false;
        }
        String key = segment.replace(".", "").trim().toLowerCase(Locale.ROOT);
        if (SUFFIX_TOKENS.contains(key)) {
            return true;
        }
        return Arrays.stream(segment.split("\\s+"))
                .map(token -> token.toLowerCase(Locale.ROOT))
                .anyMatch(PARTICLE_TOKENS::contains);
    }

    private static boolean isParticleToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String normalized = token.toLowerCase(Locale.ROOT);
        return PARTICLE_TOKENS.contains(normalized) || Character.isLowerCase(token.charAt(0));
    }

    private record NameComponents(String givenName, String middleName, String nameParticle, String familyName, String suffix) {
        NameComponents withSuffix(String newSuffix) {
            return new NameComponents(givenName, middleName, nameParticle, familyName, newSuffix == null || newSuffix.isBlank() ? suffix : newSuffix);
        }
    }

    private record NameParticlePair(String particle, String family) {}

    /**
     * Extracts name particle (von, van, de, etc.) and family name from a segment.
     * Particles are lowercase words before the family name portion.
     */
    private static NameParticlePair extractParticleAndFamily(String segment) {
        if (segment == null || segment.isBlank()) {
            return new NameParticlePair(null, null);
        }

        List<String> tokens = Arrays.stream(segment.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();

        if (tokens.isEmpty()) {
            return new NameParticlePair(null, null);
        }

        if (tokens.size() == 1) {
            return new NameParticlePair(null, tokens.getFirst());
        }

        List<String> particleTokens = new ArrayList<>();
        List<String> familyTokens = new ArrayList<>();

        // Start from the end: last token is always family name
        familyTokens.add(tokens.getLast());

        // Work backwards to find particles
        for (int i = tokens.size() - 2; i >= 0; i--) {
            if (isParticleToken(tokens.get(i))) {
                particleTokens.addFirst(tokens.get(i));
            } else {
                // Non-particle tokens at the start are part of family name
                for (int j = 0; j <= i; j++) {
                    familyTokens.addFirst(tokens.get(j));
                }
                break;
            }
        }

        String particle = particleTokens.isEmpty() ? null : String.join(" ", particleTokens);
        String family = String.join(" ", familyTokens);

        return new NameParticlePair(
                particle == null || particle.isBlank() ? null : particle.trim(),
                family.isBlank() ? null : family.trim());
    }

    private static Resource createResource(String identifier) {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        if (identifier.startsWith("_:")) {
            return vf.createBNode(identifier.substring(2));
        }
        return vf.createIRI(identifier);
    }

    private static List<Resource> selectDocumentSubjects(Model model) {
        // Collect containers (resources that are targets of isPartOf relations)
        Set<Resource> containers = new java.util.HashSet<>();
        model.filter(null, DCTERMS.IS_PART_OF, null).forEach(statement -> {
            org.eclipse.rdf4j.model.Value object = statement.getObject();
            if (object instanceof Resource resource) {
                containers.add(resource);
            }
        });

        LinkedHashSet<Resource> subjects = new LinkedHashSet<>();
        model.filter(null, RDF.TYPE, null).forEach(statement -> {
            org.eclipse.rdf4j.model.Value object = statement.getObject();
            if (object instanceof IRI type && type.stringValue().startsWith(BiboVocabulary.NAMESPACE)) {
                Resource subject = (Resource) statement.getSubject();
                // Exclude containers - they're not top-level documents
                if (!containers.contains(subject)) {
                    subjects.add(subject);
                }
            }
        });
        return new ArrayList<>(subjects);
    }

    private static BiboDocument buildDocumentFromModel(Model model, Resource subject) {
        String title = literal(model, subject, DCTERMS.TITLE)
                .orElseThrow(() -> new ValidationException("Document title is required in RDF source"));

        BiboDocumentType type =
                model.filter(subject, RDF.TYPE, null).objects().stream()
                        .filter(value -> value instanceof IRI)
                        .map(value -> (IRI) value)
                        .map(BiboDocumentType::fromIri)
                        .filter(candidate -> candidate != BiboDocumentType.OTHER)
                        .findFirst()
                        .orElse(BiboDocumentType.OTHER);

        BiboDocument.Builder builder = BiboDocument.builder(type, title);
        literal(model, subject, DCTERMS.IDENTIFIER).ifPresent(builder::id);
        literal(model, subject, BiboVocabulary.SUBTITLE).ifPresent(builder::subtitle);

        builder.contributors(readContributors(model, subject));
        parsePublicationDate(model, subject).ifPresent(builder::publicationDate);

        literal(model, subject, DCTERMS.PUBLISHER).ifPresent(builder::publisher);
        literal(model, subject, DCTERMS.SPATIAL).ifPresent(builder::placeOfPublication);
        literal(model, subject, DCTERMS.LANGUAGE).ifPresent(builder::language);
        literal(model, subject, DCTERMS.ABSTRACT).ifPresent(builder::abstractText);
        literal(model, subject, RDFS.COMMENT).ifPresent(builder::notes);

        literal(model, subject, BiboVocabulary.VOLUME).ifPresent(builder::volume);
        literal(model, subject, BiboVocabulary.ISSUE).ifPresent(builder::issue);
        literal(model, subject, BiboVocabulary.PAGES).ifPresent(builder::pages);
        literal(model, subject, BiboVocabulary.SERIES).ifPresent(builder::series);
        literal(model, subject, BiboVocabulary.EDITION).ifPresent(builder::edition);
        literal(model, subject, BiboVocabulary.DEGREE).ifPresent(builder::degreeType);

        containerTitle(model, subject).ifPresent(builder::containerTitle);
        conferenceLocation(model, subject).ifPresent(builder::conferenceLocation);
        conferenceOrganizer(model, subject).ifPresent(builder::conferenceOrganizer);
        literal(model, subject, BiboVocabulary.DEGREE).ifPresent(builder::degreeType);
        iriOrLiteral(model, subject, FOAF.PAGE).ifPresent(builder::url);

        builder.identifiers(readIdentifiers(model, subject));
        model.filter(subject, DCTERMS.SUBJECT, null).objects().stream()
                .filter(value -> value instanceof Literal)
                .map(value -> ((Literal) value).getLabel().trim())
                .filter(keyword -> !keyword.isEmpty())
                .forEach(builder::addKeyword);

        return builder.build();
    }

    private static List<BiboContributor> readContributors(Model model, Resource subject) {
        List<BiboContributor> contributors = new ArrayList<>();
        contributors.addAll(readContributorList(model, subject, BiboVocabulary.AUTHOR_LIST, BiboContributorRole.AUTHOR));
        contributors.addAll(readContributorList(model, subject, BiboVocabulary.EDITOR_LIST, BiboContributorRole.EDITOR));

        for (Map.Entry<IRI, BiboContributorRole> entry : CONTRIBUTOR_PREDICATES.entrySet()) {
            if (entry.getValue() == BiboContributorRole.AUTHOR || entry.getValue() == BiboContributorRole.EDITOR) {
                continue;
            }
            model.filter(subject, entry.getKey(), null).objects().stream()
                    .filter(value -> value instanceof Resource)
                    .map(value -> (Resource) value)
                    .forEach(resource ->
                            readPersonName(model, resource).ifPresent(name ->
                                    contributors.add(new BiboContributor(name, entry.getValue()))));
        }
        return contributors;
    }

    private static List<BiboContributor> readContributorList(
            Model model, Resource subject, IRI predicate, BiboContributorRole role) {
        List<BiboContributor> contributors = new ArrayList<>();
        model.filter(subject, predicate, null).objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .findFirst()
                .ifPresent(listHead -> {
                    List<org.eclipse.rdf4j.model.Value> values =
                            RDFCollections.asValues(model, listHead, new ArrayList<>());
                    for (org.eclipse.rdf4j.model.Value value : values) {
                        if (value instanceof Resource person) {
                            readPersonName(model, person)
                                    .ifPresent(name -> contributors.add(new BiboContributor(name, role)));
                        }
                    }
                });
        return contributors;
    }

    private static Optional<BiboPersonName> readPersonName(Model model, Resource person) {
        Optional<String> given = literal(model, person, FOAF.GIVEN_NAME);
        Optional<String> family = literal(model, person, FOAF.FAMILY_NAME);
        String label = literal(model, person, FOAF.NAME)
                .orElseGet(() -> Stream.of(given.orElse(null), family.orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")));

        if (label.isBlank()) {
            return Optional.empty();
        }

        BiboPersonName.Builder builder = BiboPersonName.builder(label);
        given.ifPresent(builder::givenName);
        family.ifPresent(builder::familyName);
        return Optional.of(builder.build());
    }

    private static List<BiboIdentifier> readIdentifiers(Model model, Resource subject) {
        List<BiboIdentifier> identifiers = new ArrayList<>();
        for (Map.Entry<IRI, BiboIdentifierType> entry : IDENTIFIER_PREDICATES.entrySet()) {
            model.filter(subject, entry.getKey(), null).objects().forEach(value -> {
                String text = value.stringValue().trim();
                if (!text.isEmpty()) {
                    identifiers.add(new BiboIdentifier(entry.getValue(), text));
                }
            });
        }
        return identifiers;
    }

    private static Optional<BiboPublicationDate> parsePublicationDate(Model model, Resource subject) {
        return Models.objectLiteral(model.filter(subject, DCTERMS.ISSUED, null)).flatMap(BibTeXBibliographicConverter::parseIssuedLiteral);
    }

    private static Optional<BiboPublicationDate> parseIssuedLiteral(Literal literal) {
        String label = literal.getLabel().trim();
        IRI datatype = literal.getDatatype();
        try {
            if (datatype != null) {
                if (XSD.DATE.equals(datatype) || XSD.DATETIME.equals(datatype)) {
                    LocalDate date = LocalDate.parse(label.substring(0, 10));
                    return Optional.of(BiboPublicationDate.ofFullDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
                }
                if (XSD.GYEARMONTH.equals(datatype)) {
                    String[] parts = label.split("-");
                    return Optional.of(BiboPublicationDate.ofYearMonth(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                }
                if (XSD.GYEAR.equals(datatype)) {
                    return Optional.of(BiboPublicationDate.ofYear(Integer.parseInt(label)));
                }
            }
            if (label.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate date = LocalDate.parse(label);
                return Optional.of(BiboPublicationDate.ofFullDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
            }
            if (label.matches("\\d{4}-\\d{2}")) {
                String[] parts = label.split("-");
                return Optional.of(BiboPublicationDate.ofYearMonth(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
            }
            if (label.matches("\\d{4}")) {
                return Optional.of(BiboPublicationDate.ofYear(Integer.parseInt(label)));
            }
        } catch (Exception ignored) {
            // ignore invalid date literal
        }
        return Optional.empty();
    }

    private static Optional<String> containerTitle(Model model, Resource subject) {
        return model.filter(subject, DCTERMS.IS_PART_OF, null).objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .map(container -> literal(model, container, DCTERMS.TITLE))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<String> conferenceLocation(Model model, Resource subject) {
        return model.filter(subject, DCTERMS.IS_PART_OF, null).objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .map(container -> literal(model, container, DCTERMS.SPATIAL))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<String> conferenceOrganizer(Model model, Resource subject) {
        return model.filter(subject, DCTERMS.IS_PART_OF, null).objects().stream()
                .filter(value -> value instanceof Resource)
                .map(value -> (Resource) value)
                .map(container -> literal(model, container, BiboVocabulary.ORGANIZER))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<String> literal(Model model, Resource subject, IRI predicate) {
        return Models.objectLiteral(model.filter(subject, predicate, null))
                .map(Literal::getLabel)
                .map(String::trim)
                .filter(text -> !text.isEmpty());
    }

    private static Optional<String> iriOrLiteral(Model model, Resource subject, IRI predicate) {
        return model.filter(subject, predicate, null).objects().stream()
                .map(value -> value instanceof Literal literal ? literal.getLabel() : value.stringValue())
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .findFirst();
    }

    private static Optional<BiboPublicationDate> parsePublicationDate(BibTeXEntry entry) {
        Optional<String> yearValue = fieldValue(entry, BibTeXEntry.KEY_YEAR);
        if (yearValue.isEmpty()) {
            return Optional.empty();
        }

        try {
            int year = Integer.parseInt(yearValue.get().trim());
            Optional<Integer> month =
                    fieldValue(entry, BibTeXEntry.KEY_MONTH).flatMap(BibTeXBibliographicConverter::parseMonth);
            Optional<Integer> day = fieldValue(entry, FIELD_DAY).flatMap(BibTeXBibliographicConverter::parseDay);

            if (month.isPresent() && day.isPresent()) {
                return Optional.of(BiboPublicationDate.ofFullDate(year, month.get(), day.get()));
            } else if (month.isPresent()) {
                return Optional.of(BiboPublicationDate.ofYearMonth(year, month.get()));
            } else {
                return Optional.of(BiboPublicationDate.ofYear(year));
            }
        } catch (NumberFormatException ex) {
            return Optional.of(BiboPublicationDate.ofYear(extractYearFromFreeForm(yearValue.get())));
        }
    }

    private static int extractYearFromFreeForm(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() >= 4) {
            return Integer.parseInt(digits.substring(0, 4));
        }
        throw new IllegalArgumentException("Cannot extract year from value: " + value);
    }

    private static Optional<Integer> parseMonth(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        if (MONTH_ALIASES.containsKey(normalized)) {
            return Optional.of(MONTH_ALIASES.get(normalized));
        }
        try {
            int month = Integer.parseInt(normalized);
            return (month >= 1 && month <= 12) ? Optional.of(month) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseDay(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        try {
            int day = Integer.parseInt(trimmed);
            return (day >= 1 && day <= 31) ? Optional.of(day) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static List<BiboIdentifier> extractIdentifiers(BibTeXEntry entry) {
        List<BiboIdentifier> identifiers = new ArrayList<>();
        fieldValue(entry, BibTeXEntry.KEY_DOI)
                .ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.DOI, value)));
        fieldValue(entry, BibTeXEntry.KEY_URL)
                .map(BibTeXBibliographicConverter::sanitizeUrl)
                .ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.URL, value)));
        fieldValue(entry, FIELD_HANDLE)
                .ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.HANDLE, value)));
        fieldValue(entry, FIELD_URI)
                .ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.URI, value)));

        fieldValue(entry, FIELD_ISBN)
                .ifPresent(value -> MULTI_VALUE_SEPARATOR.splitAsStream(value)
                        .map(String::trim)
                        .filter(token -> !token.isEmpty())
                        .map(BibTeXBibliographicConverter::classifyIsbn)
                        .flatMap(Optional::stream)
                        .forEach(identifiers::add));

        fieldValue(entry, FIELD_ISSN)
                .ifPresent(value -> MULTI_VALUE_SEPARATOR.splitAsStream(value)
                        .map(String::trim)
                        .filter(token -> !token.isEmpty())
                        .forEach(
                                token -> identifiers.add(new BiboIdentifier(BiboIdentifierType.ISSN, token))));

        return identifiers;
    }

    private static Optional<BiboIdentifier> classifyIsbn(String value) {
        String digits = value.replaceAll("[^0-9Xx]", "");
        if (digits.length() == 10) {
            return Optional.of(new BiboIdentifier(BiboIdentifierType.ISBN_10, value));
        } else if (digits.length() == 13) {
            return Optional.of(new BiboIdentifier(BiboIdentifierType.ISBN_13, value));
        } else {
            return Optional.of(new BiboIdentifier(BiboIdentifierType.OTHER, value));
        }
    }

    private static void addIdentifierFields(BibTeXEntry entry, List<BiboIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return;
        }

        for (BiboIdentifier identifier : identifiers) {
            switch (identifier.type()) {
                case DOI -> putField(entry, BibTeXEntry.KEY_DOI, identifier.value());
                case ISBN_10, ISBN_13, OTHER -> appendMultiValue(entry, FIELD_ISBN, identifier.value());
                case ISSN -> appendMultiValue(entry, FIELD_ISSN, identifier.value());
                case HANDLE -> putField(entry, FIELD_HANDLE, identifier.value());
                case URI -> putField(entry, FIELD_URI, identifier.value());
                case URL -> putField(entry, BibTeXEntry.KEY_URL, identifier.value());
            }
        }
    }

    private static void appendMultiValue(BibTeXEntry entry, Key field, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return;
        }
        String combined =
                fieldValue(entry, field).map(existing -> existing + ", " + trimmed).orElse(trimmed);
        entry.addField(field, new StringValue(combined, StringValue.Style.BRACED));
    }

    private static Optional<String> formatContributors(List<BiboContributor> contributors) {
        if (contributors == null || contributors.isEmpty()) {
            return Optional.empty();
        }
        String joined =
                contributors.stream().map(BibTeXBibliographicConverter::formatName).collect(Collectors.joining(" and "));
        return joined.isBlank() ? Optional.empty() : Optional.of(joined);
    }

    private static boolean containsCircaToken(String value) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.contains("circa") || lower.contains("c.") || lower.contains("~");
    }

    private static String formatCircaNote(String rawYear) {
        if (rawYear == null || rawYear.isBlank()) {
            return "Approximate publication date";
        }
        try {
            int year = extractYearFromFreeForm(rawYear);
            return "Approximate publication date (circa " + year + ")";
        } catch (Exception ex) {
            return "Approximate publication date (" + rawYear.trim() + ")";
        }
    }

    private static String combineNotes(String primary, String extra) {
        if (primary == null || primary.isBlank()) {
            return (extra == null || extra.isBlank()) ? null : extra.trim();
        }
        if (extra == null || extra.isBlank()) {
            return primary.trim();
        }
        return primary.trim() + " | " + extra.trim();
    }

    private static String formatName(BiboContributor contributor) {
        BiboPersonName name = contributor.name();
        Optional<String> family = name.familyName();
        Optional<String> given = name.givenName();
        Optional<String> suffix = name.suffix();

        if (family.isPresent() && given.isPresent()) {
            if (suffix.isPresent()) {
                return family.get() + ", " + suffix.get() + ", " + given.get();
            }
            return family.get() + ", " + given.get();
        }

        if (suffix.isPresent() && given.isPresent()) {
            return name.fullName() + ", " + suffix.get();
        }
        return name.fullName();
    }

    private static String sanitizeForKey(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("\\p{M}", "");
        String lowered = ascii.toLowerCase(Locale.ROOT);
        String collapsed = NON_ALPHANUMERIC.matcher(lowered).replaceAll("_");
        String trimmed = collapsed.replaceAll("^_+", "").replaceAll("_+$", "");
        return trimmed.isEmpty() ? "entry" : trimmed;
    }

    private static String monthToBibTeX(int month) {
        Set<Map.Entry<String, Integer>> entries = MONTH_ALIASES.entrySet();
        return entries.stream()
                .filter(entry -> entry.getValue() == month && entry.getKey().length() == 3)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(Integer.toString(month));
    }

    public enum KeyGenerationStrategy {
        AUTHOR_YEAR,
        AUTHOR_TITLE,
        HASH
    }
}
