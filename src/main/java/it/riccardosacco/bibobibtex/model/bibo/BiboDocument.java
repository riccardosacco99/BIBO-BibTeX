package it.riccardosacco.bibobibtex.model.bibo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BiboDocument {
    private final String id;
    private final BiboDocumentType type;
    private final String title;
    private final String subtitle;
    private final List<BiboContributor> contributors;
    private final BiboPublicationDate publicationDate;
    private final String publisher;
    private final String placeOfPublication;
    private final String containerTitle;
    private final String volume;
    private final String issue;
    private final String pages;
    private final List<BiboIdentifier> identifiers;
    private final String url;
    private final String language;
    private final String abstractText;
    private final String notes;

    private BiboDocument(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.contributors = List.copyOf(builder.contributors);
        this.publicationDate = builder.publicationDate;
        this.publisher = builder.publisher;
        this.placeOfPublication = builder.placeOfPublication;
        this.containerTitle = builder.containerTitle;
        this.volume = builder.volume;
        this.issue = builder.issue;
        this.pages = builder.pages;
        this.identifiers = List.copyOf(builder.identifiers);
        this.url = builder.url;
        this.language = builder.language;
        this.abstractText = builder.abstractText;
        this.notes = builder.notes;
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

    public static final class Builder {
        private final BiboDocumentType type;
        private final String title;
        private String id;
        private String subtitle;
        private final List<BiboContributor> contributors = new ArrayList<>();
        private BiboPublicationDate publicationDate;
        private String publisher;
        private String placeOfPublication;
        private String containerTitle;
        private String volume;
        private String issue;
        private String pages;
        private final List<BiboIdentifier> identifiers = new ArrayList<>();
        private String url;
        private String language;
        private String abstractText;
        private String notes;

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

        public BiboDocument build() {
            return new BiboDocument(this);
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
