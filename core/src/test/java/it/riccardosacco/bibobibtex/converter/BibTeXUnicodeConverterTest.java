package it.riccardosacco.bibobibtex.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Comprehensive test suite for BibTeX Unicode conversion.
 * Tests 50+ escape sequences covering all common LaTeX diacritics.
 */
class BibTeXUnicodeConverterTest {

    // ========== Acute Accent Tests (') ==========

    @ParameterizedTest
    @CsvSource({
        "{\\'e}, é",
        "{\\'E}, É",
        "{\\'a}, á",
        "{\\'A}, Á",
        "{\\'i}, í",
        "{\\'I}, Í",
        "{\\'o}, ó",
        "{\\'O}, Ó",
        "{\\'u}, ú",
        "{\\'U}, Ú",
        "{\\'y}, ý",
        "{\\'Y}, Ý",
        "{\\'c}, ć",
        "{\\'n}, ń",
        "{\\'s}, ś",
        "{\\'z}, ź"
    })
    void testAcuteAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_AcuteAccent() {
        String input = "Guasch-Ferr{\\'e}, Marta";
        String expected = "Guasch-Ferré, Marta";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Grave Accent Tests (`) ==========

    @ParameterizedTest
    @CsvSource({
        "{\\`a}, à",
        "{\\`A}, À",
        "{\\`e}, è",
        "{\\`E}, È",
        "{\\`i}, ì",
        "{\\`I}, Ì",
        "{\\`o}, ò",
        "{\\`O}, Ò",
        "{\\`u}, ù",
        "{\\`U}, Ù"
    })
    void testGraveAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_GraveAccent() {
        String input = "Ren{\\`e} Descartes";
        String expected = "Renè Descartes";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Umlaut/Diaeresis Tests (") ==========

    @ParameterizedTest
    @CsvSource({
        "{\\\"a}, ä",
        "{\\\"A}, Ä",
        "{\\\"e}, ë",
        "{\\\"E}, Ë",
        "{\\\"i}, ï",
        "{\\\"I}, Ï",
        "{\\\"o}, ö",
        "{\\\"O}, Ö",
        "{\\\"u}, ü",
        "{\\\"U}, Ü",
        "{\\\"y}, ÿ"
    })
    void testUmlautAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_Umlaut() {
        String input = "M{\\\"u}ller, Hans";
        String expected = "Müller, Hans";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Circumflex Tests (^) ==========

    @ParameterizedTest
    @CsvSource({
        "{\\^a}, â",
        "{\\^A}, Â",
        "{\\^e}, ê",
        "{\\^E}, Ê",
        "{\\^i}, î",
        "{\\^I}, Î",
        "{\\^o}, ô",
        "{\\^O}, Ô",
        "{\\^u}, û",
        "{\\^U}, Û"
    })
    void testCircumflexAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_Circumflex() {
        String input = "Fran{\\^e}ois Truffaut";
        String expected = "Franêois Truffaut";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Tilde Tests (~) ==========

    @ParameterizedTest
    @CsvSource({
        "{\\~n}, ñ",
        "{\\~N}, Ñ",
        "{\\~a}, ã",
        "{\\~A}, Ã",
        "{\\~o}, õ",
        "{\\~O}, Õ"
    })
    void testTildeAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_Tilde() {
        String input = "Pi{\\~n}era, Sebasti{\\'a}n";
        String expected = "Piñera, Sebastián";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Macron Tests (=) ==========

    @ParameterizedTest
    @CsvSource({
        "{\\=a}, ā",
        "{\\=A}, Ā",
        "{\\=e}, ē",
        "{\\=E}, Ē",
        "{\\=i}, ī",
        "{\\=I}, Ī",
        "{\\=o}, ō",
        "{\\=O}, Ō",
        "{\\=u}, ū",
        "{\\=U}, Ū"
    })
    void testMacronAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Caron Tests (v) ==========

    @ParameterizedTest
    @CsvSource({
        "{\\vc}, č",
        "{\\vC}, Č",
        "{\\vs}, š",
        "{\\vS}, Š",
        "{\\vz}, ž",
        "{\\vZ}, Ž"
    })
    void testCaronAccents(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_Caron() {
        String input = "Dvor{\\vc}k, Anton{\\'i}n";
        String expected = "Dvorčk, Antonín";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Special Characters Tests ==========

    @ParameterizedTest
    @CsvSource({
        "{\\aa}, å",
        "{\\AA}, Å",
        "{\\ae}, æ",
        "{\\AE}, Æ",
        "{\\o}, ø",
        "{\\O}, Ø",
        "{\\l}, ł",
        "{\\L}, Ł",
        "{\\ss}, ß",
        "{\\oe}, œ",
        "{\\OE}, Œ"
    })
    void testSpecialCharacters(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_SpecialChars() {
        String input = "{\\O}stergaard, Morten";
        String expected = "Østergaard, Morten";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_German() {
        String input = "Stra{\\ss}e";
        String expected = "Straße";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Cedilla Tests ==========

    @ParameterizedTest
    @CsvSource({
        "{\\c{c}}, ç",
        "{\\c{C}}, Ç",
        "{\\c{s}}, ş",
        "{\\c{S}}, Ş",
        "{\\c{t}}, ţ",
        "{\\c{T}}, Ţ"
    })
    void testCedilla(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_Cedilla() {
        String input = "Fran{\\c{c}}ois Truffaut";
        String expected = "François Truffaut";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Ring Above Tests ==========

    @ParameterizedTest
    @CsvSource({
        "{\\ua}, å",
        "{\\uA}, Å"
    })
    void testRingAbove(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testRealWorldName_RingAbove() {
        String input = "{\\uA}ngstr{\\\"o}m, Anders";
        String expected = "Ångström, Anders";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Double Acute Tests ==========

    @ParameterizedTest
    @CsvSource({
        "{\\Ho}, ő",
        "{\\HO}, Ő",
        "{\\Hu}, ű",
        "{\\HU}, Ű"
    })
    void testDoubleAcute(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Dot Above Tests ==========

    @ParameterizedTest
    @CsvSource({
        "{\\.z}, ż",
        "{\\.Z}, Ż",
        "{\\.c}, ċ",
        "{\\.C}, Ċ"
    })
    void testDotAbove(String input, String expected) {
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Unbraced Variants Tests ==========

    @Test
    void testUnbracedAcuteAccent() {
        String input = "\\'e";
        String expected = "é";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testUnbracedGraveAccent() {
        String input = "\\`a";
        String expected = "à";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testUnbracedUmlaut() {
        String input = "\\\"u";
        String expected = "ü";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Edge Cases ==========

    @Test
    void testNullInput() {
        assertNull(BibTeXUnicodeConverter.toUnicode(null));
    }

    @Test
    void testEmptyString() {
        assertEquals("", BibTeXUnicodeConverter.toUnicode(""));
    }

    @Test
    void testNoEscapeSequences() {
        String input = "Simple ASCII text";
        assertEquals(input, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testUnknownEscapeSequence() {
        String input = "{\\xyz}";
        // Should remain unchanged if not in map
        assertEquals(input, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Mixed Content Tests ==========

    @Test
    void testMultipleAccentsInOneString() {
        String input = "{\\'e}l{\\`e}ve fran{\\c{c}}ais";
        String expected = "élève français";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testMixedBracedAndUnbraced() {
        String input = "{\\'e}l\\`eve";
        String expected = "élève";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testComplexRealWorldExample() {
        String input = "M{\\\"u}ller, J{\\\"o}rg and Garc{\\'i}a-P{\\'e}rez, Mar{\\'i}a";
        String expected = "Müller, Jörg and García-Pérez, María";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Round-Trip Conversion Tests ==========

    @ParameterizedTest
    @CsvSource({
        "é, {\\'e}",
        "à, {\\`a}",
        "ü, {\\\"u}",
        "ô, {\\^o}",
        "ñ, {\\~n}",
        "ç, {\\c{c}}",
        "ø, {\\o}",
        "ß, {\\ss}"
    })
    void testRoundTripConversion(String unicode, String expectedBibtex) {
        String bibtex = BibTeXUnicodeConverter.fromUnicode(unicode);
        assertEquals(expectedBibtex, bibtex);

        // Verify reverse conversion
        String backToUnicode = BibTeXUnicodeConverter.toUnicode(bibtex);
        assertEquals(unicode, backToUnicode);
    }

    @Test
    void testRoundTripConversion_RingAbove() {
        // Note: a-ring can be represented as both {aa} and {u+a} in BibTeX
        // fromUnicode() prefers the ring above accent form
        String unicode = "\u00E5"; // a-ring
        String bibtex = BibTeXUnicodeConverter.fromUnicode(unicode);
        assertEquals("{\\ua}", bibtex);

        // Both forms should convert back to a-ring
        assertEquals(unicode, BibTeXUnicodeConverter.toUnicode("{\\ua}"));
        assertEquals(unicode, BibTeXUnicodeConverter.toUnicode("{\\aa}"));
    }

    @Test
    void testRoundTripComplexString() {
        String original = "José María García-Pérez";
        String bibtex = BibTeXUnicodeConverter.fromUnicode(original);
        String backToUnicode = BibTeXUnicodeConverter.toUnicode(bibtex);
        assertEquals(original, backToUnicode);
    }

    // ========== Real-World Academic Examples ==========

    @Test
    void testProfessorExample_PapersDB() {
        // From actual PapersDB.bib file
        String input = "Guasch-Ferr{\\'e}, Marta and Willett, Walter C";
        String expected = "Guasch-Ferré, Marta and Willett, Walter C";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testAcademicTitle_French() {
        String input = "L'{\\'e}tude des ph{\\'e}nom{\\`e}nes quantiques";
        String expected = "L'étude des phénomènes quantiques";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testAcademicTitle_German() {
        String input = "{\\\"U}ber die Entropie-Konzeption der quantitativen Linguistik";
        String expected = "Über die Entropie-Konzeption der quantitativen Linguistik";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testAcademicTitle_Spanish() {
        String input = "La educaci{\\'o}n en Am{\\'e}rica Latina: desaf{\\'i}os y oportunidades";
        String expected = "La educación en América Latina: desafíos y oportunidades";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testAcademicTitle_Polish() {
        String input = "Wp{\\l}yw technologii na rozw{\\'o}j spo{\\l}ecze\\'nstwa";
        String expected = "Wpływ technologii na rozwój społeczeństwa";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    @Test
    void testMultipleAuthorsInternational() {
        String input = "{\\O}stergaard, Morten and M{\\\"u}ller, Hans and Garc{\\'i}a, Jos{\\'e}";
        String expected = "Østergaard, Morten and Müller, Hans and García, José";
        assertEquals(expected, BibTeXUnicodeConverter.toUnicode(input));
    }

    // ========== Performance Test ==========

    @Test
    void testLargeTextPerformance() {
        // Build a string with 1000 escape sequences
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            input.append("Author{\\'e} ");
        }

        long start = System.currentTimeMillis();
        String result = BibTeXUnicodeConverter.toUnicode(input.toString());
        long duration = System.currentTimeMillis() - start;

        assertTrue(result.contains("Authoré"));
        assertTrue(duration < 1000, "Conversion should complete in less than 1 second");
    }
}
