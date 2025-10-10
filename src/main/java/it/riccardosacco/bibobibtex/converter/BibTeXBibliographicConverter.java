package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import java.util.Optional;

/**
 * Stub implementation that will eventually contain the BibTeX ↔ BIBO mapping rules.
 */
public class BibTeXBibliographicConverter implements BibliographicConverter<BibTeXEntry> {
    @Override
    public Optional<BiboDocument> convertToBibo(BibTeXEntry source) {
        return Optional.empty();
    }

    @Override
    public Optional<BibTeXEntry> convertFromBibo(BiboDocument source) {
        return Optional.empty();
    }
}
