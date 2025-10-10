package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntryType;
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

    @Override
    public Optional<BiboDocument> convertToBibo(BibTeXEntry source) {
        if (source == null) {
            return Optional.empty();
        }

        String title = source.field("title").orElse(source.citationKey());
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        BiboDocumentType documentType = mapDocumentType(source.type());
        BiboDocument.Builder builder = BiboDocument.builder(documentType, title).id(source.citationKey());

        source.field("subtitle").ifPresent(builder::subtitle);
        parseContributors(source.field("author"), BiboContributorRole.AUTHOR)
                .forEach(builder::addContributor);
        parseContributors(source.field("editor"), BiboContributorRole.EDITOR)
                .forEach(builder::addContributor);

        parsePublicationDate(source).ifPresent(builder::publicationDate);

        source.field("publisher").ifPresent(builder::publisher);
        source.field("address").ifPresent(builder::placeOfPublication);

        source.field("journal", "booktitle").ifPresent(builder::containerTitle);
        source.field("volume").ifPresent(builder::volume);
        source.field("number").ifPresent(builder::issue);
        source.field("pages").ifPresent(builder::pages);

        extractIdentifiers(source).forEach(builder::addIdentifier);
        source.field("url").ifPresent(builder::url);
        source.field("language").ifPresent(builder::language);
        source.field("abstract").ifPresent(builder::abstractText);
        source.field("note").ifPresent(builder::notes);

        return Optional.of(builder.build());
    }

    @Override
    public Optional<BibTeXEntry> convertFromBibo(BiboDocument source) {
        if (source == null) {
            return Optional.empty();
        }

        BibTeXEntryType type = mapEntryType(source.type());
        String citationKey =
                source.id()
                        .or(() -> generateCitationKey(source.title(), source.authors()))
                        .orElse("untitled");

        BibTeXEntry.Builder builder = BibTeXEntry.builder(type, citationKey);
        builder.field("title", source.title());
        source.subtitle().ifPresent(value -> builder.field("subtitle", value));

        formatContributors(source.authors()).ifPresent(value -> builder.field("author", value));
        formatContributors(source.editors()).ifPresent(value -> builder.field("editor", value));

        source.publicationDate().ifPresent(date -> {
            builder.field("year", Integer.toString(date.year()));
            date.month().ifPresent(month -> builder.field("month", monthToBibTeX(month)));
            date.day().ifPresent(day -> builder.field("day", Integer.toString(day)));
        });

        source.publisher().ifPresent(value -> builder.field(fieldForPublisher(type), value));
        source.placeOfPublication().ifPresent(value -> builder.field("address", value));

        source.containerTitle()
                .ifPresent(value -> builder.field(fieldForContainer(type), value));
        source.volume().ifPresent(value -> builder.field("volume", value));
        source.issue().ifPresent(value -> builder.field("number", value));
        source.pages().ifPresent(value -> builder.field("pages", value));

        addIdentifierFields(builder, source.identifiers());

        source.url().ifPresent(value -> builder.field("url", value));
        source.language().ifPresent(value -> builder.field("language", value));
        source.abstractText().ifPresent(value -> builder.field("abstract", value));
        source.notes().ifPresent(value -> builder.field("note", value));

        return Optional.of(builder.build());
    }

    private static BiboDocumentType mapDocumentType(BibTeXEntryType type) {
        if (type == null) {
            return BiboDocumentType.OTHER;
        }
        return switch (type) {
            case ARTICLE -> BiboDocumentType.ARTICLE;
            case BOOK -> BiboDocumentType.BOOK;
            case INBOOK, INCOLLECTION -> BiboDocumentType.BOOK_SECTION;
            case INPROCEEDINGS, PROCEEDINGS -> BiboDocumentType.CONFERENCE_PAPER;
            case MASTERSTHESIS, PHDTHESIS -> BiboDocumentType.THESIS;
            case TECHREPORT -> BiboDocumentType.REPORT;
            case ONLINE -> BiboDocumentType.WEBPAGE;
            default -> BiboDocumentType.OTHER;
        };
    }

    private static BibTeXEntryType mapEntryType(BiboDocumentType type) {
        if (type == null) {
            return BibTeXEntryType.MISC;
        }
        return switch (type) {
            case ARTICLE -> BibTeXEntryType.ARTICLE;
            case BOOK -> BibTeXEntryType.BOOK;
            case BOOK_SECTION -> BibTeXEntryType.INCOLLECTION;
            case CONFERENCE_PAPER -> BibTeXEntryType.INPROCEEDINGS;
            case THESIS -> BibTeXEntryType.PHDTHESIS;
            case REPORT -> BibTeXEntryType.TECHREPORT;
            case WEBPAGE -> BibTeXEntryType.ONLINE;
            default -> BibTeXEntryType.MISC;
        };
    }

    private static String fieldForPublisher(BibTeXEntryType type) {
        return switch (type) {
            case PHDTHESIS, MASTERSTHESIS -> "school";
            case TECHREPORT -> "institution";
            default -> "publisher";
        };
    }

    private static String fieldForContainer(BibTeXEntryType type) {
        return switch (type) {
            case ARTICLE -> "journal";
            case INPROCEEDINGS, PROCEEDINGS, INBOOK, INCOLLECTION -> "booktitle";
            default -> "booktitle";
        };
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
        Optional<String> yearValue = entry.field("year");
        if (yearValue.isEmpty()) {
            return Optional.empty();
        }

        try {
            int year = Integer.parseInt(yearValue.get().trim());
            Optional<Integer> month = entry.field("month").flatMap(BibTeXBibliographicConverter::parseMonth);
            Optional<Integer> day = entry.field("day").flatMap(BibTeXBibliographicConverter::parseDay);

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
        entry.field("doi").ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.DOI, value)));
        entry.field("url").ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.URL, value)));
        entry.field("handle").ifPresent(value -> identifiers.add(new BiboIdentifier(BiboIdentifierType.HANDLE, value)));

        entry.field("isbn")
                .ifPresent(
                        value -> MULTI_VALUE_SEPARATOR.splitAsStream(value)
                                .map(String::trim)
                                .filter(token -> !token.isEmpty())
                                .map(BibTeXBibliographicConverter::classifyIsbn)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .forEach(identifiers::add));

        entry.field("issn")
                .ifPresent(
                        value -> MULTI_VALUE_SEPARATOR.splitAsStream(value)
                                .map(String::trim)
                                .filter(token -> !token.isEmpty())
                                .forEach(token ->
                                        identifiers.add(new BiboIdentifier(BiboIdentifierType.ISSN, token))));

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

    private static void addIdentifierFields(BibTeXEntry.Builder builder, List<BiboIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return;
        }

        for (BiboIdentifier identifier : identifiers) {
            switch (identifier.type()) {
                case DOI -> builder.field("doi", identifier.value());
                case ISBN_10, ISBN_13, OTHER -> appendMultiValue(builder, "isbn", identifier.value());
                case ISSN -> appendMultiValue(builder, "issn", identifier.value());
                case HANDLE -> builder.field("handle", identifier.value());
                case URL -> builder.field("url", identifier.value());
            }
        }
    }

    private static void appendMultiValue(BibTeXEntry.Builder builder, String field, String value) {
        builder.appendField(field, value, ", ");
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
}
