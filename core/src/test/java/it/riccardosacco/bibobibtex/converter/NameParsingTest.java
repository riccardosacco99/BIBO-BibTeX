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
        assertEquals("van", name.nameParticle().orElseThrow());
        assertEquals("Gogh", name.familyName().orElseThrow());
    }

    @Test
    void particleVonNeumann() {
        BiboPersonName name = parseSingleAuthor("John von Neumann");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("von", name.nameParticle().orElseThrow());
        assertEquals("Neumann", name.familyName().orElseThrow());
    }

    @Test
    void particleDeGaulle() {
        BiboPersonName name = parseSingleAuthor("Charles de Gaulle");
        assertEquals("Charles", name.givenName().orElseThrow());
        assertEquals("de", name.nameParticle().orElseThrow());
        assertEquals("Gaulle", name.familyName().orElseThrow());
    }

    @Test
    void particleDellaFrancesca() {
        BiboPersonName name = parseSingleAuthor("Piero della Francesca");
        assertEquals("Piero", name.givenName().orElseThrow());
        assertEquals("della", name.nameParticle().orElseThrow());
        assertEquals("Francesca", name.familyName().orElseThrow());
    }

    @Test
    void particleIbnSina() {
        BiboPersonName name = parseSingleAuthor("Abu Ali ibn Sina");
        assertEquals("Abu", name.givenName().orElseThrow());
        assertEquals("Ali", name.middleName().orElseThrow());
        assertEquals("ibn", name.nameParticle().orElseThrow());
        assertEquals("Sina", name.familyName().orElseThrow());
    }

    @Test
    void suffixThreePartFormat() {
        BiboPersonName name = parseSingleAuthor("von Braun, Jr, Wernher");
        assertEquals("Wernher", name.givenName().orElseThrow());
        assertEquals("von", name.nameParticle().orElseThrow());
        assertEquals("Braun", name.familyName().orElseThrow());
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
        assertEquals("Charles", name.givenName().orElseThrow());
        assertEquals("Louis", name.middleName().orElseThrow());
        assertEquals("de", name.nameParticle().orElseThrow());
        assertEquals("Secondat", name.familyName().orElseThrow());
        assertEquals("Baron de Montesquieu", name.suffix().orElseThrow());
    }

    @Test
    void middleNameMultiWord() {
        BiboPersonName name = parseSingleAuthor("Mary Anne Johnson");
        assertEquals("Mary", name.givenName().orElseThrow());
        assertEquals("Anne", name.middleName().orElseThrow());
        assertEquals("Johnson", name.familyName().orElseThrow());
    }

    @Test
    void middleNameUnicode() {
        BiboPersonName name = parseSingleAuthor("Ana María López");
        assertEquals("Ana", name.givenName().orElseThrow());
        assertEquals("María", name.middleName().orElseThrow());
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
        assertEquals("de", name.nameParticle().orElseThrow());
        assertEquals("Cervantes", name.familyName().orElseThrow());
        assertEquals("Conde de Esquivias", name.suffix().orElseThrow());
    }

    @Test
    void bracesAreStripped() {
        BiboPersonName name = parseSingleAuthor("{Ludwig van Beethoven}");
        assertEquals("Ludwig", name.givenName().orElseThrow());
        assertEquals("van", name.nameParticle().orElseThrow());
        assertEquals("Beethoven", name.familyName().orElseThrow());
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
        assertEquals("Juan Carlos", name.givenName().orElseThrow());
        assertEquals("Alberto", name.middleName().orElseThrow());
        assertEquals("de la", name.nameParticle().orElseThrow());
        assertEquals("Vega", name.familyName().orElseThrow());
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

    // Tests for new middleName field
    @Test
    void middleNameExtractedFromThreeTokenName() {
        BiboPersonName name = parseSingleAuthor("John Sebastian Bach");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("Sebastian", name.middleName().orElseThrow());
        assertEquals("Bach", name.familyName().orElseThrow());
    }

    @Test
    void middleNameExtractedFromFourTokenName() {
        BiboPersonName name = parseSingleAuthor("Ludwig Wilhelm van Beethoven");
        assertEquals("Ludwig", name.givenName().orElseThrow());
        assertEquals("Wilhelm", name.middleName().orElseThrow());
        assertEquals("van", name.nameParticle().orElseThrow());
        assertEquals("Beethoven", name.familyName().orElseThrow());
    }

    @Test
    void middleNameWithSuffix() {
        BiboPersonName name = parseSingleAuthor("Martin Luther King, Jr.");
        assertEquals("Martin", name.givenName().orElseThrow());
        assertEquals("Luther", name.middleName().orElseThrow());
        assertEquals("King", name.familyName().orElseThrow());
        assertEquals("Jr.", name.suffix().orElseThrow());
    }

    // Tests for nameParticle field separately
    @Test
    void nameParticleExtractedSeparately_van() {
        BiboPersonName name = parseSingleAuthor("Vincent van Gogh");
        assertEquals("Vincent", name.givenName().orElseThrow());
        assertEquals("van", name.nameParticle().orElseThrow());
        assertEquals("Gogh", name.familyName().orElseThrow());
    }

    @Test
    void nameParticleExtractedSeparately_von() {
        BiboPersonName name = parseSingleAuthor("John von Neumann");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("von", name.nameParticle().orElseThrow());
        assertEquals("Neumann", name.familyName().orElseThrow());
    }

    @Test
    void nameParticleExtractedSeparately_de() {
        BiboPersonName name = parseSingleAuthor("Charles de Gaulle");
        assertEquals("Charles", name.givenName().orElseThrow());
        assertEquals("de", name.nameParticle().orElseThrow());
        assertEquals("Gaulle", name.familyName().orElseThrow());
    }

    @Test
    void nameParticleExtractedSeparately_della() {
        BiboPersonName name = parseSingleAuthor("Piero della Francesca");
        assertEquals("Piero", name.givenName().orElseThrow());
        assertEquals("della", name.nameParticle().orElseThrow());
        assertEquals("Francesca", name.familyName().orElseThrow());
    }

    @Test
    void multipleParticles_dela() {
        BiboPersonName name = parseSingleAuthor("Juan Carlos de la Vega");
        assertEquals("Juan", name.givenName().orElseThrow());
        assertEquals("Carlos", name.middleName().orElseThrow());
        assertEquals("de la", name.nameParticle().orElseThrow());
        assertEquals("Vega", name.familyName().orElseThrow());
    }

    @Test
    void suffixIII() {
        BiboPersonName name = parseSingleAuthor("John Smith, III");
        assertEquals("John", name.givenName().orElseThrow());
        assertEquals("Smith", name.familyName().orElseThrow());
        assertEquals("III", name.suffix().orElseThrow());
    }

    @Test
    void suffixSr() {
        BiboPersonName name = parseSingleAuthor("Robert Brown, Sr");
        assertEquals("Robert", name.givenName().orElseThrow());
        assertEquals("Brown", name.familyName().orElseThrow());
        assertEquals("Sr", name.suffix().orElseThrow());
    }

    @Test
    void hyphenatedFamilyName_SaintExupery() {
        BiboPersonName name = parseSingleAuthor("Antoine de Saint-Exupéry");
        assertEquals("Antoine", name.givenName().orElseThrow());
        assertEquals("de", name.nameParticle().orElseThrow());
        assertEquals("Saint-Exupéry", name.familyName().orElseThrow());
    }

    @Test
    void complexNameWithAllComponents() {
        BiboPersonName name = parseSingleAuthor("Johann Wolfgang von Goethe");
        assertEquals("Johann", name.givenName().orElseThrow());
        assertEquals("Wolfgang", name.middleName().orElseThrow());
        assertEquals("von", name.nameParticle().orElseThrow());
        assertEquals("Goethe", name.familyName().orElseThrow());
    }

    @Test
    void particleWithCommaFormat() {
        BiboPersonName name = parseSingleAuthor("van der Waals, Johannes");
        assertEquals("Johannes", name.givenName().orElseThrow());
        assertEquals("van der", name.nameParticle().orElseThrow());
        assertEquals("Waals", name.familyName().orElseThrow());
    }

    @Test
    void arabicNameWithBin() {
        BiboPersonName name = parseSingleAuthor("Mohammed bin Rashid");
        assertEquals("Mohammed", name.givenName().orElseThrow());
        assertEquals("bin", name.nameParticle().orElseThrow());
        assertEquals("Rashid", name.familyName().orElseThrow());
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
