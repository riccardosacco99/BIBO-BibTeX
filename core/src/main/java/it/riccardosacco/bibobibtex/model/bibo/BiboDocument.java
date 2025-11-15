package it.riccardosacco.bibobibtex.model.bibo;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

public final class BiboDocument {
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private final String id;
    private final BiboDocumentType type;
    private final String title;
    private final String subtitle;
    private final List<BiboContributor> contributors;
    private final BiboPublicationDate publicationDate;
    private final String publisher;
    private final String placeOfPublication;
    private final String conferenceLocation;
    private final String conferenceOrganizer;
    private final String degreeType;
    private final String containerTitle;
    private final String volume;
    private final String issue;
    private final String pages;
    private final List<BiboIdentifier> identifiers;
    private final String url;
    private final String language;
    private final String abstractText;
    private final String notes;
    private final String series;
    private final String edition;
    private final List<String> keywords;
    private final Model model;
    private final Resource resource;

    private BiboDocument(Builder builder, Resource resource, Model model) {
        this.id = builder.id;
        this.type = builder.type;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.contributors = List.copyOf(builder.contributors);
        this.publicationDate = builder.publicationDate;
        this.publisher = builder.publisher;
        this.placeOfPublication = builder.placeOfPublication;
        this.conferenceLocation = builder.conferenceLocation;
        this.conferenceOrganizer = builder.conferenceOrganizer;
        this.degreeType = builder.degreeType;
        this.containerTitle = builder.containerTitle;
        this.volume = builder.volume;
        this.issue = builder.issue;
        this.pages = builder.pages;
        this.identifiers = List.copyOf(builder.identifiers);
        this.url = builder.url;
        this.language = builder.language;
        this.abstractText = builder.abstractText;
        this.notes = builder.notes;
        this.series = builder.series;
        this.edition = builder.edition;
        this.keywords = List.copyOf(builder.keywords);
        this.model = new LinkedHashModel(model);
        this.resource = resource;
    }

    public static Builder builder(BiboDocumentType type, String title) {
        return new Builder(type, title);
    }

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    public BiboDocumentType type() {
        return type;
    }

    public String title() {
        return title;
    }

    public Optional<String> subtitle() {
        return Optional.ofNullable(subtitle);
    }

    public List<BiboContributor> contributors() {
        return contributors;
    }

    public List<BiboContributor> contributorsByRole(BiboContributorRole role) {
        Objects.requireNonNull(role, "role");
        return contributors.stream()
                .filter(contributor -> contributor.role() == role)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<BiboContributor> authors() {
        return contributorsByRole(BiboContributorRole.AUTHOR);
    }

    public List<BiboContributor> editors() {
        return contributorsByRole(BiboContributorRole.EDITOR);
    }

    public Optional<BiboPublicationDate> publicationDate() {
        return Optional.ofNullable(publicationDate);
    }

    public Optional<String> publisher() {
        return Optional.ofNullable(publisher);
    }

    public Optional<String> placeOfPublication() {
        return Optional.ofNullable(placeOfPublication);
    }

    public Optional<String> conferenceLocation() {
        return Optional.ofNullable(conferenceLocation);
    }

    public Optional<String> conferenceOrganizer() {
        return Optional.ofNullable(conferenceOrganizer);
    }

    public Optional<String> degreeType() {
        return Optional.ofNullable(degreeType);
    }

    public Optional<String> containerTitle() {
        return Optional.ofNullable(containerTitle);
    }

    public Optional<String> volume() {
        return Optional.ofNullable(volume);
    }

    public Optional<String> issue() {
        return Optional.ofNullable(issue);
    }

    public Optional<String> pages() {
        return Optional.ofNullable(pages);
    }

    public List<BiboIdentifier> identifiers() {
        return identifiers;
    }

    public Optional<String> url() {
        return Optional.ofNullable(url);
    }

    public Optional<String> language() {
        return Optional.ofNullable(language);
    }

    public Optional<String> abstractText() {
        return Optional.ofNullable(abstractText);
    }

    public Optional<String> notes() {
        return Optional.ofNullable(notes);
    }

    public Optional<String> series() {
        return Optional.ofNullable(series);
    }

    public Optional<String> edition() {
        return Optional.ofNullable(edition);
    }

    public List<String> keywords() {
        return keywords;
    }

    public Model rdfModel() {
        return new LinkedHashModel(model);
    }

    public Resource resource() {
        return resource;
    }

    /**
     * Serializes this document to Turtle format (pretty-printed).
     *
     * @return the Turtle representation as a String
     */
    public String toTurtle() {
        StringWriter writer = new StringWriter();
        writeTurtle(writer);
        return writer.toString();
    }

    /**
     * Writes this document to a Writer in Turtle format.
     *
     * @param writer the Writer to write to
     */
    public void writeTurtle(Writer writer) {
        write(writer, RDFFormat.TURTLE);
    }

    /**
     * Writes this document to a Writer in the specified RDF format.
     * Configures the writer with optimal settings for readability:
     * - Pretty printing enabled
     * - Blank nodes inlined where possible
     * - XSD strings converted to plain literals
     * - Language-tagged literals preserved
     *
     * @param writer the Writer to write to
     * @param format the RDF format to use (TURTLE, RDFXML, JSONLD, etc.)
     */
    public void write(Writer writer, RDFFormat format) {
        try {
            RDFWriter rdfWriter = Rio.createWriter(format, writer);
            WriterConfig config = rdfWriter.getWriterConfig();

            // Configure optimal settings for readability (FIX-03)
            config.set(BasicWriterSettings.PRETTY_PRINT, true);
            config.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            config.set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true);
            config.set(BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, true);

            Rio.write(model, rdfWriter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write RDF in format " + format, e);
        }
    }

    public static final class Builder {
        private final BiboDocumentType type;
        private final String title;
        private String id;
        private String subtitle;
        private final List<BiboContributor> contributors = new ArrayList<>();
        private BiboPublicationDate publicationDate;
        private String publisher;
        private String placeOfPublication;
        private String conferenceLocation;
        private String conferenceOrganizer;
        private String degreeType;
        private String containerTitle;
        private String volume;
        private String issue;
        private String pages;
        private final List<BiboIdentifier> identifiers = new ArrayList<>();
        private String url;
        private String language;
        private String abstractText;
        private String notes;
        private String series;
        private String edition;
        private final List<String> keywords = new ArrayList<>();

        private Builder(BiboDocumentType type, String title) {
            this.type = Objects.requireNonNull(type, "type");
            this.title = normalizeRequired(title, "title");
        }

        public Builder id(String id) {
            this.id = normalizeOptional(id);
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = normalizeOptional(subtitle);
            return this;
        }

        public Builder addContributor(BiboContributor contributor) {
            contributors.add(Objects.requireNonNull(contributor, "contributor"));
            return this;
        }

        public Builder contributors(Collection<BiboContributor> contributors) {
            if (contributors != null) {
                contributors.forEach(this::addContributor);
            }
            return this;
        }

        public Builder addAuthor(BiboPersonName name) {
            return addContributor(new BiboContributor(name, BiboContributorRole.AUTHOR));
        }

        public Builder addEditor(BiboPersonName name) {
            return addContributor(new BiboContributor(name, BiboContributorRole.EDITOR));
        }

        public Builder publicationDate(BiboPublicationDate publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public Builder publisher(String publisher) {
            this.publisher = normalizeOptional(publisher);
            return this;
        }

        public Builder placeOfPublication(String placeOfPublication) {
            this.placeOfPublication = normalizeOptional(placeOfPublication);
            return this;
        }

        public Builder conferenceLocation(String conferenceLocation) {
            this.conferenceLocation = normalizeOptional(conferenceLocation);
            return this;
        }

        public Builder conferenceOrganizer(String conferenceOrganizer) {
            this.conferenceOrganizer = normalizeOptional(conferenceOrganizer);
            return this;
        }

        public Builder degreeType(String degreeType) {
            this.degreeType = normalizeOptional(degreeType);
            return this;
        }

        public Builder containerTitle(String containerTitle) {
            this.containerTitle = normalizeOptional(containerTitle);
            return this;
        }

        public Builder volume(String volume) {
            this.volume = normalizeOptional(volume);
            return this;
        }

        public Builder issue(String issue) {
            this.issue = normalizeOptional(issue);
            return this;
        }

        public Builder pages(String pages) {
            this.pages = normalizeOptional(pages);
            return this;
        }

        public Builder addIdentifier(BiboIdentifier identifier) {
            identifiers.add(Objects.requireNonNull(identifier, "identifier"));
            return this;
        }

        public Builder identifiers(Collection<BiboIdentifier> identifiers) {
            if (identifiers != null) {
                identifiers.forEach(this::addIdentifier);
            }
            return this;
        }

        public Builder url(String url) {
            this.url = normalizeOptional(url);
            return this;
        }

        public Builder language(String language) {
            this.language = normalizeOptional(language);
            return this;
        }

        public Builder abstractText(String abstractText) {
            this.abstractText = normalizeOptional(abstractText);
            return this;
        }

        public Builder notes(String notes) {
            this.notes = normalizeOptional(notes);
            return this;
        }

        public Builder series(String series) {
            this.series = normalizeOptional(series);
            return this;
        }

        public Builder edition(String edition) {
            this.edition = normalizeOptional(edition);
            return this;
        }

        public Builder addKeyword(String keyword) {
            if (keyword != null && !keyword.isBlank()) {
                keywords.add(keyword.strip());
            }
            return this;
        }

        public Builder keywords(Collection<String> keywords) {
            if (keywords != null) {
                keywords.forEach(this::addKeyword);
            }
            return this;
        }

        public BiboDocument build() {
            Model model = new LinkedHashModel();
            model.setNamespace(BiboVocabulary.NS);
            model.setNamespace(DCTERMS.NS);
            model.setNamespace(FOAF.NS);
            model.setNamespace(RDF.NS);
            model.setNamespace(RDFS.NS);

            Resource subject = createSubject();
            model.add(subject, RDF.TYPE, BiboVocabulary.DOCUMENT);
            model.add(subject, RDF.TYPE, type.iri());
            model.add(subject, DCTERMS.TITLE, VF.createLiteral(title));
            if (subtitle != null) {
                model.add(subject, BiboVocabulary.SUBTITLE, VF.createLiteral(subtitle));
            }
            if (id != null) {
                model.add(subject, DCTERMS.IDENTIFIER, VF.createLiteral(id));
            }

            addContributors(model, subject);

            if (publicationDate != null) {
                model.add(subject, DCTERMS.ISSUED, publicationDate.toLiteral(VF));
            }
            if (publisher != null) {
                model.add(subject, DCTERMS.PUBLISHER, VF.createLiteral(publisher));
            }
            if (placeOfPublication != null) {
                model.add(subject, DCTERMS.SPATIAL, VF.createLiteral(placeOfPublication));
            }

            if (containerTitle != null) {
                Resource container = VF.createBNode();
                model.add(subject, DCTERMS.IS_PART_OF, container);
                model.add(container, RDF.TYPE, BiboVocabulary.DOCUMENT);
                model.add(container, DCTERMS.TITLE, VF.createLiteral(containerTitle));

                // Add conference-specific metadata to container
                if (conferenceLocation != null) {
                    model.add(container, DCTERMS.SPATIAL, VF.createLiteral(conferenceLocation));
                }
                if (conferenceOrganizer != null) {
                    model.add(container, BiboVocabulary.ORGANIZER, VF.createLiteral(conferenceOrganizer));
                }
            }

            // Add thesis degree type
            if (degreeType != null) {
                model.add(subject, BiboVocabulary.DEGREE, VF.createLiteral(degreeType));
            }

            if (volume != null) {
                model.add(subject, BiboVocabulary.VOLUME, VF.createLiteral(volume));
            }
            if (issue != null) {
                model.add(subject, BiboVocabulary.ISSUE, VF.createLiteral(issue));
            }
            if (pages != null) {
                model.add(subject, BiboVocabulary.PAGES, VF.createLiteral(pages));
            }

            identifiers.forEach(identifier -> addIdentifier(model, subject, identifier));

            if (url != null) {
                addIriOrLiteral(model, subject, FOAF.PAGE, url);
            }
            if (language != null) {
                model.add(subject, DCTERMS.LANGUAGE, VF.createLiteral(language));
            }
            if (abstractText != null) {
                model.add(subject, DCTERMS.ABSTRACT, VF.createLiteral(abstractText));
            }
            if (notes != null) {
                model.add(subject, RDFS.COMMENT, VF.createLiteral(notes));
            }
            if (series != null) {
                model.add(subject, BiboVocabulary.SERIES, VF.createLiteral(series));
            }
            if (edition != null) {
                model.add(subject, BiboVocabulary.EDITION, VF.createLiteral(edition));
            }
            keywords.forEach(keyword ->
                model.add(subject, DCTERMS.SUBJECT, VF.createLiteral(keyword))
            );

            return new BiboDocument(this, subject, model);
        }

        private Resource createSubject() {
            if (id != null) {
                try {
                    return VF.createIRI(id);
                } catch (IllegalArgumentException ignored) {
                    // fall through to blank node
                }
            }
            return VF.createBNode();
        }

        /**
         * Adds contributors to the model using RDF Collections for authors and editors.
         * Authors are stored in bibo:authorList, editors in bibo:editorList, both as ordered RDF Lists.
         * Other contributor roles (translator, advisor, reviewer) are stored as individual properties.
         */
        private void addContributors(Model model, Resource subject) {
            // Group contributors by role
            Map<BiboContributorRole, List<BiboContributor>> byRole = contributors.stream()
                .collect(Collectors.groupingBy(BiboContributor::role));

            // Process authors as RDF List
            List<BiboContributor> authors = byRole.get(BiboContributorRole.AUTHOR);
            if (authors != null && !authors.isEmpty()) {
                List<Resource> authorNodes = authors.stream()
                    .map(contributor -> createPersonNode(model, contributor.name()))
                    .collect(Collectors.toList());

                Resource authorListHead = VF.createBNode();
                RDFCollections.asRDF(authorNodes, authorListHead, model);
                model.add(subject, BiboVocabulary.AUTHOR_LIST, authorListHead);
            }

            // Process editors as RDF List
            List<BiboContributor> editors = byRole.get(BiboContributorRole.EDITOR);
            if (editors != null && !editors.isEmpty()) {
                List<Resource> editorNodes = editors.stream()
                    .map(contributor -> createPersonNode(model, contributor.name()))
                    .collect(Collectors.toList());

                Resource editorListHead = VF.createBNode();
                RDFCollections.asRDF(editorNodes, editorListHead, model);
                model.add(subject, BiboVocabulary.EDITOR_LIST, editorListHead);
            }

            // Process other roles individually (not as lists)
            for (BiboContributorRole role : Arrays.asList(
                    BiboContributorRole.TRANSLATOR,
                    BiboContributorRole.ADVISOR,
                    BiboContributorRole.REVIEWER,
                    BiboContributorRole.CONTRIBUTOR)) {
                List<BiboContributor> others = byRole.get(role);
                if (others != null) {
                    for (BiboContributor contributor : others) {
                        Resource person = createPersonNode(model, contributor.name());
                        model.add(subject, predicateForRole(role), person);
                    }
                }
            }
        }

        /**
         * Creates a person node in the RDF model with FOAF properties.
         *
         * @param model the RDF model to add statements to
         * @param name the person's name information
         * @return the Resource representing the person (blank node)
         */
        private Resource createPersonNode(Model model, BiboPersonName name) {
            Resource person = VF.createBNode();
            model.add(person, RDF.TYPE, FOAF.PERSON);
            model.add(person, FOAF.NAME, VF.createLiteral(name.fullName()));
            name.givenName().ifPresent(value ->
                model.add(person, FOAF.GIVEN_NAME, VF.createLiteral(value)));
            name.familyName().ifPresent(value ->
                model.add(person, FOAF.FAMILY_NAME, VF.createLiteral(value)));
            return person;
        }

        private void addIdentifier(Model model, Resource subject, BiboIdentifier identifier) {
            identifier.type()
                    .predicate()
                    .ifPresent(predicate -> {
                        if (predicate.equals(BiboVocabulary.URI) || predicate.equals(FOAF.PAGE)) {
                            addIriOrLiteral(model, subject, predicate, identifier.value());
                        } else {
                            model.add(subject, predicate, VF.createLiteral(identifier.value()));
                        }
                    });
        }

        private void addIriOrLiteral(Model model, Resource subject, IRI predicate, String value) {
            try {
                model.add(subject, predicate, VF.createIRI(value));
            } catch (IllegalArgumentException ex) {
                model.add(subject, predicate, VF.createLiteral(value));
            }
        }

        private static org.eclipse.rdf4j.model.IRI predicateForRole(BiboContributorRole role) {
            return switch (role) {
                case AUTHOR -> DCTERMS.CREATOR;
                case EDITOR -> BiboVocabulary.EDITOR;
                case TRANSLATOR -> BiboVocabulary.TRANSLATOR;
                case ADVISOR -> BiboVocabulary.ADVISOR;
                case REVIEWER -> BiboVocabulary.REVIEWER;
                case CONTRIBUTOR -> DCTERMS.CONTRIBUTOR;
            };
        }

        private static String normalizeRequired(String value, String fieldName) {
            Objects.requireNonNull(value, fieldName);
            String trimmed = value.strip();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException(fieldName + " cannot be blank");
            }
            return trimmed;
        }

        private static String normalizeOptional(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.strip();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
