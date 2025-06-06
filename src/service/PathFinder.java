package service;

import java.util.*;

public class PathFinder {

    public static List<String> findShortestPath(Map<String, Map<String, Double>> graph, String start, String end) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (String node : graph.keySet()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(start, 0.0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            for (Map.Entry<String, Double> neighbor : graph.get(current).entrySet()) {
                String next = neighbor.getKey();
                double weight = neighbor.getValue();
                double newDist = dist.get(current) + weight;
                if (newDist < dist.get(next)) {
                    dist.put(next, newDist);
                    prev.put(next, current);
                    queue.add(next);
                }
            }
        }

        // 回溯路径
        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}
