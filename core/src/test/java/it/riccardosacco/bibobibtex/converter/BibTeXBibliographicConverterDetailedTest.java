package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.util.Optional;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.junit.jupiter.api.Test;

class BibTeXBibliographicConverterDetailedTest {
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
    private static final Key FIELD_DAY = new Key("day");
    private static final Key FIELD_LANGUAGE = new Key("language");
    private static final Key FIELD_ABSTRACT = new Key("abstract");
    private static final Key FIELD_ISBN = new Key("isbn");
    private static final Key FIELD_ISSN = new Key("issn");
    private static final Key FIELD_URI = new Key("uri");

    @Test
    void convertToBiboMapsStandardFields() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_INPROCEEDINGS, new Key("smith2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Proceedings Example");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, Alice and Doe, Bob");
        addField(entry, BibTeXEntry.KEY_EDITOR, "Editor, Evan");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "feb");
        addField(entry, FIELD_DAY, "10");
        addField(entry, BibTeXEntry.KEY_PUBLISHER, "ACM Press");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "New York, USA");
        addField(entry, BibTeXEntry.KEY_BOOKTITLE, "Conference on Examples");
        addField(entry, BibTeXEntry.KEY_VOLUME, "12");
        addField(entry, BibTeXEntry.KEY_NUMBER, "4");
        addField(entry, BibTeXEntry.KEY_PAGES, "10-20");
        addField(entry, BibTeXEntry.KEY_DOI, "10.1000/example-doi");
        addField(entry, FIELD_ISBN, "978-1-4028-9462-6");
        addField(entry, FIELD_ISSN, "1234-5678");
        addField(entry, FIELD_URI, "urn:test:identifier");
        addField(entry, BibTeXEntry.KEY_URL, "https://example.org/proc");
        addField(entry, FIELD_LANGUAGE, "en");
        addField(entry, FIELD_ABSTRACT, "Example abstract.");
        addField(entry, BibTeXEntry.KEY_NOTE, "Example note.");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(BiboDocumentType.CONFERENCE_PAPER, document.type());
        assertEquals("Proceedings Example", document.title());
        assertEquals("Conference on Examples", document.containerTitle().orElseThrow());
        assertEquals("ACM Press", document.publisher().orElseThrow());
        assertEquals("New York, USA", document.placeOfPublication().orElseThrow());
        assertEquals("12", document.volume().orElseThrow());
        assertEquals("4", document.issue().orElseThrow());
        assertEquals("10-20", document.pages().orElseThrow());
        assertEquals(2, document.authors().size());
        assertEquals(1, document.editors().size());

        BiboPublicationDate publicationDate = document.publicationDate().orElseThrow();
        assertEquals(2023, publicationDate.year());
        assertEquals(2, publicationDate.month().orElseThrow());
        assertEquals(10, publicationDate.day().orElseThrow());

        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.DOI));
        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.ISBN_13));
        assertTrue(
                document.identifiers().stream()
                        .map(BiboIdentifier::type)
                        .anyMatch(type -> type == BiboIdentifierType.ISSN));
        assertTrue(
                document.identifiers().stream()
                        .anyMatch(identifier ->
                                identifier.type() == BiboIdentifierType.URI
                                        && identifier.value().equals("urn:test:identifier")));
        assertEquals("https://example.org/proc", document.url().orElseThrow());
        assertEquals("en", document.language().orElseThrow());
    }

    @Test
    void convertToBiboIgnoresNonNumericYearValues() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("nonNumericYear"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Legacy Manuscript");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Doe, Jane");
        addField(entry, BibTeXEntry.KEY_YEAR, "n.d.");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertTrue(document.publicationDate().isEmpty());
    }

    @Test
    void convertToBiboTreatsCorporateAuthorsAsSingleContributor() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("corporate2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Corporate Authorship Example");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "{Research and Development Department} and Doe, John");

        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertEquals(2, document.authors().size());

        BiboContributor corporate = document.authors().get(0);
        assertEquals("Research and Development Department", corporate.name().fullName());
        assertTrue(corporate.name().givenName().isEmpty());
        assertTrue(corporate.name().familyName().isEmpty());

        BiboContributor individual = document.authors().get(1);
        assertEquals("Doe, John", individual.name().fullName());
        assertTrue(individual.name().givenName().isPresent());
        assertTrue(individual.name().familyName().isPresent());
    }

    @Test
    void convertFromBiboMapsIdentifiersAndContributors() {
        BiboPersonName firstAuthor =
                BiboPersonName.builder("Alice Smith").givenName("Alice").familyName("Smith").build();
        BiboPersonName secondAuthor =
                BiboPersonName.builder("Bob Doe").givenName("Bob").familyName("Doe").build();
        BiboPersonName editor =
                BiboPersonName.builder("Evan Editor").givenName("Evan").familyName("Editor").build();

        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.CONFERENCE_PAPER, "Proceedings Example")
                        .id("smith2023")
                        .addContributor(new BiboContributor(firstAuthor, BiboContributorRole.AUTHOR))
                        .addContributor(new BiboContributor(secondAuthor, BiboContributorRole.AUTHOR))
                        .addContributor(new BiboContributor(editor, BiboContributorRole.EDITOR))
                        .publicationDate(BiboPublicationDate.ofYearMonth(2023, 2))
                        .publisher("ACM Press")
                        .placeOfPublication("New York, USA")
                        .containerTitle("Conference on Examples")
                        .volume("12")
                        .issue("4")
                        .pages("10-20")
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, "10.1000/example-doi"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "978-1-4028-9462-6"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISSN, "1234-5678"))
                        .addIdentifier(new BiboIdentifier(BiboIdentifierType.URI, "urn:test:identifier"))
                        .url("https://example.org/proc")
                        .language("en")
                        .abstractText("Example abstract.")
                        .notes("Example note.")
                        .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_INPROCEEDINGS, entry.getType());
        assertEquals("smith2023", entry.getKey().getValue());
        assertEquals("Proceedings Example", value(entry, BibTeXEntry.KEY_TITLE));
        assertEquals("Conference on Examples", value(entry, BibTeXEntry.KEY_BOOKTITLE));
        assertEquals("ACM Press", value(entry, BibTeXEntry.KEY_PUBLISHER));
        assertEquals("New York, USA", value(entry, BibTeXEntry.KEY_ADDRESS));
        assertEquals("12", value(entry, BibTeXEntry.KEY_VOLUME));
        assertEquals("4", value(entry, BibTeXEntry.KEY_NUMBER));
        assertEquals("10-20", value(entry, BibTeXEntry.KEY_PAGES));
        assertTrue(value(entry, BibTeXEntry.KEY_AUTHOR).contains("Smith, Alice"));
        assertTrue(value(entry, BibTeXEntry.KEY_AUTHOR).contains("Doe, Bob"));
        assertTrue(value(entry, BibTeXEntry.KEY_EDITOR).contains("Editor, Evan"));
        assertEquals("2023", value(entry, BibTeXEntry.KEY_YEAR));
        assertEquals("feb", value(entry, BibTeXEntry.KEY_MONTH));
        assertEquals("10.1000/example-doi", value(entry, BibTeXEntry.KEY_DOI));
        assertTrue(value(entry, FIELD_ISBN).contains("978-1-4028-9462-6"));
        assertTrue(value(entry, FIELD_ISSN).contains("1234-5678"));
        assertEquals("urn:test:identifier", value(entry, FIELD_URI));
        assertEquals("https://example.org/proc", value(entry, BibTeXEntry.KEY_URL));
        assertEquals("en", value(entry, FIELD_LANGUAGE));
        assertEquals("Example abstract.", value(entry, FIELD_ABSTRACT));
        assertEquals("Example note.", value(entry, BibTeXEntry.KEY_NOTE));
    }

    private static void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    private static String value(BibTeXEntry entry, Key key) {
        return Optional.ofNullable(entry.getField(key)).map(Value::toUserString).orElseThrow();
    }
}
