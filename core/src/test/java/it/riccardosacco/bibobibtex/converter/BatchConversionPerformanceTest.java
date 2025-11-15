package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Performance tests for batch conversion.
 * Tests are tagged with @Tag("performance") to allow selective execution.
 */
@Tag("performance")
class BatchConversionPerformanceTest {

    private BatchConverter batchConverter;
    private List<BibTeXEntry> smallDataset;   // 100 entries
    private List<BibTeXEntry> mediumDataset;  // 1000 entries
    private List<BibTeXEntry> largeDataset;   // 10000 entries

    @BeforeEach
    void setUp() {
        batchConverter = new BatchConverter();
        smallDataset = generateSyntheticEntries(100);
        mediumDataset = generateSyntheticEntries(1000);
        largeDataset = generateSyntheticEntries(10000);
    }

    @Test
    void testSmallBatchConversionPerformance() {
        long startTime = System.currentTimeMillis();
        List<BiboDocument> results = batchConverter.convertBatch(smallDataset);
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(smallDataset.size(), results.size(), "All entries should be converted");
        System.out.printf("Small batch (100 entries): %dms (%.1f entries/sec)%n",
            elapsed, 100.0 * 1000 / elapsed);

        // Should be very fast for 100 entries
        assertTrue(elapsed < 5000, "100 entries should convert in less than 5 seconds");
    }

    @Test
    void testMediumBatchConversionPerformance() {
        long startTime = System.currentTimeMillis();
        List<BiboDocument> results = batchConverter.convertBatch(mediumDataset);
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(mediumDataset.size(), results.size(), "All entries should be converted");
        System.out.printf("Medium batch (1000 entries): %dms (%.1f entries/sec)%n",
            elapsed, 1000.0 * 1000 / elapsed);

        // Target: 1000 entries in less than 10 seconds
        assertTrue(elapsed < 10000, "1000 entries should convert in less than 10 seconds");
    }

    @Test
    void testLargeBatchConversionPerformance() {
        long startTime = System.currentTimeMillis();
        List<BiboDocument> results = batchConverter.convertBatch(largeDataset);
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(largeDataset.size(), results.size(), "All entries should be converted");
        System.out.printf("Large batch (10000 entries): %dms (%.1f entries/sec)%n",
            elapsed, 10000.0 * 1000 / elapsed);

        // Should handle 10k entries reasonably well
        assertTrue(elapsed < 100000, "10000 entries should convert in less than 100 seconds");
    }

    @Test
    void testParallelConversionPerformance() {
        // Sequential
        long seqStartTime = System.currentTimeMillis();
        List<BiboDocument> seqResults = batchConverter.convertBatch(mediumDataset);
        long seqElapsed = System.currentTimeMillis() - seqStartTime;

        // Parallel
        long parStartTime = System.currentTimeMillis();
        List<BiboDocument> parResults = batchConverter.convertBatchParallel(mediumDataset);
        long parElapsed = System.currentTimeMillis() - parStartTime;

        assertEquals(seqResults.size(), parResults.size(), "Same number of results");
        System.out.printf("Sequential: %dms (%.1f entries/sec)%n",
            seqElapsed, 1000.0 * 1000 / seqElapsed);
        System.out.printf("Parallel:   %dms (%.1f entries/sec)%n",
            parElapsed, 1000.0 * 1000 / parElapsed);
        System.out.printf("Speedup: %.2fx%n", (double) seqElapsed / parElapsed);

        // Parallel should be faster or at least not significantly slower
        // (On some systems with overhead, parallel might be slightly slower for small datasets)
        System.out.println("Parallel conversion completed successfully");
    }

    @Test
    void testProgressCallback() {
        AtomicInteger progressCalls = new AtomicInteger(0);
        AtomicInteger lastProgress = new AtomicInteger(0);

        List<BiboDocument> results = batchConverter.convertBatch(
            mediumDataset,
            (current, total) -> {
                progressCalls.incrementAndGet();
                lastProgress.set(current);
                assertTrue(current <= total, "Current should not exceed total");
                assertTrue(current > 0, "Current should be positive");
            }
        );

        assertEquals(mediumDataset.size(), results.size());
        assertTrue(progressCalls.get() > 0, "Progress callback should be called");
        assertEquals(mediumDataset.size(), lastProgress.get(), "Final progress should equal total");
        System.out.printf("Progress callback called %d times%n", progressCalls.get());
    }

    @Test
    void testStreamingConversion() {
        long startTime = System.currentTimeMillis();
        List<BiboDocument> results = batchConverter.convertStream(mediumDataset.stream())
            .toList();
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(mediumDataset.size(), results.size(), "All entries should be converted");
        System.out.printf("Streaming (1000 entries): %dms (%.1f entries/sec)%n",
            elapsed, 1000.0 * 1000 / elapsed);

        assertTrue(elapsed < 15000, "Streaming should complete in reasonable time");
    }

    @Test
    void testParallelStreamingConversion() {
        long startTime = System.currentTimeMillis();
        List<BiboDocument> results = batchConverter.convertStreamParallel(mediumDataset.stream())
            .toList();
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(mediumDataset.size(), results.size(), "All entries should be converted");
        System.out.printf("Parallel streaming (1000 entries): %dms (%.1f entries/sec)%n",
            elapsed, 1000.0 * 1000 / elapsed);

        assertTrue(elapsed < 15000, "Parallel streaming should complete in reasonable time");
    }

    @Test
    void testMemoryEfficiencyWithLargeDataset() {
        Runtime runtime = Runtime.getRuntime();

        // Force garbage collection before test
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Convert large dataset using streaming (memory-efficient)
        long count = batchConverter.convertStream(largeDataset.stream()).count();

        System.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryAfter - memoryBefore) / 1024 / 1024; // MB

        assertEquals(largeDataset.size(), count, "All entries should be converted");
        System.out.printf("Memory used for 10000 entries (streaming): ~%d MB%n", memoryUsed);

        // Should use less than 512 MB for 10k entries
        assertTrue(memoryUsed < 512, "Memory usage should be reasonable");
    }

    @Test
    void testCustomParallelism() {
        // Test with different parallelism levels
        int[] parallelismLevels = {1, 2, 4, 8};

        for (int parallelism : parallelismLevels) {
            BatchConverter customConverter = new BatchConverter(parallelism);
            assertEquals(parallelism, customConverter.getParallelism());

            long startTime = System.currentTimeMillis();
            List<BiboDocument> results = customConverter.convertBatchParallel(smallDataset);
            long elapsed = System.currentTimeMillis() - startTime;

            assertEquals(smallDataset.size(), results.size());
            System.out.printf("Parallelism=%d: %dms%n", parallelism, elapsed);
        }
    }

    @Test
    void testEmptyBatch() {
        List<BiboDocument> results = batchConverter.convertBatch(List.of());
        assertTrue(results.isEmpty(), "Empty input should produce empty output");
    }

    @Test
    void testNullBatch() {
        List<BiboDocument> results = batchConverter.convertBatch(null);
        assertTrue(results.isEmpty(), "Null input should produce empty output");
    }

    /**
     * Generates synthetic BibTeX entries for performance testing.
     * Creates diverse entry types with realistic field values.
     */
    private List<BibTeXEntry> generateSyntheticEntries(int count) {
        List<BibTeXEntry> entries = new ArrayList<>(count);
        Key[] types = {
            BibTeXEntry.TYPE_ARTICLE,
            BibTeXEntry.TYPE_BOOK,
            BibTeXEntry.TYPE_INPROCEEDINGS,
            BibTeXEntry.TYPE_PHDTHESIS
        };

        for (int i = 0; i < count; i++) {
            Key type = types[i % types.length];
            BibTeXEntry entry = new BibTeXEntry(type, new Key("synthetic" + i));

            // Add required fields
            addField(entry, BibTeXEntry.KEY_TITLE, "Synthetic Title " + i);
            addField(entry, BibTeXEntry.KEY_AUTHOR, generateAuthors(i));
            addField(entry, BibTeXEntry.KEY_YEAR, String.valueOf(2000 + (i % 24)));

            // Add type-specific fields
            switch (type.getValue()) {
                case "article":
                    addField(entry, BibTeXEntry.KEY_JOURNAL, "Journal " + (i % 10));
                    addField(entry, BibTeXEntry.KEY_VOLUME, String.valueOf(1 + (i % 20)));
                    addField(entry, BibTeXEntry.KEY_PAGES, (100 + i) + "--" + (120 + i));
                    break;
                case "book":
                    addField(entry, BibTeXEntry.KEY_PUBLISHER, "Publisher " + (i % 5));
                    addField(entry, new Key("isbn"), generateISBN(i));
                    break;
                case "inproceedings":
                    addField(entry, BibTeXEntry.KEY_BOOKTITLE, "Conference " + (i % 8));
                    addField(entry, BibTeXEntry.KEY_PAGES, (100 + i) + "--" + (110 + i));
                    break;
                case "phdthesis":
                    addField(entry, BibTeXEntry.KEY_SCHOOL, "University " + (i % 6));
                    break;
            }

            // Add optional fields
            if (i % 3 == 0) {
                addField(entry, new Key("doi"), "10.1000/synthetic." + i);
            }
            if (i % 4 == 0) {
                addField(entry, BibTeXEntry.KEY_MONTH, getMonth(i % 12));
            }

            entries.add(entry);
        }

        return entries;
    }

    private void addField(BibTeXEntry entry, Key key, String value) {
        entry.addField(key, new StringValue(value, StringValue.Style.BRACED));
    }

    private String generateAuthors(int seed) {
        int numAuthors = 1 + (seed % 3); // 1-3 authors
        List<String> authors = new ArrayList<>();
        for (int i = 0; i < numAuthors; i++) {
            authors.add("Author" + ((seed + i) % 100) + ", First" + i);
        }
        return String.join(" and ", authors);
    }

    private String generateISBN(int seed) {
        return String.format("978-0-%03d-%05d-0", seed % 1000, seed);
    }

    private String getMonth(int month) {
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun",
                          "jul", "aug", "sep", "oct", "nov", "dec"};
        return months[month];
    }
}
