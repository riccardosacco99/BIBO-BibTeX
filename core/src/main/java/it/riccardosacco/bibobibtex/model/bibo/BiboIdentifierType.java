package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

public enum BiboIdentifierType {
    DOI(BiboVocabulary.DOI),
    ISBN_10(BiboVocabulary.ISBN10),
    ISBN_13(BiboVocabulary.ISBN13),
    ISSN(BiboVocabulary.ISSN),
    HANDLE(BiboVocabulary.HANDLE),
    URI(BiboVocabulary.URI),
    URL(FOAF.PAGE),
    OTHER(BiboVocabulary.IDENTIFIER);

    private final IRI predicate;

    BiboIdentifierType(IRI predicate) {
        this.predicate = predicate;
    }

    public Optional<IRI> predicate() {
        return Optional.ofNullable(predicate);
    }
}
