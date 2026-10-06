package com.mycompany.smartfood.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.mycompany.smartfood.domain.Measures.Amount;

public final class Substitutions {

    public record Route(String note, String stockKey, double ratio) {
    }

    private record Edge(String to, double ratio) {
    }

    private record Step(String node, double ratio, String note, int hops) {
    }

    private static final Map<String, List<Edge>> GRAPH = new HashMap<>();

    static {
        link("lait", "yaourt", 1);
        link("milk", "yogurt", 1);
        link("beurre", "huile", 0.8);
        link("butter", "oil", 0.8);
        link("creme", "lait", 1);
        link("cream", "milk", 1);
        link("sucre", "miel", 0.75);
        link("sugar", "honey", 0.75);
        link("yaourt", "fromage blanc", 1.2);
        link("yogurt", "fromage blanc", 1.2);
        both("lait", "milk");
        both("yaourt", "yogurt");
        both("beurre", "butter");
        both("huile", "oil");
        both("creme", "cream");
        both("sucre", "sugar");
        both("miel", "honey");
    }

    private Substitutions() {
    }

    public static Route route(String name, Pantry pantry, Amount need) {
        String start = Names.key(name);
        Queue<Step> queue = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        queue.add(new Step(start, 1, name, 0));
        seen.add(start);
        while (!queue.isEmpty()) {
            Step step = queue.remove();
            if (step.hops > 0 && step.hops <= 3 && fits(pantry, step.node, need, step.ratio)) {
                return new Route(step.note, pantry.resolve(step.node, need.dimension()), step.ratio);
            }
            if (step.hops >= 3) {
                continue;
            }
            for (Edge edge : GRAPH.getOrDefault(step.node, List.of())) {
                if (seen.add(edge.to)) {
                    queue.add(new Step(edge.to, step.ratio * edge.ratio, step.note + " → " + edge.to, step.hops + 1));
                }
            }
        }
        return null;
    }

    private static boolean fits(Pantry pantry, String node, Amount need, double ratio) {
        String stock = pantry.resolve(node, need.dimension());
        return stock != null && pantry.available(stock, need.dimension()) + 0.001 >= need.base() * ratio;
    }

    private static void both(String left, String right) {
        link(left, right, 1);
        link(right, left, 1);
    }

    private static void link(String from, String to, double ratio) {
        GRAPH.computeIfAbsent(from, ignored -> new ArrayList<>()).add(new Edge(to, ratio));
    }
}
