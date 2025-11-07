package it.riccardosacco.bibobibtex.converter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts BibTeX LaTeX escape sequences to Unicode characters.
 *
 * <p>BibTeX uses LaTeX-style escape sequences for special characters, such as:
 * <ul>
 *   <li>{\'e} → é (acute accent)</li>
 *   <li>{\`a} → à (grave accent)</li>
 *   <li>{\"u} → ü (umlaut/diaeresis)</li>
 *   <li>{\^o} → ô (circumflex)</li>
 *   <li>{\~n} → ñ (tilde)</li>
 * </ul>
 *
 * <p>This class converts these sequences to their Unicode equivalents for proper
 * display and storage in RDF.
 */
public final class BibTeXUnicodeConverter {

    // Accent patterns: {\' char}, {\` char}, {\" char}, etc.
    private static final Pattern ACCENT_PATTERN = Pattern.compile("\\{\\\\([`'^\"~=.uvHtcdb])\\s*([a-zA-Z])\\}");

    // Special braced patterns: {\'e}, {\aa}, {\o}, {\ss}, etc.
    private static final Pattern SPECIAL_BRACED_PATTERN = Pattern.compile("\\{\\\\([a-zA-Z]+)\\}");

    // Combining diacritics patterns: {\c{c}}, {\"o}, etc.
    private static final Pattern COMBINING_PATTERN = Pattern.compile("\\{\\\\c\\{([a-zA-Z])\\}\\}");

    // Unbraced variants: \'e, \`a, etc. (less common but valid)
    private static final Pattern UNBRACED_ACCENT_PATTERN = Pattern.compile("\\\\([`'^\"~=.uvHtcdb])([a-zA-Z])");

    // Map of accent + base character to Unicode
    private static final Map<String, String> ACCENT_MAP = Map.ofEntries(
            // Acute accent \'
            Map.entry("'a", "á"), Map.entry("'A", "Á"),
            Map.entry("'e", "é"), Map.entry("'E", "É"),
            Map.entry("'i", "í"), Map.entry("'I", "Í"),
            Map.entry("'o", "ó"), Map.entry("'O", "Ó"),
            Map.entry("'u", "ú"), Map.entry("'U", "Ú"),
            Map.entry("'y", "ý"), Map.entry("'Y", "Ý"),
            Map.entry("'c", "ć"), Map.entry("'C", "Ć"),
            Map.entry("'n", "ń"), Map.entry("'N", "Ń"),
            Map.entry("'s", "ś"), Map.entry("'S", "Ś"),
            Map.entry("'z", "ź"), Map.entry("'Z", "Ź"),

            // Grave accent \`
            Map.entry("`a", "à"), Map.entry("`A", "À"),
            Map.entry("`e", "è"), Map.entry("`E", "È"),
            Map.entry("`i", "ì"), Map.entry("`I", "Ì"),
            Map.entry("`o", "ò"), Map.entry("`O", "Ò"),
            Map.entry("`u", "ù"), Map.entry("`U", "Ù"),

            // Umlaut/diaeresis \"
            Map.entry("\"a", "ä"), Map.entry("\"A", "Ä"),
            Map.entry("\"e", "ë"), Map.entry("\"E", "Ë"),
            Map.entry("\"i", "ï"), Map.entry("\"I", "Ï"),
            Map.entry("\"o", "ö"), Map.entry("\"O", "Ö"),
            Map.entry("\"u", "ü"), Map.entry("\"U", "Ü"),
            Map.entry("\"y", "ÿ"), Map.entry("\"Y", "Ÿ"),

            // Circumflex \^
            Map.entry("^a", "â"), Map.entry("^A", "Â"),
            Map.entry("^e", "ê"), Map.entry("^E", "Ê"),
            Map.entry("^i", "î"), Map.entry("^I", "Î"),
            Map.entry("^o", "ô"), Map.entry("^O", "Ô"),
            Map.entry("^u", "û"), Map.entry("^U", "Û"),

            // Tilde \~
            Map.entry("~a", "ã"), Map.entry("~A", "Ã"),
            Map.entry("~n", "ñ"), Map.entry("~N", "Ñ"),
            Map.entry("~o", "õ"), Map.entry("~O", "Õ"),

            // Macron \=
            Map.entry("=a", "ā"), Map.entry("=A", "Ā"),
            Map.entry("=e", "ē"), Map.entry("=E", "Ē"),
            Map.entry("=i", "ī"), Map.entry("=I", "Ī"),
            Map.entry("=o", "ō"), Map.entry("=O", "Ō"),
            Map.entry("=u", "ū"), Map.entry("=U", "Ū"),

            // Dot above \.
            Map.entry(".z", "ż"), Map.entry(".Z", "Ż"),
            Map.entry(".c", "ċ"), Map.entry(".C", "Ċ"),

            // Ring above (LaTeX: \\u or \\r)
            Map.entry("ua", "å"), Map.entry("uA", "Å"),

            // Caron (LaTeX: \\v)
            Map.entry("vc", "č"), Map.entry("vC", "Č"),
            Map.entry("vs", "š"), Map.entry("vS", "Š"),
            Map.entry("vz", "ž"), Map.entry("vZ", "Ž"),

            // Double acute (LaTeX: \\H)
            Map.entry("Ho", "ő"), Map.entry("HO", "Ő"),
            Map.entry("Hu", "ű"), Map.entry("HU", "Ű"),

            // Tie (LaTeX: \\t)
            Map.entry("too", "o͡o"),

            // Stroke (LaTeX: \\b)
            Map.entry("bo", "o̱")
    );

    // Special single characters
    private static final Map<String, String> SPECIAL_CHAR_MAP = Map.ofEntries(
            Map.entry("aa", "å"), Map.entry("AA", "Å"),
            Map.entry("ae", "æ"), Map.entry("AE", "Æ"),
            Map.entry("o", "ø"), Map.entry("O", "Ø"),
            Map.entry("l", "ł"), Map.entry("L", "Ł"),
            Map.entry("ss", "ß"),
            Map.entry("oe", "œ"), Map.entry("OE", "Œ"),
            Map.entry("i", "ı"), // dotless i
            Map.entry("j", "ȷ")  // dotless j
    );

    // Cedilla \c{char}
    private static final Map<String, String> CEDILLA_MAP = Map.ofEntries(
            Map.entry("c", "ç"), Map.entry("C", "Ç"),
            Map.entry("s", "ş"), Map.entry("S", "Ş"),
            Map.entry("t", "ţ"), Map.entry("T", "Ţ")
    );

    private BibTeXUnicodeConverter() {
        // Utility class
    }

    /**
     * Converts BibTeX LaTeX escape sequences to Unicode characters.
     *
     * @param text the text containing BibTeX escape sequences
     * @return the text with escape sequences converted to Unicode
     */
    public static String toUnicode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // Convert combining diacritics like {\c{c}} → ç
        result = convertCombining(result);

        // Convert braced accents like {\'e} → é
        result = convertBracedAccents(result);

        // Convert unbraced accents like \'e → é
        result = convertUnbracedAccents(result);

        // Convert special characters like {\aa} → å
        result = convertSpecialChars(result);

        return result;
    }

    private static String convertCombining(String text) {
        Matcher matcher = COMBINING_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String baseChar = matcher.group(1);
            String replacement = CEDILLA_MAP.getOrDefault(baseChar, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertBracedAccents(String text) {
        Matcher matcher = ACCENT_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String accent = matcher.group(1);
            String baseChar = matcher.group(2);
            String key = accent + baseChar;
            String replacement = ACCENT_MAP.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertUnbracedAccents(String text) {
        Matcher matcher = UNBRACED_ACCENT_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String accent = matcher.group(1);
            String baseChar = matcher.group(2);
            String key = accent + baseChar;
            String replacement = ACCENT_MAP.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertSpecialChars(String text) {
        Matcher matcher = SPECIAL_BRACED_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String charName = matcher.group(1);
            String replacement = SPECIAL_CHAR_MAP.getOrDefault(charName, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Converts Unicode characters back to BibTeX LaTeX escape sequences.
     * This is the reverse operation of {@link #toUnicode(String)}.
     *
     * @param text the text containing Unicode characters
     * @return the text with Unicode characters converted to BibTeX escapes
     */
    public static String fromUnicode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // Reverse mapping: Unicode → BibTeX escape
        for (Map.Entry<String, String> entry : ACCENT_MAP.entrySet()) {
            String unicode = entry.getValue();
            String accent = entry.getKey().substring(0, 1);
            String baseChar = entry.getKey().substring(1);
            String escape = "{\\" + accent + baseChar + "}";
            result = result.replace(unicode, escape);
        }

        for (Map.Entry<String, String> entry : SPECIAL_CHAR_MAP.entrySet()) {
            String unicode = entry.getValue();
            String escape = "{\\" + entry.getKey() + "}";
            result = result.replace(unicode, escape);
        }

        for (Map.Entry<String, String> entry : CEDILLA_MAP.entrySet()) {
            String unicode = entry.getValue();
            String escape = "{\\c{" + entry.getKey() + "}}";
            result = result.replace(unicode, escape);
        }

        return result;
    }
}
