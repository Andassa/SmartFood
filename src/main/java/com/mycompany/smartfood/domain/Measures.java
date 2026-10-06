package com.mycompany.smartfood.domain;

public final class Measures {

    public enum Dimension {
        MASS, VOLUME, COUNT
    }

    public record Amount(Dimension dimension, double base) {
    }

    private Measures() {
    }

    public static Amount toBase(double quantity, String unit) {
        String normalized = unit == null ? "pcs" : unit.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "g" -> new Amount(Dimension.MASS, quantity);
            case "kg" -> new Amount(Dimension.MASS, quantity * 1000);
            case "ml" -> new Amount(Dimension.VOLUME, quantity);
            case "l" -> new Amount(Dimension.VOLUME, quantity * 1000);
            case "tbsp" -> new Amount(Dimension.VOLUME, quantity * 15);
            case "tsp" -> new Amount(Dimension.VOLUME, quantity * 5);
            default -> new Amount(Dimension.COUNT, quantity);
        };
    }

    public static double fromBase(double base, String unit) {
        String normalized = unit == null ? "pcs" : unit.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "kg" -> base / 1000;
            case "l" -> base / 1000;
            case "tbsp" -> base / 15;
            case "tsp" -> base / 5;
            default -> base;
        };
    }

    public static String canonical(String unit) {
        if (unit == null || unit.isBlank()) {
            return "pcs";
        }
        return switch (unit.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "g", "kg", "ml", "l", "tbsp", "tsp", "pcs" -> unit.trim().toLowerCase(java.util.Locale.ROOT);
            default -> "pcs";
        };
    }
}
