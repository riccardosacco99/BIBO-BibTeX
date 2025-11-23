package it.riccardosacco.bibobibtex.converter;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for batch conversion performance.
 * <p>
 * Run with:
 * <pre>
 *   mvn -DskipTests -Dtest=none -DfailIfNoTests=false -Pjmh test
 *   java -jar target/benchmarks.jar
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BatchConversionBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        BatchConverter converter;
        List<BibTeXEntry> entries100;
        List<BibTeXEntry> entries1000;

        @Setup(Level.Trial)
        public void setUp() {
            converter = new BatchConverter();
            entries100 = generateSyntheticEntries(100);
            entries1000 = generateSyntheticEntries(1000);
        }
    }

    @Benchmark
    public List<BiboDocument> benchmark100Sequential(BenchmarkState state) {
        return state.converter.convertBatch(state.entries100);
    }

    @Benchmark
    public List<BiboDocument> benchmark1000Sequential(BenchmarkState state) {
        return state.converter.convertBatch(state.entries1000);
    }

    @Benchmark
    public List<BiboDocument> benchmark100Parallel(BenchmarkState state) {
        return state.converter.convertBatchParallel(state.entries100);
    }

    @Benchmark
    public List<BiboDocument> benchmark1000Parallel(BenchmarkState state) {
        return state.converter.convertBatchParallel(state.entries1000);
    }

    private static List<BibTeXEntry> generateSyntheticEntries(int count) {
        List<BibTeXEntry> entries = new ArrayList<>(count);
        Key[] types = {
                BibTeXEntry.TYPE_ARTICLE,
                BibTeXEntry.TYPE_BOOK,
                BibTeXEntry.TYPE_INPROCEEDINGS,
                BibTeXEntry.TYPE_PHDTHESIS
        };

        for (int i = 0; i < count; i++) {
            Key type = types[i % types.length];
            BibTeXEntry entry = new BibTeXEntry(type, new Key("bench" + i));
            entry.addField(BibTeXEntry.KEY_TITLE, braced("Benchmark Title " + i));
            entry.addField(BibTeXEntry.KEY_AUTHOR, braced("Author" + i + ", Test"));
            entry.addField(BibTeXEntry.KEY_YEAR, braced(Integer.toString(2000 + (i % 24))));

            switch (type.getValue()) {
                case "article" -> {
                    entry.addField(BibTeXEntry.KEY_JOURNAL, braced("Journal " + (i % 10)));
                    entry.addField(BibTeXEntry.KEY_VOLUME, braced(Integer.toString(1 + (i % 5))));
                }
                case "book" -> entry.addField(BibTeXEntry.KEY_PUBLISHER, braced("Publisher " + (i % 5)));
                case "inproceedings" -> entry.addField(BibTeXEntry.KEY_BOOKTITLE, braced("Conference " + (i % 8)));
                case "phdthesis" -> entry.addField(BibTeXEntry.KEY_SCHOOL, braced("University " + (i % 6)));
                default -> {
                }
            }
            entries.add(entry);
        }

        return entries;
    }

    private static StringValue braced(String value) {
        return new StringValue(value, StringValue.Style.BRACED);
    }
}
