package it.riccardosacco.bibobibtex.vocbench;

import static org.junit.jupiter.api.Assertions.*;

import it.riccardosacco.bibobibtex.vocbench.VocBenchPluginConfiguration.IdentifierStrategy;
import it.riccardosacco.bibobibtex.vocbench.VocBenchPluginConfiguration.RepositoryType;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VocBenchPluginConfigurationTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsDefaultsFromResource() {
        VocBenchPluginConfiguration configuration = new VocBenchPluginConfiguration();
        assertEquals(RepositoryType.NATIVE, configuration.getRepositoryType());
        assertEquals("http://example.org/bibo/", configuration.getNamespacePrefix());
        assertFalse(configuration.isStrictFieldMapping());
        assertEquals(IdentifierStrategy.AUTHOR_YEAR_TITLE, configuration.getIdentifierStrategy());
    }

    @Test
    void environmentOverridesTakePrecedence() {
        Properties properties = defaultProperties();
        Map<String, String> env = Map.of(
                "VOCBENCH_REPO_TYPE", "memory",
                "VOCBENCH_DATA_DIR", tempDir.resolve("env").toString());

        VocBenchPluginConfiguration configuration = new VocBenchPluginConfiguration(properties, env);

        assertEquals(RepositoryType.MEMORY, configuration.getRepositoryType());
        assertEquals(env.get("VOCBENCH_DATA_DIR"), configuration.getRepositoryDataDir());
    }

    @Test
    void invalidRepositoryTypeThrows() {
        Properties properties = defaultProperties();
        properties.setProperty("repository.type", "invalid");
        assertThrows(IllegalArgumentException.class, () -> new VocBenchPluginConfiguration(properties, Map.of()));
    }

    @Test
    void missingNamespacePrefixFailsFast() {
        Properties properties = defaultProperties();
        properties.remove("namespace.prefix");
        assertThrows(IllegalStateException.class, () -> new VocBenchPluginConfiguration(properties, Map.of()));
    }

    @Test
    void invalidNamespaceUrlIsRejected() {
        Properties properties = defaultProperties();
        properties.setProperty("namespace.prefix", "not-a-url");
        assertThrows(IllegalArgumentException.class, () -> new VocBenchPluginConfiguration(properties, Map.of()));
    }

    private Properties defaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("repository.type", "native");
        properties.setProperty("repository.dataDir", tempDir.resolve("repo").toString());
        properties.setProperty("namespace.prefix", "http://example.org/test/");
        properties.setProperty("field.mapping.strict", "false");
        properties.setProperty("identifier.strategy", "author-year-title");
        return properties;
    }
}
