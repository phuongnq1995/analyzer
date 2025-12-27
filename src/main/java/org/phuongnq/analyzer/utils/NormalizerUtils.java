package org.phuongnq.analyzer.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class NormalizerUtils {

    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return normalizeSpace(deAccent(name.trim().toLowerCase()));
    }

    public static String normalizeSpace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", " ").trim();
    }

    public static String deAccent(String str) {
        // 1. Normalize the string to the NFD form (decomposed)
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);

        // 2. Define a regex pattern to match combining diacritical marks (Unicode category "M")
        Pattern pattern = Pattern.compile("\\p{M}+");

        // 3. Replace the diacritical marks with an empty string
        String result = pattern.matcher(nfdNormalizedString).replaceAll("");

        // 4. Handle the specific Vietnamese character 'đ' and 'Đ' (which is not a diacritic)
        result = result.replace('đ', 'd').replace('Đ', 'D');

        return result;
    }
}
