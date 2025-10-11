package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.util.Optional;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.junit.jupiter.api.Test;

class BibliographicConverterTest {
    private final BibliographicConverter<BibTeXEntry> converter =
            new BibTeXBibliographicConverter();

    @Test
    void convertToBiboReturnsDocumentForBasicEntry() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("doe2024"));
        addField(entry, BibTeXEntry.KEY_TITLE, "An Example Article");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Doe, Jane and Doe, John");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024");
        addField(entry, BibTeXEntry.KEY_JOURNAL, "Journal of Examples");

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
        assertEquals(BibTeXEntry.TYPE_BOOK, entry.getType());
        assertEquals("Example Book", value(entry, BibTeXEntry.KEY_TITLE));
        assertEquals("Author, Alice", value(entry, BibTeXEntry.KEY_AUTHOR));
        assertEquals("Example Publisher", value(entry, BibTeXEntry.KEY_PUBLISHER));
        assertEquals("2020", value(entry, BibTeXEntry.KEY_YEAR));
    }

    private static void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    private static String value(BibTeXEntry entry, Key key) {
        return Optional.ofNullable(entry.getField(key)).map(Value::toUserString).orElseThrow();
    }
}
