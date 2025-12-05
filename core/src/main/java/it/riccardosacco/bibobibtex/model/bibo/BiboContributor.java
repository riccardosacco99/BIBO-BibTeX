package it.riccardosacco.bibobibtex.model.bibo;

import java.util.Objects;
import java.util.Optional;

public final class BiboContributor {
    private final BiboPersonName name;
    private final BiboContributorRole role;
    private final String affiliation;

    public BiboContributor(BiboPersonName name, BiboContributorRole role) {
        this(name, role, null);
    }

    public BiboContributor(BiboPersonName name, BiboContributorRole role, String affiliation) {
        this.name = Objects.requireNonNull(name, "name");
        this.role = Objects.requireNonNull(role, "role");
        this.affiliation = normalizeOptional(affiliation);
    }

    public BiboPersonName name() {
        return name;
    }

    public BiboContributorRole role() {
        return role;
    }

    public Optional<String> affiliation() {
        return Optional.ofNullable(affiliation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BiboContributor that = (BiboContributor) o;
        return Objects.equals(name, that.name)
                && role == that.role
                && Objects.equals(affiliation, that.affiliation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, role, affiliation);
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
