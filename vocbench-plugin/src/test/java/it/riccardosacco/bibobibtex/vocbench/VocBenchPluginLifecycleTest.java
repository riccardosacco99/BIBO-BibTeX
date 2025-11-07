package it.riccardosacco.bibobibtex.vocbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.riccardosacco.bibobibtex.model.bibo.BiboDocument;
import it.riccardosacco.bibobibtex.model.bibo.BiboDocumentType;
import it.riccardosacco.bibobibtex.model.bibo.BiboPublicationDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.rdf4j.model.Model;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.jbibtex.Value;
import org.junit.jupiter.api.Test;

class VocBenchPluginLifecycleTest {
    private final InMemoryGateway gateway = new InMemoryGateway();
    private final VocBenchPluginLifecycle lifecycle =
            new VocBenchPluginLifecycle(new VocBenchPluginBootstrap(), gateway);

    @Test
    void importEntryStoresModelInGateway() {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("sample2024"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Sample Title", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue("Doe, Jane", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_YEAR, new StringValue("2024", StringValue.Style.BRACED));

        Optional<BiboDocument> document = lifecycle.importEntry(entry);

        assertTrue(document.isPresent());
        assertEquals(1, gateway.storedModels.size());
    }

    @Test
    void exportDocumentReliesOnGatewayLookup() {
        BiboDocument document =
                BiboDocument.builder(BiboDocumentType.ARTICLE, "Article Example")
                        .id("article-1")
                        .publicationDate(BiboPublicationDate.ofYear(2024))
                        .build();
        gateway.register(document);

        Optional<BibTeXEntry> entry = lifecycle.exportDocument("article-1");

        assertTrue(entry.isPresent());
        assertEquals("Article Example", value(entry.get(), BibTeXEntry.KEY_TITLE));
    }

    static final class InMemoryGateway implements VocBenchRepositoryGateway {
        private final Map<String, BiboDocument> documents = new HashMap<>();
        private final Map<String, Model> models = new HashMap<>();
        private final List<Model> storedModels = new ArrayList<>();

        @Override
        public void store(Model model) {
            storedModels.add(model);
        }

        @Override
        public Optional<BiboDocument> fetchByIdentifier(String identifier) {
            return Optional.ofNullable(documents.get(identifier));
        }

        @Override
        public List<BiboDocument> listAll() {
            return List.copyOf(documents.values());
        }

        void register(BiboDocument document) {
            document.id().ifPresent(id -> {
                documents.put(id, document);
                models.put(id, document.rdfModel());
            });
        }
    }

    private static String value(BibTeXEntry entry, Key key) {
        Value field = entry.getField(key);
        if (field == null) {
            throw new IllegalStateException("Missing field " + key);
        }
        return field.toUserString();
    }
}
