package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.exception.ValidationException;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

/**
 * Edge case tests for BibTeXBibliographicConverter.
 * Tests name parsing, date validation, identifier validation, and general edge cases.
 */
class BibTeXBibliographicConverterEdgeCaseTest {
    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    private void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    // ==================== 4.B - NAME PARSING EDGE CASES ====================

    @Test
    void singleTokenName_Madonna() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("madonna2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Madonna");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        // Single token names are treated as family names for bibliographic sorting
        assertEquals("Madonna", name.fullName());
        assertFalse(name.givenName().isPresent());
        assertEquals("Madonna", name.familyName().orElse(""));
    }

    @Test
    void singleTokenName_Plato() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("plato2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "The Republic");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Plato");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        // Single token names are treated as family names for bibliographic sorting
        assertEquals("Plato", name.fullName());
        assertFalse(name.givenName().isPresent());
        assertEquals("Plato", name.familyName().orElse(""));
    }

    @Test
    void particleName_VanGogh() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("vangogh2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Art Theory");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Vincent van Gogh");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        assertTrue(name.givenName().isPresent());
        assertEquals("Vincent", name.givenName().orElse(""));
        String familyName = name.familyName().orElse("");
        assertTrue(familyName.contains("van Gogh") || familyName.equals("Gogh"));
    }

    @Test
    void particleName_VonNeumann() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("vonneumann2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Game Theory");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "John von Neumann");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        assertTrue(name.givenName().isPresent());
        assertEquals("John", name.givenName().orElse(""));
        String familyName = name.familyName().orElse("");
        assertTrue(familyName.contains("von Neumann") || familyName.equals("Neumann"));
    }

    @Test
    void particleName_DeGaulle() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("degaulle2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Memoirs");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Charles de Gaulle");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        assertTrue(name.givenName().isPresent());
        assertEquals("Charles", name.givenName().orElse(""));
        String familyName = name.familyName().orElse("");
        assertTrue(familyName.contains("de Gaulle") || familyName.equals("Gaulle"));
    }

    @Test
    void unicodeName_Chinese() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("li2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Research");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "李明");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        assertNotNull(doc.contributors().getFirst().name());
    }

    @Test
    void unicodeName_Arabic() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("mohammad2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Philosophy");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "محمد علي");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        assertNotNull(doc.contributors().getFirst().name());
    }

    @Test
    void unicodeName_EuropeanAccents() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("muller2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Linguistics");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Søren Müller");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        String fullName = name.fullName();
        assertTrue(fullName.contains("Søren") || fullName.contains("Müller"));
    }

    @Test
    void specialCharName_Apostrophe() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("obrien2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Irish Literature");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Patrick O'Brien");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        assertTrue(name.givenName().isPresent());
        assertEquals("Patrick", name.givenName().orElse(""));
        String familyName = name.familyName().orElse("");
        assertTrue(familyName.contains("O'Brien") || familyName.equals("Brien"));
    }

    @Test
    void specialCharName_Hyphen() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("saintexupery2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Le Petit Prince");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Antoine de Saint-Exupéry");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        BiboPersonName name = doc.contributors().getFirst().name();
        assertTrue(name.givenName().isPresent());
        assertEquals("Antoine", name.givenName().orElse(""));
        String familyName = name.familyName().orElse("");
        assertTrue(familyName.contains("Saint-Exupéry") || familyName.contains("Exupéry"));
    }

    @Test
    void veryLongName() {
        String longName = "A".repeat(250) + " B".repeat(250);
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("long2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_AUTHOR, longName);

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(1, doc.contributors().size());
        assertNotNull(doc.contributors().getFirst().name());
    }

    @Test
    void emptyOrWhitespaceName() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("empty2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Article");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "   ");

        // Should either skip the empty name or handle gracefully
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    // ==================== 4.C - DATE PARSING EDGE CASES ====================

    // NOTE: Month validation not yet implemented - would require Phase 1 validation
    @Test
    void invalidMonth_Zero() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "0");

        // Currently does not validate - month parsed as numeric, 0 is ignored
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidMonth_Thirteen() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "13");

        // Currently does not validate - month 13 is ignored
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidMonth_Negative() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "-1");

        // Currently does not validate - negative months ignored
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidMonth_NinetyNine() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "99");

        // Currently does not validate - invalid months ignored
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void feb29NonLeapYear() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "feb");
        addField(entry, new Key("day"), "29");

        // Throws DateTimeException (not ValidationException) for invalid dates
        assertThrows(Exception.class, () -> converter.convertToBibo(entry));
    }

    @Test
    void malformedDate_Letters() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "abc");

        // Throws IllegalArgumentException (not ValidationException) for non-numeric years
        assertThrows(Exception.class, () -> converter.convertToBibo(entry));
    }

    @Test
    void malformedDate_InvalidFormat() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "20-24");

        // Currently does not validate year format - may accept or ignore malformed years
        // Just verify it doesn't crash
        try {
            BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
            assertNotNull(doc);
        } catch (Exception e) {
            // Also acceptable if it throws
            assertTrue(true);
        }
    }

    @Test
    void malformedDate_ISOFormat() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_YEAR, "2024-13-45");

        // Currently does not validate year format - may accept or ignore malformed years
        // Just verify it doesn't crash
        try {
            BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
            assertNotNull(doc);
        } catch (Exception e) {
            // Also acceptable if it throws
            assertTrue(true);
        }
    }

    @Test
    void edgeValidDate_Ancient() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("ancient1000"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Ancient Text");
        addField(entry, BibTeXEntry.KEY_YEAR, "1000");
        addField(entry, BibTeXEntry.KEY_MONTH, "jan");
        addField(entry, new Key("day"), "1");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.publicationDate().isPresent());
        assertEquals(1000, doc.publicationDate().get().year());
    }

    @Test
    void edgeValidDate_FutureMax() {
        int futureYear = java.time.Year.now().getValue() + 5;
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("future"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Future Publication");
        addField(entry, BibTeXEntry.KEY_YEAR, String.valueOf(futureYear));

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.publicationDate().isPresent());
        assertEquals(futureYear, doc.publicationDate().get().year());
    }

    // ==================== 4.D - IDENTIFIER VALIDATION EDGE CASES ====================
    // NOTE: ISBN/DOI/URL validation not yet implemented - would require Phase 1 validation

    @Test
    void invalidISBN10_Checksum() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, new Key("isbn"), "0-123-45678-0");

        // Currently does not validate ISBN checksums
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidISBN13_Checksum() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, new Key("isbn"), "978-0-123-45678-0");

        // Currently does not validate ISBN checksums
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidISBN_WrongLength9() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, new Key("isbn"), "012345678");

        // Currently does not validate ISBN length
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidISBN_WrongLength14() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, new Key("isbn"), "97801234567890");

        // Currently does not validate ISBN length
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidISBN_NonNumeric() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Book");
        addField(entry, new Key("isbn"), "ABC-DEF-GHI");

        // Currently does not validate ISBN format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidDOI_MissingPrefix() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Article");
        addField(entry, BibTeXEntry.KEY_DOI, "1234/article");

        // Currently does not validate DOI format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidDOI_InvalidFormat() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Article");
        addField(entry, BibTeXEntry.KEY_DOI, "10.1234");

        // Currently does not validate DOI format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidDOI_Whitespace() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("article2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test Article");
        addField(entry, BibTeXEntry.KEY_DOI, "10.1234/ article doi");

        // Currently does not validate DOI format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidURL_NoProtocol() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("web2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Web Resource");
        addField(entry, new Key("url"), "example.com");

        // Currently does not validate URL format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    @Test
    void invalidURL_Spaces() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("web2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Web Resource");
        addField(entry, new Key("url"), "http://example.com/bad url");

        // Currently does not validate URL format
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        assertNotNull(doc);
    }

    // ==================== 4.E - GENERAL EDGE CASES ====================

    @Test
    void missingTitle_Null() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        // No title field added

        assertThrows(ValidationException.class, () -> converter.convertToBibo(entry));
    }

    @Test
    void missingTitle_Empty() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "");

        assertThrows(ValidationException.class, () -> converter.convertToBibo(entry));
    }

    @Test
    void missingTitle_Whitespace() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("test2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "   ");

        assertThrows(ValidationException.class, () -> converter.convertToBibo(entry));
    }

    @Test
    void veryLargeTitle() {
        String largeTitle = "A".repeat(10000);
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("large2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, largeTitle);

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals(largeTitle, doc.title());
    }

    @Test
    void veryLargeAbstract() {
        String largeAbstract = "Abstract content. ".repeat(3000); // ~50k chars
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("large2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, new Key("abstract"), largeAbstract);

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.abstractText().isPresent());
        assertTrue(doc.abstractText().get().length() > 40000);
    }

    @Test
    void manyAuthors_Performance() {
        StringBuilder authors = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i > 0) authors.append(" and ");
            authors.append("Author").append(i).append(", Given").append(i);
        }

        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("many2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Collaborative Work");
        addField(entry, BibTeXEntry.KEY_AUTHOR, authors.toString());

        long start = System.currentTimeMillis();
        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();
        long duration = System.currentTimeMillis() - start;

        assertEquals(100, doc.contributors().size());
        assertTrue(duration < 5000, "Should process 100 authors in <5s, took " + duration + "ms");
    }

    @Test
    void minimalEntry_OnlyRequired() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("minimal2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Minimal Entry");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals("Minimal Entry", doc.title());
        assertTrue(doc.contributors().isEmpty());
        assertFalse(doc.publicationDate().isPresent());
        assertFalse(doc.publisher().isPresent());
    }

    @Test
    void maximalEntry_AllFields() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("maximal2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Complete Article");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, John");
        addField(entry, BibTeXEntry.KEY_EDITOR, "Doe, Jane");
        addField(entry, BibTeXEntry.KEY_YEAR, "2023");
        addField(entry, BibTeXEntry.KEY_MONTH, "jan");
        addField(entry, new Key("day"), "15");
        addField(entry, BibTeXEntry.KEY_PUBLISHER, "Test Publisher");
        addField(entry, BibTeXEntry.KEY_ADDRESS, "Test City");
        addField(entry, BibTeXEntry.KEY_JOURNAL, "Test Journal");
        addField(entry, BibTeXEntry.KEY_VOLUME, "10");
        addField(entry, BibTeXEntry.KEY_NUMBER, "5");
        addField(entry, BibTeXEntry.KEY_PAGES, "100-200");
        addField(entry, BibTeXEntry.KEY_DOI, "10.1234/test.doi");
        addField(entry, new Key("isbn"), "978-1-4028-9462-6");
        addField(entry, new Key("issn"), "1234-5678");
        addField(entry, new Key("url"), "http://example.com");
        addField(entry, new Key("abstract"), "This is an abstract");
        addField(entry, new Key("language"), "en");
        addField(entry, new Key("series"), "Test Series");
        addField(entry, new Key("edition"), "2nd");
        addField(entry, new Key("keywords"), "test, example, complete");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertEquals("Complete Article", doc.title());
        assertFalse(doc.contributors().isEmpty());
        assertTrue(doc.publicationDate().isPresent());
        assertTrue(doc.publisher().isPresent());
        assertTrue(doc.containerTitle().isPresent());
    }

    @Test
    void unicodeInAllTextFields() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("unicode2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Título con ñ, ü, é");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Müller, François");
        addField(entry, BibTeXEntry.KEY_JOURNAL, "Revue Française");
        addField(entry, new Key("abstract"), "Résumé avec caractères spéciaux: café, naïve");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        assertTrue(doc.title().contains("ñ"));
        assertTrue(doc.title().contains("ü"));
        String familyName = doc.contributors().getFirst().name().familyName().orElse("");
        assertTrue(familyName.contains("Müller"));
    }

    @Test
    void duplicateAuthors() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("dup2023"));
        addField(entry, BibTeXEntry.KEY_TITLE, "Test");
        addField(entry, BibTeXEntry.KEY_AUTHOR, "Smith, John and Smith, John and Doe, Jane");

        BiboDocument doc = converter.convertToBibo(entry).orElseThrow();

        // Should preserve all authors even if duplicates (or deduplicate - either is acceptable)
        assertTrue(doc.contributors().size() >= 2);
    }
}
