package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntryType;
import org.junit.jupiter.api.Test;

class BibliographicConverterTest {
    private final BibliographicConverter<BibTeXEntry> converter =
            new BibTeXBibliographicConverter();

    @Test
    void convertToBiboReturnsDocumentForBasicEntry() {
        BibTeXEntry entry =
                BibTeXEntry.builder(BibTeXEntryType.ARTICLE, "doe2024")
                        .field("title", "An Example Article")
                        .field("author", "Doe, Jane and Doe, John")
                        .field("year", "2024")
                        .field("journal", "Journal of Examples")
                        .build();

        var result = converter.convertToBibo(entry);
        assertTrue(result.isPresent());
        BiboDocument document = result.get();
        assertEquals(BiboDocumentType.ARTICLE, document.type());
        assertEquals("An Example Article", document.title());
        assertEquals(2, document.authors().size());
        assertEquals("Journal of Examples", document.containerTitle().orElseThrow());
        assertEquals(2024, document.publicationDate().orElseThrow().year());
    }

    @Test
    void convertFromBiboProducesBibTeXEntry() {
        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.BOOK, "Example Book")
                        .addAuthor(
                                BiboPersonName.builder("Alice Author")
                                        .givenName("Alice")
                                        .familyName("Author")
                                        .build())
                        .publisher("Example Publisher")
                        .publicationDate(BiboPublicationDate.ofYear(2020))
                        .build();

        var result = converter.convertFromBibo(document);
        assertTrue(result.isPresent());
        BibTeXEntry entry = result.get();
        assertEquals(BibTeXEntryType.BOOK, entry.type());
        assertEquals("Example Book", entry.field("title").orElseThrow());
        assertEquals("Author, Alice", entry.field("author").orElseThrow());
        assertEquals("Example Publisher", entry.field("publisher").orElseThrow());
        assertEquals("2020", entry.field("year").orElseThrow());
    }
}
