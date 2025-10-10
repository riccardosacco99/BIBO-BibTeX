package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import org.junit.jupiter.api.Test;

class BibliographicConverterTest {
    private final BiboToBibTeXConverter toBibTeX = new BiboToBibTeXConverter();
    private final BibTeXToBiboConverter toBibo = new BibTeXToBiboConverter();

    @Test
    void conversionStubsReturnEmptyOptional() {
        assertTrue(toBibTeX.convert(new BiboDocument("dummy")).isEmpty());
        assertTrue(toBibo.convert(new BibTeXEntry("dummy")).isEmpty());
    }
}
