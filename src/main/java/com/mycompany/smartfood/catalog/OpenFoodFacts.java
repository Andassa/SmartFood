package com.mycompany.smartfood.catalog;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.dto.ProductLookup;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public final class OpenFoodFacts {

    public record Per100g(double kcal, double protein) {
    }

    private static final Pattern QUANTITY = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*([a-zA-Z]+)");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private static final Map<String, Optional<Per100g>> NUTRIENTS = new ConcurrentHashMap<>();

    private OpenFoodFacts() {
    }

    public static ProductLookup lookup(String code) {
        if (code == null || !code.matches("\\d{8,14}")) {
            throw new IllegalArgumentException("Code-barres invalide");
        }
        try {
            return parse(code, body(code));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Catalogue produits indisponible");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Catalogue produits indisponible");
        }
    }

    public static Per100g per100g(String code) {
        if (code == null || !code.matches("\\d{8,14}")) {
            return null;
        }
        return NUTRIENTS.computeIfAbsent(code, OpenFoodFacts::loadNutrients).orElse(null);
    }

    private static Optional<Per100g> loadNutrients(String code) {
        try {
            return Optional.ofNullable(nutrients(body(code)));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static String body(String code) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://world.openfoodfacts.org/api/v2/product/" + code + ".json"))
                .timeout(Duration.ofSeconds(4))
                .header("User-Agent", "SmartFood/1.0 (local pantry)")
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("Produit introuvable");
        }
        return response.body();
    }

    private static Per100g nutrients(String body) {
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject root = reader.readObject();
            if (!root.containsKey("status") || root.getInt("status") != 1 || root.isNull("product")) {
                return null;
            }
            JsonObject product = root.getJsonObject("product");
            if (!product.containsKey("nutriments") || product.isNull("nutriments")) {
                return null;
            }
            JsonObject nutriments = product.getJsonObject("nutriments");
            return new Per100g(number(nutriments, "energy-kcal_100g"), number(nutriments, "proteins_100g"));
        }
    }

    private static double number(JsonObject object, String key) {
        if (!object.containsKey(key) || object.isNull(key)) {
            return 0;
        }
        return object.getJsonNumber(key).doubleValue();
    }

    private static ProductLookup parse(String code, String body) {
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonObject root = reader.readObject();
            int status = root.containsKey("status") ? root.getInt("status") : 0;
            if (status != 1 || root.isNull("product")) {
                throw new IllegalArgumentException("Produit introuvable");
            }
            JsonObject product = root.getJsonObject("product");
            String name = text(product, "product_name_fr");
            if (name.isBlank()) {
                name = text(product, "product_name");
            }
            if (name.isBlank()) {
                throw new IllegalArgumentException("Produit introuvable");
            }
            int quantity = 1;
            String unit = "pcs";
            Matcher matcher = QUANTITY.matcher(text(product, "quantity"));
            if (matcher.find()) {
                double value = Double.parseDouble(matcher.group(1).replace(',', '.'));
                unit = matcher.group(2).toLowerCase(java.util.Locale.ROOT);
                if ("cl".equals(unit)) {
                    value *= 10;
                    unit = "ml";
                } else if (!unit.matches("g|kg|ml|l")) {
                    unit = "pcs";
                }
                quantity = (int) Math.max(1, Math.round(value));
            }
            return new ProductLookup(code, Names.clean(name, 120), quantity, unit);
        }
    }

    private static String text(JsonObject object, String key) {
        return !object.containsKey(key) || object.isNull(key) ? "" : object.getString(key).trim();
    }
}
