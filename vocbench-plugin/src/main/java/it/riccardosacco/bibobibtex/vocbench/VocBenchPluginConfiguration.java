package it.riccardosacco.bibobibtex.vocbench;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads and validates plugin configuration, supporting environment overrides for deployment.
 */
public final class VocBenchPluginConfiguration {
    private static final String PROPERTIES_RESOURCE = "/plugin.properties";
    private static final String ENV_REPOSITORY_TYPE = "VOCBENCH_REPO_TYPE";
    private static final String ENV_DATA_DIR = "VOCBENCH_DATA_DIR";

    private final Properties properties;
    private final Map<String, String> environment;

    public VocBenchPluginConfiguration() {
        this(loadDefaultProperties(), System.getenv());
    }

    VocBenchPluginConfiguration(Properties properties, Map<String, String> environment) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.environment = environment == null ? Map.of() : Map.copyOf(environment);
        validate();
    }

    private static Properties loadDefaultProperties() {
        Properties props = new Properties();
        try (InputStream stream = VocBenchPluginConfiguration.class.getResourceAsStream(PROPERTIES_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing configuration resource: " + PROPERTIES_RESOURCE);
            }
            props.load(stream);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration resource " + PROPERTIES_RESOURCE, e);
        }
    }

    public RepositoryType getRepositoryType() {
        String value = envOrProperty(ENV_REPOSITORY_TYPE, "repository.type");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("repository.type is required");
        }
        try {
            return RepositoryType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid repository.type value: " + value, ex);
        }
    }

    public String getRepositoryDataDir() {
        String value = envOrProperty(ENV_DATA_DIR, "repository.dataDir");
        if (getRepositoryType() == RepositoryType.NATIVE) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("repository.dataDir is required for NATIVE repositories");
            }
        }
        return value;
    }

    public String getNamespacePrefix() {
        String prefix = properties.getProperty("namespace.prefix");
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalStateException("namespace.prefix is required");
        }
        try {
            URI uri = URI.create(prefix);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("namespace.prefix must be an absolute http(s) URL");
            }
            return ensureTrailingSeparator(prefix);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid namespace.prefix URL: " + prefix, ex);
        }
    }

    public boolean isStrictFieldMapping() {
        return Boolean.parseBoolean(properties.getProperty("field.mapping.strict", "false"));
    }

    public IdentifierStrategy getIdentifierStrategy() {
        String value = properties.getProperty("identifier.strategy", "author-year-title");
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return IdentifierStrategy.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid identifier.strategy value: " + value, ex);
        }
    }

    private String envOrProperty(String envKey, String propertyKey) {
        String envValue = environment.get(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return properties.getProperty(propertyKey);
    }

    private void validate() {
        getRepositoryType();
        getNamespacePrefix();
        if (getRepositoryType() == RepositoryType.NATIVE) {
            getRepositoryDataDir();
        }
    }

    private static String ensureTrailingSeparator(String prefix) {
        if (prefix.endsWith("/") || prefix.endsWith("#")) {
            return prefix;
        }
        return prefix + "/";
    }

    public enum RepositoryType {
        NATIVE,
        MEMORY
    }

    public enum IdentifierStrategy {
        AUTHOR_YEAR_TITLE,
        UUID
    }
}
