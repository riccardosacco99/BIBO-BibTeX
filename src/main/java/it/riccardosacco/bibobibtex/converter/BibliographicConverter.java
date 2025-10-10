package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.Optional;

public interface BibliographicConverter<T> {
    Optional<BiboDocument> convertToBibo(T source);

    Optional<T> convertFromBibo(BiboDocument source);
}
