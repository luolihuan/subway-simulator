package utils;
import model.MetroMap;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DataLoader {

    /**
     * 从 subway.txt 加载地铁图数据
     * @param filePath 文件路径，如 "data/subway.txt"
     * @param map MetroMap 实例（传入空图）
     */
    public static void loadSubwayData(String filePath, MetroMap map) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.contains("站点") || line.contains("间距")) continue; // 跳过表头

                String[] parts = line.split("\t");
                if (parts.length != 2) continue;

                String[] stations = parts[0].split("---");
                if (stations.length != 2) continue;

                String from = stations[0].trim();
                String to = stations[1].trim();
                double distance = Double.parseDouble(parts[1].trim());

                map.addEdge(from, to, distance);
            }

        } catch (IOException e) {
            System.err.println("读取地铁数据文件失败: " + e.getMessage());
        }
    }
}
