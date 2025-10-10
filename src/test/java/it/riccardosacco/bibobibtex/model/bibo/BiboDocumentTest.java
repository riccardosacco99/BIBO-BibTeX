package it.riccardosacco.bibobibtex.model.bibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BiboDocumentTest {
    @Test
    void builderPopulatesDocumentMetadata() {
        BiboPersonName author =
                BiboPersonName.builder("Ada Lovelace").givenName("Ada").familyName("Lovelace").build();
        BiboPersonName editor = BiboPersonName.builder("Charles Babbage").build();

        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Sketch of the Analytical Engine")
                        .id("doc-1")
                        .subtitle("With Notes by the Translator")
                        .addAuthor(author)
                        .addEditor(editor)
                        .publicationDate(BiboPublicationDate.ofFullDate(1843, 8, 1))
                        .publisher("Brettell")
                        .placeOfPublication("London")
                        .containerTitle("Scientific Memoirs")
                        .volume("3")
                        .issue("8")
                        .pages("666-699")
                        .addIdentifier(
                                new BiboIdentifier(
                                        BiboIdentifierType.DOI, "10.0000/analytical-engine-notes"))
                        .url("https://example.org/analytical-engine")
                        .language("en")
                        .abstractText("Interpretation of the Analytical Engine and its potential.")
                        .notes("First published in French and translated into English.")
                        .build();

        assertEquals("doc-1", document.id().orElseThrow());
        assertEquals(BiboDocumentType.ARTICLE, document.type());
        assertEquals("Sketch of the Analytical Engine", document.title());
        assertEquals("With Notes by the Translator", document.subtitle().orElseThrow());

        assertEquals(2, document.contributors().size());
        assertEquals(1, document.authors().size());
        assertEquals(1, document.editors().size());
        assertEquals(List.of(document.authors().get(0), document.editors().get(0)), document.contributors());
        assertEquals("Ada Lovelace", document.authors().get(0).name().fullName());
        assertEquals(BiboContributorRole.AUTHOR, document.authors().get(0).role());
        assertEquals("Charles Babbage", document.editors().get(0).name().fullName());
        assertEquals(BiboContributorRole.EDITOR, document.editors().get(0).role());

        BiboPublicationDate publicationDate = document.publicationDate().orElseThrow();
        assertEquals(1843, publicationDate.year());
        assertEquals(8, publicationDate.month().orElseThrow());
        assertEquals(1, publicationDate.day().orElseThrow());

        assertEquals("Brettell", document.publisher().orElseThrow());
        assertEquals("London", document.placeOfPublication().orElseThrow());
        assertEquals("Scientific Memoirs", document.containerTitle().orElseThrow());
        assertEquals("3", document.volume().orElseThrow());
        assertEquals("8", document.issue().orElseThrow());
        assertEquals("666-699", document.pages().orElseThrow());

        assertEquals(1, document.identifiers().size());
        BiboIdentifier identifier = document.identifiers().get(0);
        assertEquals(BiboIdentifierType.DOI, identifier.type());
        assertEquals("10.0000/analytical-engine-notes", identifier.value());

        assertEquals("https://example.org/analytical-engine", document.url().orElseThrow());
        assertEquals("en", document.language().orElseThrow());
        assertEquals(
                "Interpretation of the Analytical Engine and its potential.",
                document.abstractText().orElseThrow());
        assertEquals(
                "First published in French and translated into English.", document.notes().orElseThrow());

        assertThrows(
                UnsupportedOperationException.class,
                () -> document.contributors().add(document.contributors().get(0)));
        assertThrows(
                UnsupportedOperationException.class, () -> document.identifiers().add(identifier));

        assertTrue(publicationDate.toLocalDate().isPresent());
        assertFalse(BiboPublicationDate.ofYear(1900).month().isPresent());
        assertFalse(BiboPublicationDate.ofYearMonth(1900, 5).day().isPresent());
    }
}
