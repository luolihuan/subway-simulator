package test;
import service.*;
import model.*;
import util.Triple;
import java.util.*;

public class Test {
    public static void main(String[] args) {
        SubwaySystem system = new SubwaySystem();
        
        try {
            // 1. 加载地铁数据
            System.out.println("正在加载地铁数据...");
            system.loadFromFile("data/subway.txt");
            System.out.println("数据加载完成！");
            
            // 2. 测试任务1：获取所有中转站
            System.out.println("\n===== 任务1: 中转站识别 =====");
            Map<String, Set<String>> transferStations = system.getTransferStations();
            if (transferStations.isEmpty()) {
                System.out.println("未找到中转站，请检查数据文件格式");
            } else {
                System.out.println("找到 " + transferStations.size() + " 个中转站:");
                transferStations.forEach((station, lines) -> 
                    System.out.println(" - " + station + "站: " + lines));
            }
            
            // 3. 测试任务2：附近站点查询
            System.out.println("\n===== 任务2: 附近站点查询 =====");
            String testStation = "古田四路";
            double maxDistance = 2.0;
            System.out.println("查询站点: " + testStation + ", 最大距离: " + maxDistance + "公里");
            
            List<Triple<String, String, Double>> nearby = system.getStationsWithinDistance(testStation, maxDistance);
            if (nearby.isEmpty()) {
                System.out.println("未找到附近站点");
            } else {
                System.out.println("找到 " + nearby.size() + " 个附近站点:");
                nearby.forEach(System.out::println);
            }
            
            // 4. 测试任务3：所有路径查询
            System.out.println("\n===== 任务3: 所有路径查询 =====");
            String startStation = "汉口火车站";
            String endStation = "武昌火车站";
            System.out.println("查询路径: " + startStation + " → " + endStation);
            
            List<List<String>> allPaths = system.findAllPaths(startStation, endStation);
            System.out.println("找到路径数量: " + allPaths.size());
            if (!allPaths.isEmpty()) {
                System.out.println("第一条路径: " + allPaths.get(0));
            }
            
            // 5. 测试任务4：最短路径查询
            System.out.println("\n===== 任务4: 最短路径查询 =====");
            List<String> shortestPath = system.findShortestPath(startStation, endStation);
            System.out.println("最短路径: " + shortestPath);
            
            // 6. 测试任务5：格式化输出路径 - 确保调用此功能
            System.out.println("\n===== 任务5: 路径格式化输出 =====");
            system.printFormattedPath(shortestPath);
            
            // 7. 测试任务6和7：票价计算
            System.out.println("\n===== 任务6 & 7: 票价计算 =====");
            double normalFare = system.calculateFare(shortestPath, "normal");
            double wuhanpassFare = system.calculateFare(shortestPath, "wuhanpass");
            double daypassFare = system.calculateFare(shortestPath, "daypass");
            
            System.out.printf("普通票: %.2f元%n", normalFare);
            System.out.printf("武汉通: %.2f元%n", wuhanpassFare);
            System.out.printf("日票: %.2f元%n", daypassFare);
            
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
