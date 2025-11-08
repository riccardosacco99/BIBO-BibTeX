package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;

public class BibTeXBibliographicConverter implements BibliographicConverter<BibTeXEntry> {
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

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s+and\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTI_VALUE_SEPARATOR = Pattern.compile("[,;]");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private static final Key FIELD_SUBTITLE = new Key("subtitle");
    private static final Key FIELD_DAY = new Key("day");
    private static final Key FIELD_HANDLE = new Key("handle");
    private static final Key FIELD_ISBN = new Key("isbn");
    private static final Key FIELD_ISSN = new Key("issn");
    private static final Key FIELD_LANGUAGE = new Key("language");
    private static final Key FIELD_ABSTRACT = new Key("abstract");
    private static final Key FIELD_URI = new Key("uri");
    private static final Key FIELD_KEYWORDS = new Key("keywords");
    private static final Key FIELD_ORGANIZATION = new Key("organization");
    private static final Key FIELD_HOWPUBLISHED = new Key("howpublished");
    private static final Key FIELD_TYPE = new Key("type");
    private static final Key TYPE_ONLINE = new Key("online");
    private static final Key TYPE_BOOKLET = new Key("booklet");
    private static final Key TYPE_MANUAL = new Key("manual");
    private static final Key TYPE_UNPUBLISHED = new Key("unpublished");

    @Override
    public Optional<BiboDocument> convertToBibo(BibTeXEntry source) {
        validateBibTeXEntry(source);

        String title = fieldValue(source, BibTeXEntry.KEY_TITLE).orElseGet(() -> citationKeyValue(source));
        if (title == null || title.isBlank()) {
            throw BibliographicConversionException.missingRequiredField("title");
        }

        BiboDocumentType documentType = mapDocumentType(source.getType());
        BiboDocument.Builder builder = BiboDocument.builder(documentType, title).id(citationKeyValue(source));

        fieldValue(source, FIELD_SUBTITLE).ifPresent(builder::subtitle);
        parseContributors(fieldValue(source, BibTeXEntry.KEY_AUTHOR), BiboContributorRole.AUTHOR)
                .forEach(builder::addContributor);
        parseContributors(fieldValue(source, BibTeXEntry.KEY_EDITOR), BiboContributorRole.EDITOR)
                .forEach(builder::addContributor);

        parsePublicationDate(source).ifPresent(builder::publicationDate);

        // Publisher field varies by type: publisher, school, or institution
        fieldValue(source, BibTeXEntry.KEY_PUBLISHER, BibTeXEntry.KEY_SCHOOL, BibTeXEntry.KEY_INSTITUTION)
                .ifPresent(builder::publisher);
        fieldValue(source, BibTeXEntry.KEY_ADDRESS).ifPresent(builder::placeOfPublication);

        fieldValue(source, BibTeXEntry.KEY_JOURNAL, BibTeXEntry.KEY_BOOKTITLE).ifPresent(builder::containerTitle);
        fieldValue(source, BibTeXEntry.KEY_VOLUME).ifPresent(builder::volume);
        fieldValue(source, BibTeXEntry.KEY_NUMBER).ifPresent(builder::issue);
        fieldValue(source, BibTeXEntry.KEY_PAGES).ifPresent(builder::pages);

        extractIdentifiers(source).forEach(builder::addIdentifier);
        fieldValue(source, BibTeXEntry.KEY_URL).ifPresent(builder::url);
        fieldValue(source, FIELD_LANGUAGE).ifPresent(builder::language);
        fieldValue(source, FIELD_ABSTRACT).ifPresent(builder::abstractText);
        fieldValue(source, BibTeXEntry.KEY_NOTE).ifPresent(builder::notes);

        // Extended fields (Sprint 01 - US-03)
        fieldValue(source, BibTeXEntry.KEY_SERIES).ifPresent(builder::series);
        fieldValue(source, BibTeXEntry.KEY_EDITION).ifPresent(builder::edition);
        fieldValue(source, FIELD_KEYWORDS).ifPresent(keywords -> parseKeywords(keywords).forEach(builder::addKeyword));
        fieldValue(source, FIELD_ORGANIZATION).ifPresent(builder::organization);
        fieldValue(source, FIELD_HOWPUBLISHED).ifPresent(builder::howPublished);

        // Sprint 02 - US-08: Handle degree type for thesis
        fieldValue(source, FIELD_TYPE).ifPresent(builder::degreeType);

        return Optional.of(builder.build());
    }

    @Override
    public Optional<BibTeXEntry> convertFromBibo(BiboDocument source) {
        validateBiboDocument(source);

        Key entryType = mapEntryType(source.type());
        String citationKey =
                source.id()
                        .or(() -> generateCitationKey(source.title(), source.authors()))
                        .orElse("untitled");

        BibTeXEntry entry = new BibTeXEntry(entryType, new Key(citationKey));
        putField(entry, BibTeXEntry.KEY_TITLE, source.title());
        source.subtitle().ifPresent(value -> putField(entry, FIELD_SUBTITLE, value));

        formatContributors(source.authors()).ifPresent(value -> putField(entry, BibTeXEntry.KEY_AUTHOR, value));
        formatContributors(source.editors()).ifPresent(value -> putField(entry, BibTeXEntry.KEY_EDITOR, value));

        source.publicationDate().ifPresent(date -> {
            putField(entry, BibTeXEntry.KEY_YEAR, Integer.toString(date.year()));
            date.month().ifPresent(month -> putField(entry, BibTeXEntry.KEY_MONTH, monthToBibTeX(month)));
            date.day().ifPresent(day -> putField(entry, FIELD_DAY, Integer.toString(day)));
        });

        source.publisher().ifPresent(value -> putField(entry, fieldForPublisher(entryType), value));
        source.placeOfPublication().ifPresent(value -> putField(entry, BibTeXEntry.KEY_ADDRESS, value));

        source.containerTitle().ifPresent(value -> putField(entry, fieldForContainer(entryType), value));
        source.volume().ifPresent(value -> putField(entry, BibTeXEntry.KEY_VOLUME, value));
        source.issue().ifPresent(value -> putField(entry, BibTeXEntry.KEY_NUMBER, value));
        source.pages().ifPresent(value -> putField(entry, BibTeXEntry.KEY_PAGES, value));

        addIdentifierFields(entry, source.identifiers());

        source.url().ifPresent(value -> putField(entry, BibTeXEntry.KEY_URL, value));
        source.language().ifPresent(value -> putField(entry, FIELD_LANGUAGE, value));
        source.abstractText().ifPresent(value -> putField(entry, FIELD_ABSTRACT, value));
        source.notes().ifPresent(value -> putField(entry, BibTeXEntry.KEY_NOTE, value));

        // Extended fields (Sprint 01 - US-03)
        source.series().ifPresent(value -> putField(entry, BibTeXEntry.KEY_SERIES, value));
        source.edition().ifPresent(value -> putField(entry, BibTeXEntry.KEY_EDITION, value));
        if (!source.keywords().isEmpty()) {
            putField(entry, FIELD_KEYWORDS, String.join(", ", source.keywords()));
        }
        source.organization().ifPresent(value -> putField(entry, FIELD_ORGANIZATION, value));
        source.howPublished().ifPresent(value -> putField(entry, FIELD_HOWPUBLISHED, value));

        // Sprint 02 - US-08: Output degree type for thesis
        source.degreeType().ifPresent(value -> putField(entry, FIELD_TYPE, value));

        return Optional.of(entry);
    }

    private static String citationKeyValue(BibTeXEntry entry) {
        Key key = entry.getKey();
        return key != null ? key.getValue() : null;
    }

    private static Optional<String> fieldValue(BibTeXEntry entry, Key key) {
        if (key == null) {
            return Optional.empty();
        }
        Value value = entry.getField(key);
        if (value == null) {
            return Optional.empty();
        }
        String text = value.toUserString().trim();
        if (text.isEmpty()) {
            return Optional.empty();
        }
        // Convert BibTeX LaTeX escapes to Unicode
        text = BibTeXUnicodeConverter.toUnicode(text);
        return Optional.of(text);
    }

    private static Optional<String> fieldValue(BibTeXEntry entry, Key primary, Key fallback) {
        return fieldValue(entry, primary).or(() -> fieldValue(entry, fallback));
    }

    private static Optional<String> fieldValue(BibTeXEntry entry, Key first, Key second, Key third) {
        return fieldValue(entry, first).or(() -> fieldValue(entry, second)).or(() -> fieldValue(entry, third));
    }

    private static void putField(BibTeXEntry entry, Key key, String value) {
        if (value == null) {
            return;
        }
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return;
        }
        entry.addField(key, new StringValue(trimmed, StringValue.Style.BRACED));
    }

    private static BiboDocumentType mapDocumentType(Key type) {
        if (type == null) {
            return BiboDocumentType.OTHER;
        } else if (BibTeXEntry.TYPE_ARTICLE.equals(type)) {
            return BiboDocumentType.ARTICLE;
        } else if (BibTeXEntry.TYPE_BOOK.equals(type)) {
            return BiboDocumentType.BOOK;
        } else if (BibTeXEntry.TYPE_INBOOK.equals(type) || BibTeXEntry.TYPE_INCOLLECTION.equals(type)) {
            return BiboDocumentType.BOOK_SECTION;
        } else if (BibTeXEntry.TYPE_INPROCEEDINGS.equals(type)) {
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
            return BiboDocumentType.UNPUBLISHED;
        }
        return BiboDocumentType.OTHER;
    }

    private static Key mapEntryType(BiboDocumentType type) {
        if (type == null) {
            return BibTeXEntry.TYPE_MISC;
        }
        return switch (type) {
            case ARTICLE -> BibTeXEntry.TYPE_ARTICLE;
            case BOOK -> BibTeXEntry.TYPE_BOOK;
            case BOOK_SECTION -> BibTeXEntry.TYPE_INCOLLECTION;
            case CONFERENCE_PAPER -> BibTeXEntry.TYPE_INPROCEEDINGS;
            case PROCEEDINGS -> BibTeXEntry.TYPE_PROCEEDINGS;
            case THESIS -> BibTeXEntry.TYPE_PHDTHESIS;
            case REPORT -> BibTeXEntry.TYPE_TECHREPORT;
            case WEBPAGE -> TYPE_ONLINE;
            case BOOKLET -> TYPE_BOOKLET;
            case MANUAL -> TYPE_MANUAL;
            case UNPUBLISHED -> TYPE_UNPUBLISHED;
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

    /**
     * Parses keywords from a string where keywords are separated by commas or semicolons.
     *
     * @param keywords the keywords string to parse
     * @return list of individual keywords
     * @since Sprint 01 - US-03
     */
    private static List<String> parseKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }
        return MULTI_VALUE_SEPARATOR.splitAsStream(keywords)
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toList());
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

    private static BiboPersonName parseName(String rawName) {
        BiboPersonName.Builder builder = BiboPersonName.builder(rawName);
        String normalized = rawName.trim();

        if (normalized.contains(",")) {
            String[] parts = normalized.split(",", 2);
            builder.familyName(parts[0].trim());
            builder.givenName(parts.length > 1 ? parts[1].trim() : null);
        } else {
            String[] tokens = normalized.split("\\s+");
            if (tokens.length > 1) {
                builder.givenName(tokens[0]);
                builder.familyName(tokens[tokens.length - 1]);
            }
        }
        return builder.build();
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
                        .filter(Optional::isPresent)
                        .map(Optional::get)
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

    private static String formatName(BiboContributor contributor) {
        BiboPersonName name = contributor.name();
        if (name.familyName().isPresent() && name.givenName().isPresent()) {
            return name.familyName().get() + ", " + name.givenName().get();
        }
        return name.fullName();
    }

    private static Optional<String> generateCitationKey(String title, List<BiboContributor> contributors) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        String base = sanitizeForKey(title);

        if (contributors != null && !contributors.isEmpty()) {
            String family =
                    contributors.stream()
                            .map(BiboContributor::name)
                            .map(BiboPersonName::familyName)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .orElseGet(() -> contributors.get(0).name().fullName());
            base = sanitizeForKey(family) + "_" + base;
        }
        return Optional.of(base);
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

    /**
     * Validates a BibTeX entry before conversion.
     *
     * @param entry the BibTeX entry to validate
     * @throws BibliographicConversionException if the entry is null or invalid
     */
    private static void validateBibTeXEntry(BibTeXEntry entry) {
        if (entry == null) {
            throw BibliographicConversionException.nullInput("BibTeX entry");
        }
        // Additional validation can be added here
        // Note: Title validation is done later since we allow citation key as fallback
    }

    /**
     * Validates a BIBO document before conversion.
     *
     * @param document the BIBO document to validate
     * @throws BibliographicConversionException if the document is null or missing required fields
     */
    private static void validateBiboDocument(BiboDocument document) {
        if (document == null) {
            throw BibliographicConversionException.nullInput("BIBO document");
        }

        // Validate required fields
        if (document.title() == null || document.title().isBlank()) {
            throw BibliographicConversionException.missingRequiredField("title");
        }

        if (document.type() == null) {
            throw BibliographicConversionException.missingRequiredField("type");
        }
    }
}
