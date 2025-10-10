package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import org.junit.jupiter.api.Test;

class BibliographicConverterTest {
    private final BibliographicConverter<BibTeXEntry> converter =
            new BibTeXBibliographicConverter();

    @Test
    void conversionStubsReturnEmptyOptional() {
        BiboDocument dummyDocument =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Dummy title").id("dummy").build();
        assertTrue(converter.convertFromBibo(dummyDocument).isEmpty());
        assertTrue(converter.convertToBibo(new BibTeXEntry("dummy")).isEmpty());
    }
}
