package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.converter.BibTeXBibliographicConverter.KeyGenerationStrategy;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributor;
import it.riccardosacco.bibobibtex.model.bibo.BiboContributorRole;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.util.List;
import org.jbibtex.BibTeXEntry;
import org.junit.jupiter.api.Test;

class CitationKeyGenerationTest {

    @Test
    void duplicateGeneratedKeysReceiveSuffix() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
        BiboDocument first = createDocument("First Study", 2024, "Alice", "Smith");
        BiboDocument second = createDocument("Second Study", 2024, "Alice", "Smith");

        List<BibTeXEntry> entries = converter.convertFromBiboBatch(List.of(first, second));
        assertEquals("smith_2024", entries.getFirst().getKey().getValue());
        assertEquals("smith_2024_2", entries.get(1).getKey().getValue());
    }

    @Test
    void manualKeysDeduplicatedWithSuffix() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
        BiboDocument manual = createDocument("Manual Key", 2022, "Luca", "Verdi", "ManualKey");
        BiboDocument duplicate = createDocument("Manual Key 2", 2022, "Luca", "Verdi", "ManualKey");

        String firstKey = converter.convertFromBibo(manual).orElseThrow().getKey().getValue();
        String secondKey = converter.convertFromBibo(duplicate).orElseThrow().getKey().getValue();

        assertEquals("ManualKey", firstKey);
        assertEquals("ManualKey_2", secondKey);
    }

    @Test
    void invalidManualKeyFallsBackToGenerated() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
        BiboDocument document = createDocument("Invalid Key Doc", 2023, "Nina", "Rossi", "Bad Key With Spaces");

        String key = converter.convertFromBibo(document).orElseThrow().getKey().getValue();
        assertEquals("rossi_2023", key);
    }

    @Test
    void authorTitleStrategyUsesTitleToken() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter(KeyGenerationStrategy.AUTHOR_TITLE);
        BiboDocument document = createDocument("Quantum Mechanics Overview", 2021, "Ana", "Gomez");

        String key = converter.convertFromBibo(document).orElseThrow().getKey().getValue();
        assertEquals("gomez_quantum", key);
    }

    @Test
    void hashStrategyProducesEightCharacterKey() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter(KeyGenerationStrategy.HASH);
        BiboDocument document = createDocument("Hash Driven Study", 2020, "Leo", "Chen");

        String key = converter.convertFromBibo(document).orElseThrow().getKey().getValue();
        assertEquals(8, key.length());
        assertTrue(key.matches("[0-9a-f]{8}"));
    }

    @Test
    void generatedKeyRespectsMaxLength() {
        BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();
        String longFamily = "LongSurname".repeat(10);
        BiboDocument document = createDocument("Length Stress", 2024, "Avery", longFamily);

        String key = converter.convertFromBibo(document).orElseThrow().getKey().getValue();
        assertEquals(64, key.length());
    }

    private BiboDocument createDocument(String title, int year, String given, String family) {
        return createDocument(title, year, given, family, null);
    }

    private BiboDocument createDocument(String title, int year, String given, String family, String id) {
        BiboDocument.Builder builder =
                BiboDocument.builder(BiboDocumentType.ARTICLE, title).publicationDate(BiboPublicationDate.ofYear(year));
        builder.addContributor(author(given, family));
        if (id != null) {
            builder.id(id);
        }
        return builder.build();
    }

    private BiboContributor author(String given, String family) {
        BiboPersonName name = BiboPersonName.builder(given + " " + family)
                .givenName(given)
                .familyName(family)
                .build();
        return new BiboContributor(name, BiboContributorRole.AUTHOR);
    }
}
