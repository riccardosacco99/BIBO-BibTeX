package it.riccardosacco.bibobibtex.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts BibTeX LaTeX escape sequences to Unicode characters.
 * Handles diacritical marks, special characters, and various LaTeX notations
 * commonly used in bibliographic data.
 */
public final class BibTeXUnicodeConverter {

    private static final Map<String, String> ESCAPE_SEQUENCES = new HashMap<>();
    private static final Map<String, String> REVERSE_SEQUENCES = new HashMap<>();

    static {
        // Acute accent (')
        ESCAPE_SEQUENCES.put("{\\'a}", "á");
        ESCAPE_SEQUENCES.put("{\\'A}", "Á");
        ESCAPE_SEQUENCES.put("{\\'e}", "é");
        ESCAPE_SEQUENCES.put("{\\'E}", "É");
        ESCAPE_SEQUENCES.put("{\\'i}", "í");
        ESCAPE_SEQUENCES.put("{\\'I}", "Í");
        ESCAPE_SEQUENCES.put("{\\'o}", "ó");
        ESCAPE_SEQUENCES.put("{\\'O}", "Ó");
        ESCAPE_SEQUENCES.put("{\\'u}", "ú");
        ESCAPE_SEQUENCES.put("{\\'U}", "Ú");
        ESCAPE_SEQUENCES.put("{\\'y}", "ý");
        ESCAPE_SEQUENCES.put("{\\'Y}", "Ý");
        ESCAPE_SEQUENCES.put("{\\'c}", "ć");
        ESCAPE_SEQUENCES.put("{\\'C}", "Ć");
        ESCAPE_SEQUENCES.put("{\\'n}", "ń");
        ESCAPE_SEQUENCES.put("{\\'N}", "Ń");
        ESCAPE_SEQUENCES.put("{\\'s}", "ś");
        ESCAPE_SEQUENCES.put("{\\'S}", "Ś");
        ESCAPE_SEQUENCES.put("{\\'z}", "ź");
        ESCAPE_SEQUENCES.put("{\\'Z}", "Ź");

        // Grave accent (`)
        ESCAPE_SEQUENCES.put("{\\`a}", "à");
        ESCAPE_SEQUENCES.put("{\\`A}", "À");
        ESCAPE_SEQUENCES.put("{\\`e}", "è");
        ESCAPE_SEQUENCES.put("{\\`E}", "È");
        ESCAPE_SEQUENCES.put("{\\`i}", "ì");
        ESCAPE_SEQUENCES.put("{\\`I}", "Ì");
        ESCAPE_SEQUENCES.put("{\\`o}", "ò");
        ESCAPE_SEQUENCES.put("{\\`O}", "Ò");
        ESCAPE_SEQUENCES.put("{\\`u}", "ù");
        ESCAPE_SEQUENCES.put("{\\`U}", "Ù");

        // Circumflex (^)
        ESCAPE_SEQUENCES.put("{\\^a}", "â");
        ESCAPE_SEQUENCES.put("{\\^A}", "Â");
        ESCAPE_SEQUENCES.put("{\\^e}", "ê");
        ESCAPE_SEQUENCES.put("{\\^E}", "Ê");
        ESCAPE_SEQUENCES.put("{\\^i}", "î");
        ESCAPE_SEQUENCES.put("{\\^I}", "Î");
        ESCAPE_SEQUENCES.put("{\\^o}", "ô");
        ESCAPE_SEQUENCES.put("{\\^O}", "Ô");
        ESCAPE_SEQUENCES.put("{\\^u}", "û");
        ESCAPE_SEQUENCES.put("{\\^U}", "Û");

        // Tilde (~)
        ESCAPE_SEQUENCES.put("{\\~a}", "ã");
        ESCAPE_SEQUENCES.put("{\\~A}", "Ã");
        ESCAPE_SEQUENCES.put("{\\~n}", "ñ");
        ESCAPE_SEQUENCES.put("{\\~N}", "Ñ");
        ESCAPE_SEQUENCES.put("{\\~o}", "õ");
        ESCAPE_SEQUENCES.put("{\\~O}", "Õ");

        // Diaeresis/Umlaut (")
        ESCAPE_SEQUENCES.put("{\\\"a}", "ä");
        ESCAPE_SEQUENCES.put("{\\\"A}", "Ä");
        ESCAPE_SEQUENCES.put("{\\\"e}", "ë");
        ESCAPE_SEQUENCES.put("{\\\"E}", "Ë");
        ESCAPE_SEQUENCES.put("{\\\"i}", "ï");
        ESCAPE_SEQUENCES.put("{\\\"I}", "Ï");
        ESCAPE_SEQUENCES.put("{\\\"o}", "ö");
        ESCAPE_SEQUENCES.put("{\\\"O}", "Ö");
        ESCAPE_SEQUENCES.put("{\\\"u}", "ü");
        ESCAPE_SEQUENCES.put("{\\\"U}", "Ü");
        ESCAPE_SEQUENCES.put("{\\\"y}", "ÿ");
        ESCAPE_SEQUENCES.put("{\\\"Y}", "Ÿ");

        // Macron (=)
        ESCAPE_SEQUENCES.put("{\\=a}", "ā");
        ESCAPE_SEQUENCES.put("{\\=A}", "Ā");
        ESCAPE_SEQUENCES.put("{\\=e}", "ē");
        ESCAPE_SEQUENCES.put("{\\=E}", "Ē");
        ESCAPE_SEQUENCES.put("{\\=i}", "ī");
        ESCAPE_SEQUENCES.put("{\\=I}", "Ī");
        ESCAPE_SEQUENCES.put("{\\=o}", "ō");
        ESCAPE_SEQUENCES.put("{\\=O}", "Ō");
        ESCAPE_SEQUENCES.put("{\\=u}", "ū");
        ESCAPE_SEQUENCES.put("{\\=U}", "Ū");

        // Breve (u) / Ring (for a/A, u means ring in BibTeX)
        ESCAPE_SEQUENCES.put("{\\ua}", "å");
        ESCAPE_SEQUENCES.put("{\\uA}", "Å");
        ESCAPE_SEQUENCES.put("{\\ue}", "ĕ");
        ESCAPE_SEQUENCES.put("{\\uE}", "Ĕ");
        ESCAPE_SEQUENCES.put("{\\ug}", "ğ");
        ESCAPE_SEQUENCES.put("{\\uG}", "Ğ");
        ESCAPE_SEQUENCES.put("{\\ui}", "ĭ");
        ESCAPE_SEQUENCES.put("{\\uI}", "Ĭ");
        ESCAPE_SEQUENCES.put("{\\uo}", "ŏ");
        ESCAPE_SEQUENCES.put("{\\uO}", "Ŏ");
        ESCAPE_SEQUENCES.put("{\\uu}", "ŭ");
        ESCAPE_SEQUENCES.put("{\\uU}", "Ŭ");

        // Caron/Hacek (v)
        ESCAPE_SEQUENCES.put("{\\vc}", "č");
        ESCAPE_SEQUENCES.put("{\\vC}", "Č");
        ESCAPE_SEQUENCES.put("{\\vd}", "ď");
        ESCAPE_SEQUENCES.put("{\\vD}", "Ď");
        ESCAPE_SEQUENCES.put("{\\ve}", "ě");
        ESCAPE_SEQUENCES.put("{\\vE}", "Ě");
        ESCAPE_SEQUENCES.put("{\\vn}", "ň");
        ESCAPE_SEQUENCES.put("{\\vN}", "Ň");
        ESCAPE_SEQUENCES.put("{\\vr}", "ř");
        ESCAPE_SEQUENCES.put("{\\vR}", "Ř");
        ESCAPE_SEQUENCES.put("{\\vs}", "š");
        ESCAPE_SEQUENCES.put("{\\vS}", "Š");
        ESCAPE_SEQUENCES.put("{\\vt}", "ť");
        ESCAPE_SEQUENCES.put("{\\vT}", "Ť");
        ESCAPE_SEQUENCES.put("{\\vz}", "ž");
        ESCAPE_SEQUENCES.put("{\\vZ}", "Ž");

        // Dot above (.)
        ESCAPE_SEQUENCES.put("{\\.c}", "ċ");
        ESCAPE_SEQUENCES.put("{\\.C}", "Ċ");
        ESCAPE_SEQUENCES.put("{\\.e}", "ė");
        ESCAPE_SEQUENCES.put("{\\.E}", "Ė");
        ESCAPE_SEQUENCES.put("{\\.g}", "ġ");
        ESCAPE_SEQUENCES.put("{\\.G}", "Ġ");
        ESCAPE_SEQUENCES.put("{\\.I}", "İ");
        ESCAPE_SEQUENCES.put("{\\.z}", "ż");
        ESCAPE_SEQUENCES.put("{\\.Z}", "Ż");

        // Ring above (r) / Angstrom
        ESCAPE_SEQUENCES.put("{\\ra}", "å");
        ESCAPE_SEQUENCES.put("{\\rA}", "Å");
        ESCAPE_SEQUENCES.put("{\\ru}", "ů");
        ESCAPE_SEQUENCES.put("{\\rU}", "Ů");

        // Double acute (H)
        ESCAPE_SEQUENCES.put("{\\Ho}", "ő");
        ESCAPE_SEQUENCES.put("{\\HO}", "Ő");
        ESCAPE_SEQUENCES.put("{\\Hu}", "ű");
        ESCAPE_SEQUENCES.put("{\\HU}", "Ű");

        // Ogonek (k)
        ESCAPE_SEQUENCES.put("{\\ka}", "ą");
        ESCAPE_SEQUENCES.put("{\\kA}", "Ą");
        ESCAPE_SEQUENCES.put("{\\ke}", "ę");
        ESCAPE_SEQUENCES.put("{\\kE}", "Ę");
        ESCAPE_SEQUENCES.put("{\\ki}", "į");
        ESCAPE_SEQUENCES.put("{\\kI}", "Į");
        ESCAPE_SEQUENCES.put("{\\ku}", "ų");
        ESCAPE_SEQUENCES.put("{\\kU}", "Ų");

        // Cedilla (c with nested braces)
        ESCAPE_SEQUENCES.put("{\\c{c}}", "ç");
        ESCAPE_SEQUENCES.put("{\\c{C}}", "Ç");
        ESCAPE_SEQUENCES.put("{\\c{s}}", "ş");
        ESCAPE_SEQUENCES.put("{\\c{S}}", "Ş");
        ESCAPE_SEQUENCES.put("{\\c{t}}", "ţ");
        ESCAPE_SEQUENCES.put("{\\c{T}}", "Ţ");

        // Special standalone characters
        ESCAPE_SEQUENCES.put("{\\aa}", "å");
        ESCAPE_SEQUENCES.put("{\\AA}", "Å");
        ESCAPE_SEQUENCES.put("{\\ae}", "æ");
        ESCAPE_SEQUENCES.put("{\\AE}", "Æ");
        ESCAPE_SEQUENCES.put("{\\o}", "ø");
        ESCAPE_SEQUENCES.put("{\\O}", "Ø");
        ESCAPE_SEQUENCES.put("{\\l}", "ł");
        ESCAPE_SEQUENCES.put("{\\L}", "Ł");
        ESCAPE_SEQUENCES.put("{\\ss}", "ß");
        ESCAPE_SEQUENCES.put("{\\oe}", "œ");
        ESCAPE_SEQUENCES.put("{\\OE}", "Œ");
        ESCAPE_SEQUENCES.put("{\\i}", "ı");
        ESCAPE_SEQUENCES.put("{\\dj}", "đ");
        ESCAPE_SEQUENCES.put("{\\DJ}", "Đ");
        ESCAPE_SEQUENCES.put("{\\ng}", "ŋ");
        ESCAPE_SEQUENCES.put("{\\NG}", "Ŋ");
        ESCAPE_SEQUENCES.put("{\\th}", "þ");
        ESCAPE_SEQUENCES.put("{\\TH}", "Þ");
        ESCAPE_SEQUENCES.put("{\\dh}", "ð");
        ESCAPE_SEQUENCES.put("{\\DH}", "Ð");

        // Build reverse mapping for fromUnicode conversion
        // Note: For characters with multiple LaTeX representations (like å),
        // we prefer the first occurrence in our mappings
        for (Map.Entry<String, String> entry : ESCAPE_SEQUENCES.entrySet()) {
            String latex = entry.getKey();
            String unicode = entry.getValue();

            // Only add if not already present (first occurrence wins)
            if (!REVERSE_SEQUENCES.containsKey(unicode)) {
                REVERSE_SEQUENCES.put(unicode, latex);
            }
        }
    }

    private BibTeXUnicodeConverter() {
        // Utility class
    }

    /**
     * Converts a string containing BibTeX LaTeX escape sequences to Unicode.
     * Handles both braced forms (e.g., {\\'e}) and unbraced forms (e.g., \\'e).
     *
     * @param input the input string with LaTeX escape sequences
     * @return the string with escape sequences converted to Unicode characters
     */
    public static String toUnicode(String input) {
        if (input == null) {
            return null;
        }

        String result = input;

        // First, replace all braced escape sequences
        for (Map.Entry<String, String> entry : ESCAPE_SEQUENCES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // Then handle unbraced forms (remove the braces from the pattern)
        // We need to be careful with the order and pattern matching
        // Common unbraced patterns: \'e, \`a, \"u, \^o, \~n, etc.
        result = convertUnbracedAccents(result);

        return result;
    }

    /**
     * Converts unbraced LaTeX accent commands to Unicode.
     * Handles patterns like \'e, \`a, \"u, etc.
     */
    private static String convertUnbracedAccents(String input) {
        String result = input;

        // Acute accent: \'
        result = replaceUnbracedPattern(result, "\\\\'([a-zA-Z])", "'");

        // Grave accent: \`
        result = replaceUnbracedPattern(result, "\\\\`([a-zA-Z])", "`");

        // Umlaut/diaeresis: \"
        result = replaceUnbracedPattern(result, "\\\\\"([a-zA-Z])", "\"");

        // Circumflex: \^
        result = replaceUnbracedPattern(result, "\\\\\\^([a-zA-Z])", "^");

        // Tilde: \~
        result = replaceUnbracedPattern(result, "\\\\~([a-zA-Z])", "~");

        // Macron: \=
        result = replaceUnbracedPattern(result, "\\\\=([a-zA-Z])", "=");

        // Breve/Ring: backslash-u
        result = replaceUnbracedPattern(result, "\\\\u([a-zA-Z])", "u");

        // Caron: backslash-v
        result = replaceUnbracedPattern(result, "\\\\v([a-zA-Z])", "v");

        // Dot: \.
        result = replaceUnbracedPattern(result, "\\\\.([a-zA-Z])", ".");

        // Ring: \r
        result = replaceUnbracedPattern(result, "\\\\r([a-zA-Z])", "r");

        // Double acute: \H
        result = replaceUnbracedPattern(result, "\\\\H([a-zA-Z])", "H");

        // Ogonek: \k
        result = replaceUnbracedPattern(result, "\\\\k([a-zA-Z])", "k");

        return result;
    }

    /**
     * Replaces unbraced accent patterns with Unicode characters.
     */
    private static String replaceUnbracedPattern(String input, String regex, String accent) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String letter = matcher.group(1);
            String bracedForm = "{\\" + accent + letter + "}";
            String unicode = ESCAPE_SEQUENCES.get(bracedForm);

            if (unicode != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(unicode));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Converts a string containing Unicode characters to BibTeX LaTeX escape sequences.
     * This is the reverse operation of toUnicode().
     *
     * @param input the input string with Unicode characters
     * @return the string with Unicode characters converted to LaTeX escape sequences
     */
    public static String fromUnicode(String input) {
        if (input == null) {
            return null;
        }

        String result = input;

        // Replace all known Unicode characters with their LaTeX equivalents
        for (Map.Entry<String, String> entry : REVERSE_SEQUENCES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
