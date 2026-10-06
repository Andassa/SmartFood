package com.mycompany.smartfood.domain;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class Names {

    private Names() {
    }

    public static String key(String value) {
        if (value == null) {
            return "";
        }
        String folded = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return folded.replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim();
    }

    public static boolean same(String left, String right) {
        if (key(left).equals(key(right))) {
            return true;
        }
        List<String> a = tokens(left);
        List<String> b = tokens(right);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        List<String> small = a.size() <= b.size() ? a : b;
        List<String> large = small == a ? b : a;
        if (!new HashSet<>(large).containsAll(small)) {
            return false;
        }
        for (String extra : large) {
            if (!small.contains(extra) && CONFLICT.contains(extra)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hits(String name, Set<String> keys) {
        for (String key : keys) {
            if (same(name, key)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> tokens(String value) {
        List<String> tokens = new ArrayList<>();
        for (String part : key(value).split(" ")) {
            String stem = part.endsWith("s") && part.length() > 3 ? part.substring(0, part.length() - 1) : part;
            if (stem.length() >= 3 && !STOP.contains(stem)) {
                tokens.add(stem);
            }
        }
        return tokens;
    }

    private static final Set<String> STOP = Set.of("de", "du", "des", "au", "aux", "et", "of", "the");
    private static final Set<String> CONFLICT = Set.of("coco", "amande", "soja", "avoine", "riz", "epeautre");

    public static String clean(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException("Texte trop long");
        }
        return trimmed;
    }
}
