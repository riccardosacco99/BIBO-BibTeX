package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibtex.BibTeXEntry;
import java.util.Optional;

public class BiboToBibTeXConverter implements BibliographicConverter<BiboDocument, BibTeXEntry> {
    @Override
    public Optional<BibTeXEntry> convert(BiboDocument source) {
        return Optional.empty();
    }
}
