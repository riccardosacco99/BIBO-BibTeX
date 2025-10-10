package it.riccardosacco.bibobibtex.model.bibtex;

public class BibTeXEntry {
    private final String citationKey;

    public BibTeXEntry(String citationKey) {
        this.citationKey = citationKey;
    }

    public String citationKey() {
        return citationKey;
    }
}
