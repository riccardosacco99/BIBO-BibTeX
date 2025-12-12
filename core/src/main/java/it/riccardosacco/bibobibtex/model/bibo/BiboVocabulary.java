package it.riccardosacco.bibobibtex.model.bibo;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class BiboVocabulary {
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    // Standard BIBO namespace
    public static final String NAMESPACE = "http://purl.org/ontology/bibo/";
    public static final Namespace NS = new SimpleNamespace("bibo", NAMESPACE);

    // BIBO Extension namespace (for properties not in standard BIBO)
    public static final String EXT_NAMESPACE = "http://purl.org/ontology/bibo-ext/";
    public static final Namespace EXT_NS = new SimpleNamespace("bibo-ext", EXT_NAMESPACE);

    private BiboVocabulary() {
        // utility class
    }

    private static IRI iri(String localName) {
        return VF.createIRI(NAMESPACE, localName);
    }

    private static IRI extIri(String localName) {
        return VF.createIRI(EXT_NAMESPACE, localName);
    }

    public static final IRI DOCUMENT = iri("Document");
    public static final IRI ARTICLE = iri("Article");
    public static final IRI BOOK = iri("Book");
    public static final IRI CHAPTER = iri("Chapter");
    public static final IRI THESIS = iri("Thesis");
    public static final IRI REPORT = iri("Report");
    public static final IRI CONFERENCE_PAPER = iri("ConferencePaper");
    public static final IRI PROCEEDINGS = iri("Proceedings");
    public static final IRI WEBPAGE = iri("Webpage");
    public static final IRI MANUAL = iri("Manual");
    public static final IRI MANUSCRIPT = iri("Manuscript");

    public static final IRI SUBTITLE = iri("subtitle");
    public static final IRI VOLUME = iri("volume");
    public static final IRI ISSUE = iri("issue");
    public static final IRI PAGES = iri("pages");
    public static final IRI SERIES = iri("series");
    public static final IRI EDITION = iri("edition");

    // Extended metadata (US-16)
    public static final IRI NUM_PAGES = iri("numPages");
    public static final IRI PAGE_START = iri("pageStart");
    public static final IRI PAGE_END = iri("pageEnd");
    public static final IRI CHAPTER_NUMBER = iri("chapter");
    public static final IRI STATUS = iri("status");

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

    // RDF Lists for ordered contributors (FIX-01)
    public static final IRI AUTHOR_LIST = iri("authorList");
    public static final IRI EDITOR_LIST = iri("editorList");
    public static final IRI CONTRIBUTOR_LIST = iri("contributorList");

    // Event and thesis properties
    public static final IRI PRESENTED_AT = iri("presentedAt");
    public static final IRI ORGANIZER = iri("organizer");
    public static final IRI DEGREE = iri("degree");
    public static final IRI THESIS_DEGREE = iri("ThesisDegree");

    // =========================================================================
    // BIBO Extension (bibo-ext:) - Properties not in standard BIBO
    // =========================================================================

    // Extended identifier properties
    public static final IRI EXT_EISSN = extIri("eissn");
    public static final IRI EXT_PMID = extIri("pmid");
    public static final IRI EXT_PMCID = extIri("pmcid");
    public static final IRI EXT_ARXIV_ID = extIri("arxivId");
    public static final IRI EXT_MR = extIri("mr");
    public static final IRI EXT_ZBL = extIri("zbl");

    // Author/contributor metadata
    public static final IRI EXT_ORCID = extIri("orcid");
    public static final IRI EXT_AFFILIATION = extIri("affiliation");
    public static final IRI EXT_AFFILIATION_NAME = extIri("affiliationName");

    // Extended document types
    public static final IRI EXT_DATASET = extIri("Dataset");
    public static final IRI EXT_SOFTWARE = extIri("Software");
    public static final IRI EXT_PREPRINT = extIri("Preprint");
    public static final IRI EXT_STANDARD = extIri("Standard");
    public static final IRI EXT_ONLINE = extIri("Online");

    // BibTeX-specific metadata
    public static final IRI EXT_HOWPUBLISHED = extIri("howpublished");
    public static final IRI EXT_CROSSREF = extIri("crossref");
    public static final IRI EXT_KEY = extIri("key");
    public static final IRI EXT_ANNOTATION = extIri("annotation");
    public static final IRI EXT_KEYWORDS = extIri("keywords");
    public static final IRI EXT_LANGUAGE = extIri("language");
    public static final IRI EXT_VERSION = extIri("version");

    // Publication venue properties
    public static final IRI EXT_JOURNAL_SUBTITLE = extIri("journalSubtitle");
    public static final IRI EXT_SHORT_JOURNAL = extIri("shortJournal");
    public static final IRI EXT_EVENT_TITLE = extIri("eventTitle");
    public static final IRI EXT_VENUE = extIri("venue");
}
