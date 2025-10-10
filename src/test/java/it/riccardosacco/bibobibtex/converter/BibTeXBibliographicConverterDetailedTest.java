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
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntryType;
import org.junit.jupiter.api.Test;

class BibTeXBibliographicConverterDetailedTest {
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    void convertToBiboMapsStandardFields() {
        BibTeXEntry entry =
                BibTeXEntry.builder(BibTeXEntryType.INPROCEEDINGS, "smith2023")
                        .field("title", "Proceedings Example")
                        .field("author", "Smith, Alice and Doe, Bob")
                        .field("editor", "Editor, Evan")
                        .field("year", "2023")
                        .field("month", "feb")
                        .field("day", "10")
                        .field("publisher", "ACM Press")
                        .field("address", "New York, USA")
                        .field("booktitle", "Conference on Examples")
                        .field("volume", "12")
                        .field("number", "4")
                        .field("pages", "10-20")
                        .field("doi", "10.1000/example-doi")
                        .field("isbn", "978-1-4028-9462-6")
                        .field("issn", "1234-5678")
                        .field("url", "https://example.org/proc")
                        .field("language", "en")
                        .field("abstract", "Example abstract.")
                        .field("note", "Example note.")
                        .build();

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
        assertEquals("https://example.org/proc", document.url().orElseThrow());
        assertEquals("en", document.language().orElseThrow());
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
                        .url("https://example.org/proc")
                        .language("en")
                        .abstractText("Example abstract.")
                        .notes("Example note.")
                        .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntryType.INPROCEEDINGS, entry.type());
        assertEquals("smith2023", entry.citationKey());
        assertEquals("Proceedings Example", entry.field("title").orElseThrow());
        assertEquals("Conference on Examples", entry.field("booktitle").orElseThrow());
        assertEquals("ACM Press", entry.field("publisher").orElseThrow());
        assertEquals("New York, USA", entry.field("address").orElseThrow());
        assertEquals("12", entry.field("volume").orElseThrow());
        assertEquals("4", entry.field("number").orElseThrow());
        assertEquals("10-20", entry.field("pages").orElseThrow());
        assertTrue(entry.field("author").map(value -> value.contains("Smith, Alice")).orElse(false));
        assertTrue(entry.field("author").map(value -> value.contains("Doe, Bob")).orElse(false));
        assertTrue(entry.field("editor").map(value -> value.contains("Editor, Evan")).orElse(false));
        assertEquals("2023", entry.field("year").orElseThrow());
        assertEquals("feb", entry.field("month").orElseThrow());
        assertEquals("10.1000/example-doi", entry.field("doi").orElseThrow());
        assertTrue(entry.field("isbn").map(value -> value.contains("978-1-4028-9462-6")).orElse(false));
        assertTrue(entry.field("issn").map(value -> value.contains("1234-5678")).orElse(false));
        assertEquals("https://example.org/proc", entry.field("url").orElseThrow());
        assertEquals("en", entry.field("language").orElseThrow());
        assertEquals("Example abstract.", entry.field("abstract").orElseThrow());
        assertEquals("Example note.", entry.field("note").orElseThrow());
    }
}
