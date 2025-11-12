package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import it.riccardosacco.bibobibtex.model.bibo.BiboPersonName;
import java.util.List;
import java.util.stream.Collectors;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.jupiter.api.Test;

class NameParsingTest {

    private final BibTeXBibliographicConverter converter = new BibTeXBibliographicConverter();

    @Test
    void singleNameMadonna() {
        BiboPersonName name = parseSingleAuthor("Madonna");
        assertFalse(name.givenName().isPresent());
        assertEquals("Madonna", name.familyName().orElseThrow());
    }

    @Test
    void singleNamePlato() {
        BiboPersonName name = parseSingleAuthor("Plato");
        assertFalse(name.givenName().isPresent());
        assertEquals("Plato", name.familyName().orElseThrow());
    }

    @Test
    void particleVanGogh() {
        BiboPersonName name = parseSingleAuthor("Vincent van Gogh");
        assertEquals("Vincent", name.givenName().orElseThrow());
        assertEquals("van Gogh", name.familyName().orElseThrow());
    }

    @Test
    void particleVonNeumann() {
        BiboPersonName name = parseSingleAuthor("John von Neumann");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("von Neumann", name.familyName().orElseThrow());
    }

    @Test
    void particleDeGaulle() {
        BiboPersonName name = parseSingleAuthor("Charles de Gaulle");
        assertEquals("Charles", name.givenName().orElseThrow());
        assertEquals("de Gaulle", name.familyName().orElseThrow());
    }

    @Test
    void particleDellaFrancesca() {
        BiboPersonName name = parseSingleAuthor("Piero della Francesca");
        assertEquals("Piero", name.givenName().orElseThrow());
        assertEquals("della Francesca", name.familyName().orElseThrow());
    }

    @Test
    void particleIbnSina() {
        BiboPersonName name = parseSingleAuthor("Abu Ali ibn Sina");
        assertEquals("Abu Ali", name.givenName().orElseThrow());
        assertEquals("ibn Sina", name.familyName().orElseThrow());
    }

    @Test
    void suffixThreePartFormat() {
        BiboPersonName name = parseSingleAuthor("von Braun, Jr, Wernher");
        assertEquals("Wernher", name.givenName().orElseThrow());
        assertEquals("von Braun", name.familyName().orElseThrow());
        assertEquals("Jr", name.suffix().orElseThrow());
    }

    @Test
    void suffixTwoPartFormatClassic() {
        BiboPersonName name = parseSingleAuthor("John Smith, Jr");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("Smith", name.familyName().orElseThrow());
        assertEquals("Jr", name.suffix().orElseThrow());
    }

    @Test
    void suffixProfessionalTitleRecognized() {
        BiboPersonName name = parseSingleAuthor("Jane Doe, PhD");
        assertEquals("Jane", name.givenName().orElseThrow());
        assertEquals("Doe", name.familyName().orElseThrow());
        assertEquals("PhD", name.suffix().orElseThrow());
    }

    @Test
    void suffixBaronExampleFromPlan() {
        BiboPersonName name = parseSingleAuthor("Charles Louis de Secondat, Baron de Montesquieu");
        assertEquals("Charles Louis", name.givenName().orElseThrow());
        assertEquals("de Secondat", name.familyName().orElseThrow());
        assertEquals("Baron de Montesquieu", name.suffix().orElseThrow());
    }

    @Test
    void middleNameMultiWord() {
        BiboPersonName name = parseSingleAuthor("Mary Anne Johnson");
        assertEquals("Mary Anne", name.givenName().orElseThrow());
        assertEquals("Johnson", name.familyName().orElseThrow());
    }

    @Test
    void middleNameUnicode() {
        BiboPersonName name = parseSingleAuthor("Ana María López");
        assertEquals("Ana María", name.givenName().orElseThrow());
        assertEquals("López", name.familyName().orElseThrow());
    }

    @Test
    void middleNameHyphen() {
        BiboPersonName name = parseSingleAuthor("Jean-Luc Picard");
        assertEquals("Jean-Luc", name.givenName().orElseThrow());
        assertEquals("Picard", name.familyName().orElseThrow());
    }

    @Test
    void combinedParticleAndSuffix() {
        BiboPersonName name = parseSingleAuthor("Miguel de Cervantes, Conde de Esquivias");
        assertEquals("Miguel", name.givenName().orElseThrow());
        assertEquals("de Cervantes", name.familyName().orElseThrow());
        assertEquals("Conde de Esquivias", name.suffix().orElseThrow());
    }

    @Test
    void bracesAreStripped() {
        BiboPersonName name = parseSingleAuthor("{Ludwig van Beethoven}");
        assertEquals("Ludwig", name.givenName().orElseThrow());
        assertEquals("van Beethoven", name.familyName().orElseThrow());
    }

    @Test
    void apostropheFamilyNamePreserved() {
        BiboPersonName name = parseSingleAuthor("Patrick O'Brien");
        assertEquals("Patrick", name.givenName().orElseThrow());
        assertEquals("O'Brien", name.familyName().orElseThrow());
    }

    @Test
    void longNamesSplitCorrectly() {
        BiboPersonName name = parseSingleAuthor("Juan Carlos Alberto de la Vega");
        assertEquals("Juan Carlos Alberto", name.givenName().orElseThrow());
        assertEquals("de la Vega", name.familyName().orElseThrow());
    }

    @Test
    void unicodeSingleCharacterName() {
        BiboPersonName name = parseSingleAuthor("李明");
        assertFalse(name.givenName().isPresent());
        assertEquals("李明", name.familyName().orElseThrow());
    }

    @Test
    void multipleAuthorsMaintainOrderWithNewParser() {
        BiboPersonName first = parseAuthors("First, Alice and Second, Bob").get(0);
        BiboPersonName second = parseAuthors("First, Alice and Second, Bob").get(1);
        assertEquals("Alice", first.givenName().orElseThrow());
        assertEquals("First", first.familyName().orElseThrow());
        assertEquals("Bob", second.givenName().orElseThrow());
        assertEquals("Second", second.familyName().orElseThrow());
    }

    private BiboPersonName parseSingleAuthor(String authorField) {
        List<BiboPersonName> names = parseAuthors(authorField);
        return names.get(0);
    }

    private List<BiboPersonName> parseAuthors(String authorField) {
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key("nameTest"));
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue("Name Parsing Test", StringValue.Style.BRACED));
        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue(authorField, StringValue.Style.BRACED));
        return converter.convertToBibo(entry).orElseThrow().authors().stream()
                .map(contributor -> contributor.name())
                .collect(Collectors.toList());
    }
}
