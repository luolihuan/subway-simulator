package test;

import model.MetroMap;
import service.*;
import utils.DataLoader;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        MetroMap map = new MetroMap();

        // 从 data/subway.txt 读取数据
        DataLoader.loadSubwayData("data/subway.txt", map);

        // 查询最短路径
        List<String> path = PathFinder.findShortestPath(map.getGraph(), "径河", "汉西一路");
        System.out.println("最短路径：");
        for (String station : path) {
            System.out.print(station + " → ");
        }
        System.out.println("终点");

        // 计算总距离
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += map.getGraph().get(path.get(i)).get(path.get(i + 1));
        }

        // 输出不同票种票价
        System.out.println("\n总距离：" + totalDistance + " KM");
        System.out.println("普通票价：" + FareCalculator.calculateFare(totalDistance, "normal") + " 元");
        System.out.println("武汉通票价：" + FareCalculator.calculateFare(totalDistance, "wuhanpass") + " 元");
        System.out.println("日票票价：" + FareCalculator.calculateFare(totalDistance, "daypass") + " 元");
    }
}
