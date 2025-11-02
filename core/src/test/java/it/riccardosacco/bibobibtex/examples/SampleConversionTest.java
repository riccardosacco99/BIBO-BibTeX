package it.riccardosacco.bibobibtex.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SampleConversionTest {
    @TempDir
    Path tempDir;

    @Test
    void writeRdfSanitizesFileNameFromIdentifier() throws IOException {
        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Example")
                        .id("https://example.org/items/42")
                        .build();

        SampleConversion.writeRdf(document, tempDir);

        String expectedName = SampleConversion.safeFileName("https://example.org/items/42") + ".rdf";
        assertTrue(Files.exists(tempDir.resolve(expectedName)));
    }

    @Test
    void safeFileNameProvidesDeterministicFallback() {
        String sanitized = SampleConversion.safeFileName("https://example.org/item/42");
        assertEquals("https_example.org_item_42", sanitized);

        String fallback = SampleConversion.safeFileName("???");
        assertEquals("document", fallback);
    }
}
