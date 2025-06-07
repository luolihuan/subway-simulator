package service;
import model.*;
import util.Triple;
import java.util.*;
import java.io.*;

public class SubwaySystem {
    private Map<String, Line> lines = new HashMap<>();
    private Map<String, Station> stations = new HashMap<>();
    private MetroMap metroMap = new MetroMap();
    
    // 文件解析方法 - 修复线路解析和站点关联
    public void loadFromFile(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String currentLine = null;
            String line;
            List<String> currentStations = new ArrayList<>();
            
            while ((line = br.readLine()) != null) {
                // 检测新线路 - 修复线路名称提取
                if (line.contains("号线站点间距")) {
                    currentLine = line.split("号线")[0].trim();
                    if (currentLine.startsWith("（")) {
                        currentLine = currentLine.substring(1);
                    }
                    lines.put(currentLine, new Line(currentLine));
                    currentStations.clear();
                    br.readLine(); // 跳过表头
                    br.readLine(); // 跳过空行
                    continue;
                }
                
                // 解析站点数据
                if (line.contains("---")) {
                    String[] parts = line.split("\t");
                    if (parts.length < 2) continue;
                    
                    String[] stationsPair = parts[0].split("---");
                    if (stationsPair.length != 2) continue;
                    
                    String stationA = stationsPair[0].trim();
                    String stationB = stationsPair[1].trim();
                    double distance = Double.parseDouble(parts[1].trim());
                    
                    // 添加站点到线路
                    Line lineObj = lines.get(currentLine);
                    if (!currentStations.contains(stationA)) {
                        lineObj.addStation(stationA);
                        currentStations.add(stationA);
                    }
                    lineObj.addStation(stationB);
                    currentStations.add(stationB);
                    
                    // 创建或更新站点对象 - 确保线路正确关联
                    Station stationAObj = stations.computeIfAbsent(stationA, Station::new);
                    Station stationBObj = stations.computeIfAbsent(stationB, Station::new);
                    stationAObj.addLine(currentLine);
                    stationBObj.addLine(currentLine);
                    
                    // 添加到地铁网络图
                    metroMap.addEdge(stationA, stationB, distance);
                }
            }
        }
        
        // 修复：确保所有线路上的站点都有关联的线路信息
        for (Line line : lines.values()) {
            for (String stationName : line.getStations()) {
                Station station = stations.computeIfAbsent(stationName, Station::new);
                station.addLine(line.getName());
            }
        }
    }
    
    // 任务1: 获取所有中转站 - 修复空结果问题
    public Map<String, Set<String>> getTransferStations() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Station station : stations.values()) {
            if (station.getLines().size() >= 2) {
                result.put(station.getName(), new HashSet<>(station.getLines()));
            }
        }
        return result;
    }
    
    // 任务2: 获取距离小于n的所有站点 - 修复空结果问题
    public List<Triple<String, String, Double>> getStationsWithinDistance(
            String stationName, double maxDistance) throws Exception {
        if (!stations.containsKey(stationName)) {
            throw new Exception("站点不存在: " + stationName);
        }
        
        List<Triple<String, String, Double>> result = new ArrayList<>();
        Map<String, Double> distances = calculateAllDistances(stationName);
        
        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            String name = entry.getKey();
            double distance = entry.getValue();
            
            if (distance <= maxDistance && distance > 0) {
                Station station = stations.get(name);
                // 确保站点有线路信息
                if (station != null && !station.getLines().isEmpty()) {
                    for (String line : station.getLines()) {
                        result.add(new Triple<>(name, line, distance));
                    }
                }
            }
        }
        return result;
    }
    
    // 计算从起点到所有站点的距离
    private Map<String, Double> calculateAllDistances(String start) {
        Map<String, Double> dist = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        
        // 初始化
        for (String node : metroMap.getStations()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(start, 0.0);
        queue.add(start);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, Double> neighbors = metroMap.getGraph().getOrDefault(current, new HashMap<>());
            
            for (Map.Entry<String, Double> neighbor : neighbors.entrySet()) {
                String next = neighbor.getKey();
                double weight = neighbor.getValue();
                double newDist = dist.get(current) + weight;
                
                if (newDist < dist.get(next)) {
                    dist.put(next, newDist);
                    queue.add(next);
                }
            }
        }
        return dist;
    }
    
    // 任务3: 查找所有路径（DFS实现）
    public List<List<String>> findAllPaths(String start, String end) throws Exception {
        validateStation(start);
        validateStation(end);
        
        List<List<String>> paths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        
        dfs(start, end, visited, currentPath, paths);
        return paths;
    }
    
    private void dfs(String current, String end, Set<String> visited, 
                    List<String> currentPath, List<List<String>> paths) {
        visited.add(current);
        currentPath.add(current);
        
        if (current.equals(end)) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            Map<String, Double> neighbors = metroMap.getGraph().getOrDefault(current, new HashMap<>());
            for (String neighbor : neighbors.keySet()) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, end, visited, currentPath, paths);
                }
            }
        }
        
        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }
    
    // 任务4: 查找最短路径
    public List<String> findShortestPath(String start, String end) throws Exception {
        validateStation(start);
        validateStation(end);
        return PathFinder.findShortestPath(metroMap.getGraph(), start, end);
    }
    
    public void printFormattedPath(List<String> path) {
        if (path == null || path.size() < 2) {
            System.out.println("路径无效或过短");
            return;
        }
        
        System.out.println("推荐乘车路线:");
        
        String currentLine = getCommonLine(path.get(0), path.get(1));
        String segmentStart = path.get(0);
        int segmentCount = 0;  // 从0开始计数，因为第一个站是起点
        
        for (int i = 1; i < path.size(); i++) {
            String prevStation = path.get(i-1);
            String currentStation = path.get(i);
            
            // 获取两站之间的共同线路
            Set<String> commonLines = getCommonLines(prevStation, currentStation);
            
            // 如果线路发生变化（换乘）
            if (!commonLines.contains(currentLine)) {
                // 输出当前段
                if (segmentCount > 0) {
                    System.out.printf("乘坐 %s 从 %s 到 %s (%d站)%n", 
                            currentLine, segmentStart, prevStation, segmentCount);
                }
                
                // 更新为新线路
                currentLine = commonLines.iterator().next();
                segmentStart = prevStation;
                segmentCount = 1;  // 从换乘站到下一站算1站
            } else {
                segmentCount++;
            }
            
            // 如果是最后一站
            if (i == path.size() - 1) {
                System.out.printf("乘坐 %s 从 %s 到 %s (%d站)%n", 
                        currentLine, segmentStart, currentStation, segmentCount);
            }
        }
    }
    
    // 任务6 & 7: 计算票价
    public double calculateFare(List<String> path, String ticketType) {
        double totalDistance = calculatePathDistance(path);
        return FareCalculator.calculateFare(totalDistance, ticketType);
    }
    
    // 辅助方法
    private double calculatePathDistance(List<String> path) {
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i+1);
            distance += metroMap.getGraph().get(from).get(to);
        }
        return distance;
    }
    
    private Set<String> getCommonLines(String stationA, String stationB) {
        Station a = stations.get(stationA);
        Station b = stations.get(stationB);
        Set<String> common = new HashSet<>(a.getLines());
        common.retainAll(b.getLines());
        return common;
    }
    
    private String getCommonLine(String stationA, String stationB) {
        return getCommonLines(stationA, stationB).iterator().next();
    }
    
    private void validateStation(String name) throws Exception {
        if (!stations.containsKey(name)) {
            throw new Exception("站点不存在: " + name);
        }
    }
}