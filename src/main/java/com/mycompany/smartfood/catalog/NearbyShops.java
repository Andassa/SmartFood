package com.mycompany.smartfood.catalog;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mycompany.smartfood.domain.Names;
import com.mycompany.smartfood.dto.ShopRoute;
import com.mycompany.smartfood.dto.ShopStop;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

public final class NearbyShops {

    private enum Shelf { PRODUCE, DAIRY, MEAT, BREAD, GROCERY }

    private record Place(String name, String osm, String address, double latitude, double longitude, int meters) {
    }

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private static final Set<String> PRODUCE = Set.of("tomate", "salade", "pomme", "banane", "carotte", "oignon", "fruit", "legume", "courgette", "pomme de terre", "herbe", "persil", "citron");
    private static final Set<String> DAIRY = Set.of("lait", "yaourt", "fromage", "beurre", "creme", "milk", "yogurt", "cheese", "butter");
    private static final Set<String> MEAT = Set.of("viande", "poulet", "boeuf", "poisson", "jambon", "steak", "chicken", "beef", "fish");
    private static final Set<String> BREAD = Set.of("pain", "baguette", "brioche", "bread");

    private NearbyShops() {
    }

    public static ShopRoute plan(double latitude, double longitude, List<String> basket, String expiring) {
        ShopRoute route = new ShopRoute();
        if (basket.isEmpty()) {
            route.setAdvice("La liste de courses est vide.");
            return route;
        }
        List<Place> places = fetch(latitude, longitude, 4000);
        if (places.size() < 2) {
            places = fetch(latitude, longitude, 12000);
        }
        if (places.isEmpty()) {
            route.setAdvice("Aucun magasin trouvé dans les 12 km.");
            return route;
        }
        Map<Place, ShopStop> chosen = new LinkedHashMap<>();
        for (String item : basket) {
            Place place = pick(places, shelf(item));
            ShopStop stop = chosen.computeIfAbsent(place, NearbyShops::stop);
            stop.getItems().add(item);
        }
        List<ShopStop> stops = new ArrayList<>(chosen.values());
        stops.sort(Comparator.comparingInt(ShopStop::getMeters));
        route.setStops(stops);
        route.setAdvice(advice(stops, expiring));
        return route;
    }

    public static ShopRoute guide(List<String> basket) {
        ShopRoute route = new ShopRoute();
        if (basket.isEmpty()) {
            route.setAdvice("Ajoute des produits pour savoir où les chercher.");
            return route;
        }
        Map<String, ShopStop> groups = new LinkedHashMap<>();
        for (String item : basket) {
            String kind = recommended(shelf(item));
            ShopStop stop = groups.computeIfAbsent(kind, NearbyShops::kindStop);
            stop.getItems().add(item);
        }
        route.setStops(new ArrayList<>(groups.values()));
        route.setAdvice("Chaque produit est orienté vers le type de magasin qui le vend vraiment.");
        return route;
    }

    private static ShopStop kindStop(String kind) {
        ShopStop stop = new ShopStop();
        stop.setName(kind);
        stop.setKind(kind);
        return stop;
    }

    private static String recommended(Shelf shelf) {
        return switch (shelf) {
            case PRODUCE -> "Marché ou primeur";
            case DAIRY -> "Crémerie ou supermarché";
            case MEAT -> "Boucher ou supermarché";
            case BREAD -> "Boulangerie";
            case GROCERY -> "Supermarché";
        };
    }

    private static Place pick(List<Place> places, Shelf shelf) {
        Place best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Place place : places) {
            if (!sells(place.osm, shelf)) {
                continue;
            }
            int score = place.meters - (specialist(place.osm, shelf) ? 600 : 0);
            if (score < bestScore) {
                best = place;
                bestScore = score;
            }
        }
        if (best != null) {
            return best;
        }
        return places.stream().min(Comparator.comparingInt(Place::meters)).orElseThrow();
    }

    private static ShopStop stop(Place place) {
        ShopStop stop = new ShopStop();
        stop.setName(place.name);
        stop.setKind(label(place.osm));
        stop.setMeters(place.meters);
        stop.setAddress(place.address);
        stop.setLatitude(place.latitude);
        stop.setLongitude(place.longitude);
        return stop;
    }

    private static String advice(List<ShopStop> stops, String expiring) {
        StringBuilder text = new StringBuilder();
        for (ShopStop stop : stops) {
            String distance = stop.getMeters() < 1000 ? stop.getMeters() + " m" : String.format(Locale.FRANCE, "%.1f km", stop.getMeters() / 1000d);
            text.append(stop.getName()).append(" (").append(stop.getKind()).append(", ").append(distance).append(") : ")
                    .append(String.join(", ", stop.getItems())).append(". ");
        }
        if (expiring != null && !expiring.isBlank()) {
            text.append("Cuisine d'abord ").append(expiring).append(", il périme dans la semaine.");
        }
        return text.toString().trim();
    }

    private static List<Place> fetch(double latitude, double longitude, int radius) {
        String query = String.format(Locale.US, """
                [out:json][timeout:12];
                (
                  node["shop"~"^(supermarket|grocery|convenience|greengrocer|butcher|bakery|dairy|cheese|farm)$"](around:%d,%f,%f);
                  way["shop"~"^(supermarket|grocery|convenience|greengrocer|butcher|bakery|dairy|cheese|farm)$"](around:%d,%f,%f);
                  node["amenity"="marketplace"](around:%d,%f,%f);
                  way["amenity"="marketplace"](around:%d,%f,%f);
                );
                out center tags 40;
                """, radius, latitude, longitude, radius, latitude, longitude, radius, latitude, longitude, radius, latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://overpass-api.de/api/interpreter"))
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "SmartFood/1.0 (local pantry)")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8)))
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Magasins indisponibles");
            }
            return parse(latitude, longitude, response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Magasins indisponibles");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Magasins indisponibles");
        }
    }

    private static List<Place> parse(double latitude, double longitude, String body) {
        List<Place> places = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            JsonArray elements = reader.readObject().getJsonArray("elements");
            for (JsonValue value : elements) {
                JsonObject element = value.asJsonObject();
                if (!element.containsKey("tags") || element.isNull("tags")) {
                    continue;
                }
                double lat = coordinate(element, "lat");
                double lon = coordinate(element, "lon");
                if (Double.isNaN(lat)) {
                    continue;
                }
                JsonObject tags = element.getJsonObject("tags");
                String osm = tags.containsKey("amenity") && "marketplace".equals(text(tags, "amenity"))
                        ? "marketplace"
                        : text(tags, "shop");
                if (osm.isBlank()) {
                    continue;
                }
                String name = text(tags, "name");
                if (name.isBlank()) {
                    name = label(osm);
                }
                String street = (text(tags, "addr:housenumber") + " " + text(tags, "addr:street")).trim();
                places.add(new Place(name, osm, street, lat, lon, meters(latitude, longitude, lat, lon)));
            }
        }
        places.sort(Comparator.comparingInt(Place::meters));
        return places;
    }

    private static double coordinate(JsonObject element, String key) {
        if (element.containsKey(key) && !element.isNull(key)) {
            return element.getJsonNumber(key).doubleValue();
        }
        if (!element.containsKey("center") || element.isNull("center")) {
            return Double.NaN;
        }
        JsonObject center = element.getJsonObject("center");
        return center.containsKey(key) && !center.isNull(key) ? center.getJsonNumber(key).doubleValue() : Double.NaN;
    }

    private static Shelf shelf(String name) {
        String key = Names.key(name);
        if (contains(PRODUCE, key)) {
            return Shelf.PRODUCE;
        }
        if (contains(DAIRY, key)) {
            return Shelf.DAIRY;
        }
        if (contains(MEAT, key)) {
            return Shelf.MEAT;
        }
        if (contains(BREAD, key)) {
            return Shelf.BREAD;
        }
        return Shelf.GROCERY;
    }

    private static boolean contains(Set<String> words, String key) {
        for (String word : words) {
        if (key.equals(word) || key.startsWith(word + " ") || key.endsWith(" " + word) || key.contains(" " + word + " ")) {
            return true;
        }
        }
        return false;
    }

    private static boolean sells(String osm, Shelf shelf) {
        return switch (osm) {
            case "supermarket", "grocery" -> true;
            case "convenience" -> shelf == Shelf.DAIRY || shelf == Shelf.BREAD || shelf == Shelf.GROCERY;
            case "greengrocer", "farm", "marketplace" -> shelf == Shelf.PRODUCE;
            case "butcher" -> shelf == Shelf.MEAT;
            case "bakery" -> shelf == Shelf.BREAD;
            case "dairy", "cheese" -> shelf == Shelf.DAIRY;
            default -> false;
        };
    }

    private static boolean specialist(String osm, Shelf shelf) {
        return switch (shelf) {
            case PRODUCE -> osm.equals("greengrocer") || osm.equals("farm") || osm.equals("marketplace");
            case MEAT -> osm.equals("butcher");
            case BREAD -> osm.equals("bakery");
            case DAIRY -> osm.equals("dairy") || osm.equals("cheese");
            case GROCERY -> false;
        };
    }

    private static String label(String osm) {
        return switch (osm) {
            case "supermarket" -> "Supermarché";
            case "grocery" -> "Épicerie";
            case "convenience" -> "Supérette";
            case "greengrocer", "farm" -> "Primeur";
            case "marketplace" -> "Marché";
            case "butcher" -> "Boucher";
            case "bakery" -> "Boulangerie";
            case "dairy", "cheese" -> "Crèmerie";
            default -> "Magasin";
        };
    }

    private static int meters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return (int) Math.round(6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    private static String text(JsonObject object, String key) {
        return !object.containsKey(key) || object.isNull(key) ? "" : object.getString(key).trim();
    }
}
