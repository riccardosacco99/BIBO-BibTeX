package it.riccardosacco.bibobibtex.converter;

import java.util.Optional;

public interface BibliographicConverter<S, T> {
    Optional<T> convert(S source);
}
