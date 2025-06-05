package model;

import java.util.*;

public class MetroMap {
    // 地铁站图结构：站点名 → 邻接站和距离
    private Map<String, Map<String, Double>> graph = new HashMap<>();

    public void addEdge(String from, String to, double distance) {
        graph.putIfAbsent(from, new HashMap<>());
        graph.putIfAbsent(to, new HashMap<>());
        graph.get(from).put(to, distance);
        graph.get(to).put(from, distance); // 地铁是双向的
    }

    public Map<String, Map<String, Double>> getGraph() {
        return graph;
    }

    public Set<String> getStations() {
        return graph.keySet();
    }
}
