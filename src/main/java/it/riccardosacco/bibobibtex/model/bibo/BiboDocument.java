package it.riccardosacco.bibobibtex.model.bibo;

public class BiboDocument {
    private final String identifier;

    public BiboDocument(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
    }
}
