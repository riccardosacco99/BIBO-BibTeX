package it.riccardosacco.bibobibtex.model.bibo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class BiboVocabulary {
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static final String NAMESPACE = "http://purl.org/ontology/bibo/";
    public static final Namespace NS = new SimpleNamespace("bibo", NAMESPACE);

    private BiboVocabulary() {
        // utility class
    }

    private static IRI iri(String localName) {
        return VF.createIRI(NAMESPACE, localName);
    }

    public static final IRI DOCUMENT = iri("Document");
    public static final IRI ARTICLE = iri("Article");
    public static final IRI BOOK = iri("Book");
    public static final IRI CHAPTER = iri("Chapter");
    public static final IRI THESIS = iri("Thesis");
    public static final IRI REPORT = iri("Report");
    public static final IRI CONFERENCE_PAPER = iri("ConferencePaper");
    public static final IRI WEBPAGE = iri("Webpage");

    public static final IRI SUBTITLE = iri("subtitle");
    public static final IRI VOLUME = iri("volume");
    public static final IRI ISSUE = iri("issue");
    public static final IRI PAGES = iri("pages");

    public static final IRI DOI = iri("doi");
    public static final IRI HANDLE = iri("handle");
    public static final IRI ISSN = iri("issn");
    public static final IRI ISBN10 = iri("isbn10");
    public static final IRI ISBN13 = iri("isbn13");
    public static final IRI URI = iri("uri");
    public static final IRI IDENTIFIER = iri("identifier");

    public static final IRI CONTRIBUTOR = iri("contributor");
    public static final IRI EDITOR = iri("editor");
    public static final IRI TRANSLATOR = iri("translator");
    public static final IRI ADVISOR = iri("advisor");
    public static final IRI REVIEWER = iri("reviewer");
    public static final IRI ORDER = iri("sequence");
}
