package br.com.mechanic.account.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FullNameFormatter {

    private static final Set<String> LOWERCASE_PARTICLES = Set.of(
            "de",
            "da",
            "do",
            "das",
            "dos",
            "e"
    );

    public static String formatFromParts(String firstName, String lastName) {
        String combined = firstName.trim() + " " + lastName.trim();
        return formatTitleCase(combined);
    }

    private static String formatTitleCase(String fullName) {
        String[] words = fullName.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < words.length; index++) {
            String word = words[index];
            if (word.isEmpty()) {
                continue;
            }
            String lower = word.toLowerCase(Locale.ROOT);
            if (LOWERCASE_PARTICLES.contains(lower)) {
                result.append(lower);
            } else {
                result.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    result.append(lower.substring(1));
                }
            }
            if (index < words.length - 1) {
                result.append(' ');
            }
        }
        return result.toString();
    }
}
