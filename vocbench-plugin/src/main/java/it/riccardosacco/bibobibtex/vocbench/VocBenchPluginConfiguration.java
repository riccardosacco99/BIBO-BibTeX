package it.riccardosacco.bibobibtex.vocbench;

import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * Configuration settings for the VocBench BIBO-BibTeX converter plugin.
 * Sprint 01 - US-07: VocBench Plugin Configuration
 */
public class VocBenchPluginConfiguration {

    private RDFFormat outputFormat;
    private boolean validateIdentifiers;
    private boolean preserveOriginalCitationKeys;
    private boolean strictValidation;
    private String repositoryType;
    private String repositoryPath;

    /**
     * Creates a default configuration with reasonable defaults.
     */
    public VocBenchPluginConfiguration() {
        // Default values
        this.outputFormat = RDFFormat.TURTLE;
        this.validateIdentifiers = true;
        this.preserveOriginalCitationKeys = true;
        this.strictValidation = false;
        this.repositoryType = "memory"; // "memory" or "native"
        this.repositoryPath = null;
    }

    /**
     * Gets the RDF output format for serialization.
     * @return the RDF format (default: TURTLE)
     */
    public RDFFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the RDF output format for serialization.
     * @param outputFormat the RDF format (TURTLE, RDFXML, JSONLD, etc.)
     */
    public void setOutputFormat(RDFFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Checks if identifier validation is enabled.
     * @return true if identifiers should be validated (default: true)
     */
    public boolean isValidateIdentifiers() {
        return validateIdentifiers;
    }

    /**
     * Enables or disables identifier validation.
     * When enabled, ISBNs, ISSNs, DOIs are validated with checksums.
     * @param validateIdentifiers true to enable validation
     */
    public void setValidateIdentifiers(boolean validateIdentifiers) {
        this.validateIdentifiers = validateIdentifiers;
    }

    /**
     * Checks if original BibTeX citation keys should be preserved.
     * @return true if citation keys are preserved (default: true)
     */
    public boolean isPreserveOriginalCitationKeys() {
        return preserveOriginalCitationKeys;
    }

    /**
     * Enables or disables preservation of original citation keys.
     * When disabled, citation keys are auto-generated.
     * @param preserveOriginalCitationKeys true to preserve keys
     */
    public void setPreserveOriginalCitationKeys(boolean preserveOriginalCitationKeys) {
        this.preserveOriginalCitationKeys = preserveOriginalCitationKeys;
    }

    /**
     * Checks if strict validation mode is enabled.
     * @return true if strict validation is enabled (default: false)
     */
    public boolean isStrictValidation() {
        return strictValidation;
    }

    /**
     * Enables or disables strict validation mode.
     * In strict mode, conversions fail on warnings (future dates, invalid identifiers, etc.).
     * In permissive mode, warnings are logged but conversion proceeds.
     * @param strictValidation true for strict mode
     */
    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }

    /**
     * Gets the repository type.
     * @return "memory" or "native"
     */
    public String getRepositoryType() {
        return repositoryType;
    }

    /**
     * Sets the repository type.
     * @param repositoryType "memory" for in-memory repository, "native" for persistent
     */
    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    /**
     * Gets the repository path (for native repositories).
     * @return the file system path, or null for in-memory repositories
     */
    public String getRepositoryPath() {
        return repositoryPath;
    }

    /**
     * Sets the repository path (for native repositories).
     * @param repositoryPath the file system path
     */
    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    /**
     * Creates a configuration from system properties or environment variables.
     * System properties take precedence over defaults.
     *
     * @return a new configuration based on system settings
     */
    public static VocBenchPluginConfiguration fromSystemProperties() {
        VocBenchPluginConfiguration config = new VocBenchPluginConfiguration();

        // Read from system properties
        String format = System.getProperty("vocbench.biblio.outputFormat", "TURTLE");
        config.setOutputFormat(parseRDFFormat(format));

        String validateIds = System.getProperty("vocbench.biblio.validateIdentifiers", "true");
        config.setValidateIdentifiers(Boolean.parseBoolean(validateIds));

        String preserveKeys = System.getProperty("vocbench.biblio.preserveCitationKeys", "true");
        config.setPreserveOriginalCitationKeys(Boolean.parseBoolean(preserveKeys));

        String strict = System.getProperty("vocbench.biblio.strictValidation", "false");
        config.setStrictValidation(Boolean.parseBoolean(strict));

        String repoType = System.getProperty("vocbench.biblio.repositoryType", "memory");
        config.setRepositoryType(repoType);

        String repoPath = System.getProperty("vocbench.biblio.repositoryPath");
        config.setRepositoryPath(repoPath);

        return config;
    }

    /**
     * Parses an RDF format string (case-insensitive).
     * @param formatName the format name (TURTLE, RDFXML, JSONLD, etc.)
     * @return the corresponding RDFFormat, or TURTLE if unrecognized
     */
    private static RDFFormat parseRDFFormat(String formatName) {
        if (formatName == null) {
            return RDFFormat.TURTLE;
        }

        return switch (formatName.toUpperCase()) {
            case "TURTLE", "TTL" -> RDFFormat.TURTLE;
            case "RDFXML", "XML" -> RDFFormat.RDFXML;
            case "JSONLD", "JSON" -> RDFFormat.JSONLD;
            case "NTRIPLES", "NT" -> RDFFormat.NTRIPLES;
            case "N3" -> RDFFormat.N3;
            case "TRIG" -> RDFFormat.TRIG;
            case "NQUADS", "NQ" -> RDFFormat.NQUADS;
            default -> RDFFormat.TURTLE;
        };
    }

    @Override
    public String toString() {
        return "VocBenchPluginConfiguration{" +
                "outputFormat=" + outputFormat +
                ", validateIdentifiers=" + validateIdentifiers +
                ", preserveOriginalCitationKeys=" + preserveOriginalCitationKeys +
                ", strictValidation=" + strictValidation +
                ", repositoryType='" + repositoryType + '\'' +
                ", repositoryPath='" + repositoryPath + '\'' +
                '}';
    }
}
