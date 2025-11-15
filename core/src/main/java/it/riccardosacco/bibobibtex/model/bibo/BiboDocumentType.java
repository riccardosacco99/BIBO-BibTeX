package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;

public enum BiboDocumentType {
    ARTICLE(BiboVocabulary.ARTICLE),
    BOOK(BiboVocabulary.BOOK),
    BOOK_SECTION(BiboVocabulary.CHAPTER),
    THESIS(BiboVocabulary.THESIS),
    REPORT(BiboVocabulary.REPORT),
    CONFERENCE_PAPER(BiboVocabulary.CONFERENCE_PAPER),
    PROCEEDINGS(BiboVocabulary.PROCEEDINGS),
    WEBPAGE(BiboVocabulary.WEBPAGE),
    BOOKLET(BiboVocabulary.DOCUMENT),
    MANUAL(BiboVocabulary.MANUAL),
    MANUSCRIPT(BiboVocabulary.MANUSCRIPT),
    OTHER(BiboVocabulary.DOCUMENT);

    private static final Map<IRI, BiboDocumentType> BY_IRI =
            Collections.unmodifiableMap(
                    Arrays.stream(values()).collect(Collectors.toMap(
                            BiboDocumentType::iri,
                            Function.identity(),
                            (a, b) -> a)));

    private final IRI iri;

    BiboDocumentType(IRI iri) {
        this.iri = iri;
    }

    public IRI iri() {
        return iri;
    }

    public static BiboDocumentType fromIri(IRI iri) {
        if (iri == null) {
            return OTHER;
        }
        return BY_IRI.getOrDefault(iri, OTHER);
    }

    public static EnumSet<BiboDocumentType> primaryTypes() {
        return EnumSet.complementOf(EnumSet.of(OTHER));
    }
}
