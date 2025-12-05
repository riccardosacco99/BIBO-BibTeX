package it.riccardosacco.bibobibtex.vocbench;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuration for custom BibTeX ↔ BIBO field mappings.
 *
 * <p>Allows extending the default field mappings with custom fields.
 * Configuration can be loaded from a properties file or set programmatically.
 *
 * <p>Example properties file:
 * <pre>
 * # Custom BibTeX → BIBO mappings
 * bibtex.affiliation = http://example.org/bibo/affiliation
 * bibtex.funding = http://example.org/bibo/funding
 *
 * # Custom BIBO → BibTeX mappings
 * bibo.http://example.org/bibo/affiliation = affiliation
 * </pre>
 */
public class FieldMappingConfiguration {

    private static final String BIBTEX_PREFIX = "bibtex.";
    private static final String BIBO_PREFIX = "bibo.";

    private final Map<String, IRI> bibtexToBibo;
    private final Map<IRI, String> biboToBibtex;

    private FieldMappingConfiguration(
            Map<String, IRI> bibtexToBibo,
            Map<IRI, String> biboToBibtex) {
        this.bibtexToBibo = Collections.unmodifiableMap(new HashMap<>(bibtexToBibo));
        this.biboToBibtex = Collections.unmodifiableMap(new HashMap<>(biboToBibtex));
    }

    /**
     * Creates an empty configuration with no custom mappings.
     */
    public static FieldMappingConfiguration empty() {
        return new FieldMappingConfiguration(Map.of(), Map.of());
    }

    /**
     * Loads configuration from a properties file.
     *
     * @param path path to the properties file
     * @return the loaded configuration
     * @throws IOException if the file cannot be read
     */
    public static FieldMappingConfiguration fromFile(Path path) throws IOException {
        Objects.requireNonNull(path, "path");

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path)) {
            props.load(is);
        }

        return fromProperties(props);
    }

    /**
     * Loads configuration from Properties.
     *
     * @param props the properties to load
     * @return the loaded configuration
     */
    public static FieldMappingConfiguration fromProperties(Properties props) {
        Objects.requireNonNull(props, "props");

        Map<String, IRI> bibtexToBibo = new HashMap<>();
        Map<IRI, String> biboToBibtex = new HashMap<>();

        var valueFactory = SimpleValueFactory.getInstance();

        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);

            if (key.startsWith(BIBTEX_PREFIX)) {
                String bibtexField = key.substring(BIBTEX_PREFIX.length());
                if (!bibtexField.isBlank() && !value.isBlank()) {
                    IRI iri = valueFactory.createIRI(value);
                    bibtexToBibo.put(bibtexField.toLowerCase(), iri);
                }
            } else if (key.startsWith(BIBO_PREFIX)) {
                String iriString = key.substring(BIBO_PREFIX.length());
                if (!iriString.isBlank() && !value.isBlank()) {
                    IRI iri = valueFactory.createIRI(iriString);
                    biboToBibtex.put(iri, value.toLowerCase());
                }
            }
        }

        return new FieldMappingConfiguration(bibtexToBibo, biboToBibtex);
    }

    /**
     * Creates a new builder for constructing configurations.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the BIBO IRI for a BibTeX field.
     *
     * @param bibtexField the BibTeX field name
     * @return the mapped BIBO IRI, or empty if not configured
     */
    public Optional<IRI> getBiboProperty(String bibtexField) {
        if (bibtexField == null || bibtexField.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(bibtexToBibo.get(bibtexField.toLowerCase()));
    }

    /**
     * Gets the BibTeX field for a BIBO IRI.
     *
     * @param biboProperty the BIBO property IRI
     * @return the mapped BibTeX field, or empty if not configured
     */
    public Optional<String> getBibtexField(IRI biboProperty) {
        if (biboProperty == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(biboToBibtex.get(biboProperty));
    }

    /**
     * Gets all custom BibTeX → BIBO mappings.
     */
    public Map<String, IRI> getBibtexToBiboMappings() {
        return bibtexToBibo;
    }

    /**
     * Gets all custom BIBO → BibTeX mappings.
     */
    public Map<IRI, String> getBiboToBibtexMappings() {
        return biboToBibtex;
    }

    /**
     * Checks if a BibTeX field has a custom mapping.
     */
    public boolean hasCustomBibtexMapping(String bibtexField) {
        return bibtexField != null && bibtexToBibo.containsKey(bibtexField.toLowerCase());
    }

    /**
     * Checks if a BIBO property has a custom mapping.
     */
    public boolean hasCustomBiboMapping(IRI biboProperty) {
        return biboProperty != null && biboToBibtex.containsKey(biboProperty);
    }

    /**
     * Creates a new configuration by merging this one with another.
     * The other configuration's mappings take precedence.
     *
     * @param other the configuration to merge with
     * @return a new merged configuration
     */
    public FieldMappingConfiguration merge(FieldMappingConfiguration other) {
        Objects.requireNonNull(other, "other");

        Map<String, IRI> mergedBibtexToBibo = new HashMap<>(bibtexToBibo);
        mergedBibtexToBibo.putAll(other.bibtexToBibo);

        Map<IRI, String> mergedBiboToBibtex = new HashMap<>(biboToBibtex);
        mergedBiboToBibtex.putAll(other.biboToBibtex);

        return new FieldMappingConfiguration(mergedBibtexToBibo, mergedBiboToBibtex);
    }

    /**
     * Builder for creating FieldMappingConfiguration.
     */
    public static class Builder {
        private final Map<String, IRI> bibtexToBibo = new HashMap<>();
        private final Map<IRI, String> biboToBibtex = new HashMap<>();

        /**
         * Adds a bidirectional mapping.
         *
         * @param bibtexField   the BibTeX field name
         * @param biboProperty  the BIBO property IRI
         * @return this builder
         */
        public Builder addMapping(String bibtexField, IRI biboProperty) {
            Objects.requireNonNull(bibtexField, "bibtexField");
            Objects.requireNonNull(biboProperty, "biboProperty");

            bibtexToBibo.put(bibtexField.toLowerCase(), biboProperty);
            biboToBibtex.put(biboProperty, bibtexField.toLowerCase());
            return this;
        }

        /**
         * Adds a bidirectional mapping using IRI string.
         *
         * @param bibtexField  the BibTeX field name
         * @param biboIri      the BIBO property IRI as string
         * @return this builder
         */
        public Builder addMapping(String bibtexField, String biboIri) {
            Objects.requireNonNull(biboIri, "biboIri");
            IRI iri = SimpleValueFactory.getInstance().createIRI(biboIri);
            return addMapping(bibtexField, iri);
        }

        /**
         * Adds a BibTeX → BIBO only mapping.
         */
        public Builder addBibtexToBibo(String bibtexField, IRI biboProperty) {
            Objects.requireNonNull(bibtexField, "bibtexField");
            Objects.requireNonNull(biboProperty, "biboProperty");
            bibtexToBibo.put(bibtexField.toLowerCase(), biboProperty);
            return this;
        }

        /**
         * Adds a BIBO → BibTeX only mapping.
         */
        public Builder addBiboToBibtex(IRI biboProperty, String bibtexField) {
            Objects.requireNonNull(biboProperty, "biboProperty");
            Objects.requireNonNull(bibtexField, "bibtexField");
            biboToBibtex.put(biboProperty, bibtexField.toLowerCase());
            return this;
        }

        /**
         * Builds the configuration.
         */
        public FieldMappingConfiguration build() {
            return new FieldMappingConfiguration(bibtexToBibo, biboToBibtex);
        }
    }
}
