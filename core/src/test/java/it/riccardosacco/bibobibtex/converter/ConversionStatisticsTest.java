package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.List;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

class ConversionStatisticsTest {

    private final BatchConverter batchConverter = new BatchConverter();

    @Test
    void collectsCountsAndWarnings() {
        BibTeXEntry ok = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("ok"));
        ok.addField(BibTeXEntry.KEY_TITLE, braced("Title"));
        ok.addField(BibTeXEntry.KEY_AUTHOR, braced("Author, A."));
        ok.addField(BibTeXEntry.KEY_JOURNAL, braced("Journal"));
        ok.addField(BibTeXEntry.KEY_YEAR, braced("2024"));

        BibTeXEntry bad = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("bad"));
        // Missing title triggers ValidationException

        BatchConversionResult result = batchConverter.convertBatchWithStats(List.of(ok, bad));

        ConversionStatistics stats = result.getStatistics();
        assertEquals(2, stats.getTotalEntries());
        assertEquals(1, stats.getSuccessfulConversions());
        assertEquals(1, stats.getFailedConversions());
        assertFalse(stats.getWarningMessages().isEmpty(), "Should record failure warning");
        assertTrue(stats.getFieldStatistics().getOrDefault("contributors", 0) >= 1);
    }

    @Test
    void textReportAndJsonAreNotEmpty() {
        BibTeXEntry ok = new BibTeXEntry(BibTeXEntry.TYPE_BOOK, new Key("book"));
        ok.addField(BibTeXEntry.KEY_TITLE, braced("Book Title"));
        ok.addField(BibTeXEntry.KEY_AUTHOR, braced("Author, A."));
        ok.addField(BibTeXEntry.KEY_PUBLISHER, braced("Publisher"));
        ok.addField(BibTeXEntry.KEY_YEAR, braced("2023"));

        BatchConversionResult result = batchConverter.convertBatchWithStats(List.of(ok));
        ConversionStatistics stats = result.getStatistics();
        String text = stats.toTextReport();
        String json = stats.toJson();

        assertTrue(text.contains("Total entries"));
        assertTrue(json.contains("\"total\""));
    }

    @Test
    void statisticsMatchDocumentsSize() {
        List<BibTeXEntry> entries = List.of(createEntry("a"), createEntry("b"), createEntry("c"));
        BatchConversionResult result = batchConverter.convertBatchWithStats(entries);
        ConversionStatistics stats = result.getStatistics();
        List<BiboDocument> docs = result.getDocuments();

        assertEquals(docs.size(), stats.getSuccessfulConversions());
        assertEquals(entries.size(), stats.getTotalEntries());
    }

    private BibTeXEntry createEntry(String key) {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key(key));
        entry.addField(BibTeXEntry.KEY_TITLE, braced("Title " + key));
        entry.addField(BibTeXEntry.KEY_AUTHOR, braced("Author, " + key));
        entry.addField(BibTeXEntry.KEY_JOURNAL, braced("Journal"));
        entry.addField(BibTeXEntry.KEY_YEAR, braced("2022"));
        return entry;
    }

    private StringValue braced(String value) {
        return new StringValue(value, StringValue.Style.BRACED);
    }
}
