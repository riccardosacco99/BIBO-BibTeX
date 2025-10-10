package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.Optional;

public class BibTeXToBiboConverter implements BibliographicConverter<BibTeXEntry, BiboDocument> {
    @Override
    public Optional<BiboDocument> convert(BibTeXEntry source) {
        return Optional.empty();
    }
}
