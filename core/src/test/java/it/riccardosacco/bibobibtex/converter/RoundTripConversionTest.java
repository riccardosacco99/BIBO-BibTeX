package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifier;
import it.riccardosacco.bibobibtex.model.bibo.BiboIdentifierType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.ParseException;
import org.jbibtex.Value;
import org.junit.jupiter.api.Test;

class RoundTripConversionTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    void articleRoundTripPreservesJournalMetadata() {
        BibTeXEntry entry = loadBibTeX("/roundtrip/bib/article.bib");
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertBiboFieldEquals(document, "type", BiboDocumentType.ARTICLE);
        assertBiboFieldEquals(document, "containerTitle", "Journal of Bidirectional Conversion");
        assertBiboFieldEquals(document, "volume", "58");
        assertBiboFieldEquals(document, "issue", "2");
        assertBiboFieldEquals(document, "pages", "100-120");
        assertModelIsomorphic(loadRDF("/roundtrip/rdf/article.ttl"), document.rdfModel());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(roundTrip, "journal", "Journal of Bidirectional Conversion");
        assertBibTeXFieldEquals(roundTrip, "volume", "58");
        assertBibTeXFieldEquals(roundTrip, "number", "2");
        assertBibTeXFieldEquals(roundTrip, "pages", "100-120");
        assertBibTeXFieldEquals(roundTrip, "doi", "10.5555/article-2024");
        assertBibTeXFieldEquals(roundTrip, "issn", "1234-5678");

        assertModelIsomorphic(document.rdfModel(), converter.convertToBibo(roundTrip).orElseThrow().rdfModel());
    }

    @Test
    void bookRoundTripKeepsSeriesEditionAndKeywords() {
        BibTeXEntry entry = loadBibTeX("/roundtrip/bib/book.bib");
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertBiboFieldEquals(document, "type", BiboDocumentType.BOOK);
        assertBiboFieldEquals(document, "publisher", "Data Press");
        assertBiboFieldEquals(document, "series", "Linked Data Series");
        assertBiboFieldEquals(document, "edition", "2");
        assertBiboFieldEquals(document, "keywords", List.of("conversion", "rdf", "bibtex"));
        assertModelIsomorphic(loadRDF("/roundtrip/rdf/book.ttl"), document.rdfModel());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(roundTrip, "publisher", "Data Press");
        assertBibTeXFieldEquals(roundTrip, "series", "Linked Data Series");
        assertBibTeXFieldEquals(roundTrip, "edition", "2");
        assertBibTeXFieldEquals(roundTrip, "isbn", "978-1-4028-9462-6");
        assertBibTeXFieldEquals(roundTrip, "keywords", "conversion, rdf, bibtex");

        assertModelIsomorphic(document.rdfModel(), converter.convertToBibo(roundTrip).orElseThrow().rdfModel());
    }

    @Test
    void inproceedingsRoundTripSkipsOrganizationButKeepsConferenceInfo() {
        BibTeXEntry entry = loadBibTeX("/roundtrip/bib/inproceedings.bib");
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertBiboFieldEquals(document, "type", BiboDocumentType.CONFERENCE_PAPER);
        assertBiboFieldEquals(document, "containerTitle", "Proceedings of the Conversion Summit");
        assertBiboFieldEquals(document, "pages", "55-64");
        assertModelIsomorphic(loadRDF("/roundtrip/rdf/inproceedings.ttl"), document.rdfModel());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(roundTrip, "booktitle", "Proceedings of the Conversion Summit");
        assertBibTeXFieldEquals(roundTrip, "pages", "55-64");
        // US-24: Organization field now preserved (conference organizer convention)
        assertBibTeXFieldEquals(roundTrip, "organization", "Conversion Society");

        assertModelIsomorphic(document.rdfModel(), converter.convertToBibo(roundTrip).orElseThrow().rdfModel());
    }

    @Test
    void phdThesisRoundTripKeepsSchoolAndAdvisors() {
        BibTeXEntry entry = loadBibTeX("/roundtrip/bib/phdthesis.bib");
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();

        assertBiboFieldEquals(document, "type", BiboDocumentType.THESIS);
        assertBiboFieldEquals(document, "publisher", "University of Linked Data");
        assertEquals(2, document.contributorsByRole(BiboContributorRole.ADVISOR).size());

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertEquals(BibTeXEntry.TYPE_PHDTHESIS, roundTrip.getType());
        assertBibTeXFieldEquals(roundTrip, "school", "University of Linked Data");
        String advisorField = bibtexValue(roundTrip, new Key("advisor"));
        assertNotNull(advisorField);
        assertTrue(advisorField.contains("White, Sara"));
        assertTrue(advisorField.contains("Kim, Daniel"));

        assertModelIsomorphic(document.rdfModel(), converter.convertToBibo(roundTrip).orElseThrow().rdfModel());
    }

    @Test
    void miscRoundTripDropsHowPublishedButKeepsUrlAndNotes() {
        BibTeXEntry entry = loadBibTeX("/roundtrip/bib/misc.bib");
        BiboDocument document = converter.convertToBibo(entry).orElseThrow();
        assertBiboFieldEquals(document, "type", BiboDocumentType.OTHER);

        BibTeXEntry roundTrip = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(roundTrip, "url", "https://example.org/notes/conversion");
        assertBibTeXFieldEquals(roundTrip, "note", "Requires VPN");
        assertBibTeXFieldMissing(roundTrip, "howpublished");

        assertModelIsomorphic(document.rdfModel(), converter.convertToBibo(roundTrip).orElseThrow().rdfModel());
    }

    @Test
    void biboArticleRoundTripPreservesMetadata() {
        BiboDocument document = BiboDocument.builder(BiboDocumentType.ARTICLE, "Bidirectional Reliability")
                .subtitle("A Practical Guide")
                .addContributor(new BiboContributor(person("Smith, Alice", "Alice", "Smith"), BiboContributorRole.AUTHOR))
                .addContributor(new BiboContributor(person("Doe, Bob", "Bob", "Doe"), BiboContributorRole.AUTHOR))
                .addContributor(new BiboContributor(person("Editor, Eva", "Eva", "Editor"), BiboContributorRole.EDITOR))
                .publicationDate(BiboPublicationDate.ofYearMonth(2024, 4))
                .publisher("Knowledge Press")
                .containerTitle("International Journal of Round Trips")
                .volume("14")
                .issue("1")
                .pages("10-28")
                .addIdentifier(new BiboIdentifier(BiboIdentifierType.DOI, "10.1111/round-trip"))
                .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISSN, "2049-1234"))
                .url("https://example.org/articles/bidirectional")
                .language("en")
                .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(entry, "journal", "International Journal of Round Trips");
        assertBibTeXFieldEquals(entry, "volume", "14");
        assertBibTeXFieldEquals(entry, "number", "1");

        BiboDocument reconverted = converter.convertToBibo(entry).orElseThrow();
        assertEquals(document.type(), reconverted.type());
        assertEquals(document.containerTitle().orElseThrow(), reconverted.containerTitle().orElseThrow());
        assertEquals(document.volume().orElseThrow(), reconverted.volume().orElseThrow());
        assertEquals(document.issue().orElseThrow(), reconverted.issue().orElseThrow());
        assertEquals(document.pages().orElseThrow(), reconverted.pages().orElseThrow());
    }

    @Test
    void biboBookWithSeriesEditionRoundTrip() {
        BiboDocument document = BiboDocument.builder(BiboDocumentType.BOOK, "Handbook of Round Trips")
                .publisher("Library Press")
                .placeOfPublication("Berlin")
                .series("Advanced Conversion")
                .edition("3")
                .keywords(List.of("handbook", "roundtrip"))
                .addContributor(new BiboContributor(person("Garcia, Maria", "Maria", "Garcia"), BiboContributorRole.AUTHOR))
                .publicationDate(BiboPublicationDate.ofYear(2020))
                .addIdentifier(new BiboIdentifier(BiboIdentifierType.ISBN_13, "9780306406157"))
                .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(entry, "publisher", "Library Press");
        assertBibTeXFieldEquals(entry, "series", "Advanced Conversion");
        assertBibTeXFieldEquals(entry, "edition", "3");

        BiboDocument reconverted = converter.convertToBibo(entry).orElseThrow();
        assertEquals(document.publisher().orElseThrow(), reconverted.publisher().orElseThrow());
        assertEquals(document.series().orElseThrow(), reconverted.series().orElseThrow());
        assertEquals(document.edition().orElseThrow(), reconverted.edition().orElseThrow());
        assertEquals(document.keywords(), reconverted.keywords());
    }

    @Test
    void biboConferencePaperRoundTrip() {
        BiboDocument document = BiboDocument.builder(BiboDocumentType.CONFERENCE_PAPER, "Conference Pipelines")
                .containerTitle("Proceedings of the Pipeline Summit")
                .pages("200-210")
                .publicationDate(BiboPublicationDate.ofYear(2023))
                .addContributor(new BiboContributor(person("Lee, Hana", "Hana", "Lee"), BiboContributorRole.AUTHOR))
                .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(entry, "booktitle", "Proceedings of the Pipeline Summit");
        assertBibTeXFieldEquals(entry, "pages", "200-210");

        BiboDocument reconverted = converter.convertToBibo(entry).orElseThrow();
        assertEquals(document.containerTitle().orElseThrow(), reconverted.containerTitle().orElseThrow());
        assertEquals(document.pages().orElse(null), reconverted.pages().orElse(null));
    }

    @Test
    void biboThesisRoundTripPreservesAdvisors() {
        BiboDocument document = BiboDocument.builder(BiboDocumentType.THESIS, "Resilient Converters")
                .publisher("Global University")
                .addContributor(new BiboContributor(person("Rao, Priya", "Priya", "Rao"), BiboContributorRole.AUTHOR))
                .addContributor(new BiboContributor(person("Smith, Adam", "Adam", "Smith"), BiboContributorRole.ADVISOR))
                .addContributor(new BiboContributor(person("Jones, Carla", "Carla", "Jones"), BiboContributorRole.ADVISOR))
                .publicationDate(BiboPublicationDate.ofYear(2021))
                .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(entry, "school", "Global University");
        assertBibTeXFieldEquals(entry, "advisor", "Smith, Adam and Jones, Carla");

        BiboDocument reconverted = converter.convertToBibo(entry).orElseThrow();
        assertEquals(2, reconverted.contributorsByRole(BiboContributorRole.ADVISOR).size());
        assertEquals(document.publisher().orElseThrow(), reconverted.publisher().orElseThrow());
    }

    @Test
    void biboWebPageRoundTrip() {
        BiboDocument document = BiboDocument.builder(BiboDocumentType.WEBPAGE, "Project Homepage")
                .url("https://example.org/web")
                .addContributor(new BiboContributor(person("Taylor, Sam", "Sam", "Taylor"), BiboContributorRole.AUTHOR))
                .notes("Landing page")
                .build();

        BibTeXEntry entry = converter.convertFromBibo(document).orElseThrow();
        assertBibTeXFieldEquals(entry, "title", "Project Homepage");
        assertBibTeXFieldEquals(entry, "url", "https://example.org/web");

        BiboDocument reconverted = converter.convertToBibo(entry).orElseThrow();
        assertEquals(document.url().orElseThrow(), reconverted.url().orElseThrow());
        assertEquals(document.notes().orElseThrow(), reconverted.notes().orElseThrow());
    }

    private BibTeXEntry loadBibTeX(String resourcePath) {
        try (Reader reader = new InputStreamReader(openResource(resourcePath), StandardCharsets.UTF_8)) {
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase database = parser.parse(reader);
            return database.getEntries().values().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No entries in " + resourcePath));
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Unable to load BibTeX resource " + resourcePath, e);
        }
    }

    private Model loadRDF(String resourcePath) {
        try (InputStream stream = openResource(resourcePath)) {
            return Rio.parse(stream, "", RDFFormat.TURTLE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load RDF resource " + resourcePath, e);
        }
    }

    private InputStream openResource(String resourcePath) {
        InputStream stream = RoundTripConversionTest.class.getResourceAsStream(resourcePath);
        return Objects.requireNonNull(stream, "Missing resource: " + resourcePath);
    }

    private void assertBibTeXFieldEquals(BibTeXEntry entry, String field, String expected) {
        assertEquals(expected, bibtexValue(entry, keyFor(field)), "Mismatch for BibTeX field " + field);
    }

    private void assertBibTeXFieldMissing(BibTeXEntry entry, String field) {
        assertNull(bibtexValue(entry, keyFor(field)), "Expected BibTeX field to be absent: " + field);
    }

    private void assertBiboFieldEquals(BiboDocument document, String field, Object expected) {
        Object actual = switch (field) {
            case "type" -> document.type();
            case "title" -> document.title();
            case "containerTitle" -> document.containerTitle().orElse(null);
            case "publisher" -> document.publisher().orElse(null);
            case "placeOfPublication" -> document.placeOfPublication().orElse(null);
            case "volume" -> document.volume().orElse(null);
            case "issue" -> document.issue().orElse(null);
            case "pages" -> document.pages().orElse(null);
            case "url" -> document.url().orElse(null);
            case "language" -> document.language().orElse(null);
            case "series" -> document.series().orElse(null);
            case "edition" -> document.edition().orElse(null);
            case "keywords" -> document.keywords();
            default -> throw new IllegalArgumentException("Unsupported field: " + field);
        };
        assertEquals(expected, actual, "Mismatch for BIBO field " + field);
    }

    private void assertModelIsomorphic(Model expected, Model actual) {
        assertTrue(Models.isomorphic(expected, actual), "RDF models are not isomorphic");
    }

    private String bibtexValue(BibTeXEntry entry, Key key) {
        Value value = entry.getField(key);
        return value == null ? null : value.toUserString();
    }

    private Key keyFor(String field) {
        return switch (field.toLowerCase(Locale.ROOT)) {
            case "title" -> BibTeXEntry.KEY_TITLE;
            case "author" -> BibTeXEntry.KEY_AUTHOR;
            case "editor" -> BibTeXEntry.KEY_EDITOR;
            case "journal" -> BibTeXEntry.KEY_JOURNAL;
            case "booktitle" -> BibTeXEntry.KEY_BOOKTITLE;
            case "publisher" -> BibTeXEntry.KEY_PUBLISHER;
            case "school" -> new Key("school");
            case "institution" -> new Key("institution");
            case "year" -> BibTeXEntry.KEY_YEAR;
            case "month" -> BibTeXEntry.KEY_MONTH;
            case "volume" -> BibTeXEntry.KEY_VOLUME;
            case "number" -> BibTeXEntry.KEY_NUMBER;
            case "pages" -> BibTeXEntry.KEY_PAGES;
            case "doi" -> BibTeXEntry.KEY_DOI;
            case "url" -> BibTeXEntry.KEY_URL;
            case "note" -> BibTeXEntry.KEY_NOTE;
            case "series" -> new Key("series");
            case "edition" -> new Key("edition");
            case "keywords" -> new Key("keywords");
            case "organization" -> new Key("organization");
            case "howpublished" -> new Key("howpublished");
            case "advisor" -> new Key("advisor");
            case "issn" -> new Key("issn");
            case "isbn" -> new Key("isbn");
            default -> new Key(field);
        };
    }

    private BiboPersonName person(String fullName, String givenName, String familyName) {
        return BiboPersonName.builder(fullName)
                .givenName(givenName)
                .familyName(familyName)
                .build();
    }
}
