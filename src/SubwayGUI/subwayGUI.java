package SubwayGUI;

import service.*;
import model.*;
import util.Triple;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.util.List;

public class subwayGUI extends JFrame {
    private SubwaySystem system;
    private JComboBox<String> stationComboBox;
    private DefaultListModel<String> stationListModel;
    private JList<String> stationList;
    private JTextArea outputArea;
    private JTabbedPane tabbedPane;

    public subwayGUI() {
        super("武汉地铁导航系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // 初始化系统
        system = new SubwaySystem();
        try {
            system.loadFromFile("data/subway.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载地铁数据失败: " + e.getMessage(), 
                                          "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        initUI();
    }

    private void initUI() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建顶部面板
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("中转站查询", createTransferPanel());
        tabbedPane.addTab("附近站点查询", createNearbyPanel());
        tabbedPane.addTab("路径查询", createPathPanel());
        tabbedPane.addTab("票价计算", createFarePanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 创建输出区域
        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("宋体", Font.PLAIN, 14));
        outputArea.setBorder(BorderFactory.createTitledBorder("系统输出"));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("地铁站点"));
        
        // 站点选择下拉框
        stationComboBox = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            stationComboBox.addItem(station);
        }
        stationComboBox.setPreferredSize(new Dimension(250, 30));
        
        // 添加到列表按钮
        JButton addButton = new JButton("添加到列表");
        addButton.addActionListener(e -> addToStationList());
        
        // 清空列表按钮
        JButton clearButton = new JButton("清空列表");
        clearButton.addActionListener(e -> stationListModel.clear());
        
        // 站点列表
        stationListModel = new DefaultListModel<>();
        stationList = new JList<>(stationListModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setFixedCellWidth(200);
        stationList.setFixedCellHeight(25);
        
        JScrollPane listScroll = new JScrollPane(stationList);
        listScroll.setPreferredSize(new Dimension(250, 100));
        
        panel.add(new JLabel("选择站点:"));
        panel.add(stationComboBox);
        panel.add(addButton);
        panel.add(clearButton);
        panel.add(new JLabel("已选站点:"));
        panel.add(listScroll);
        
        return panel;
    }
    
    private void addToStationList() {
        String selected = (String) stationComboBox.getSelectedItem();
        if (selected != null && !stationListModel.contains(selected)) {
            stationListModel.addElement(selected);
        }
    }
    
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 按钮
        JButton transferButton = new JButton("查询所有中转站");
        transferButton.addActionListener(e -> {
            Map<String, Set<String>> transfers = system.getTransferStations();
            if (transfers.isEmpty()) {
                outputArea.setText("未找到中转站");
                return;
            }
            
            StringBuilder sb = new StringBuilder("===== 中转站列表 =====\n");
            transfers.forEach((station, lines) -> 
                sb.append(station).append("站: ").append(lines).append("\n"));
            
            outputArea.setText(sb.toString());
        });
        
        panel.add(transferButton, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNearbyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 起点选择
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("起点站:"), gbc);
        
        JComboBox<String> startCombo = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            startCombo.addItem(station);
        }
        startCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(startCombo, gbc);
        
        // 距离输入
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("最大距离(公里):"), gbc);
        
        JSpinner distanceSpinner = new JSpinner(new SpinnerNumberModel(2.0, 0.1, 50.0, 0.5));
        distanceSpinner.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(distanceSpinner, gbc);
        
        // 查询按钮
        JButton nearbyButton = new JButton("查询附近站点");
        nearbyButton.addActionListener(e -> {
            String start = (String) startCombo.getSelectedItem();
            double maxDistance = (Double) distanceSpinner.getValue();
            
            try {
                List<Triple<String, String, Double>> results = 
                    system.getStationsWithinDistance(start, maxDistance);
                
                if (results.isEmpty()) {
                    outputArea.setText("未找到附近站点");
                    return;
                }
                
                StringBuilder sb = new StringBuilder("===== 附近站点查询结果 =====\n");
                sb.append("起点: ").append(start).append("\n");
                sb.append("最大距离: ").append(maxDistance).append("公里\n\n");
                
                for (Triple<String, String, Double> result : results) {
                    sb.append(String.format("%s (%s) - %.2f公里\n", 
                            result.first, result.second, result.third));
                }
                
                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("错误: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(nearbyButton, gbc);
        
        return panel;
    }
    
    private JPanel createPathPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建选择面板
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // 起点选择
        selectionPanel.add(new JLabel("起点:"));
        JComboBox<String> startCombo = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            startCombo.addItem(station);
        }
        startCombo.setPreferredSize(new Dimension(150, 30));
        selectionPanel.add(startCombo);
        
        // 终点选择
        selectionPanel.add(new JLabel("终点:"));
        JComboBox<String> endCombo = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            endCombo.addItem(station);
        }
        endCombo.setPreferredSize(new Dimension(150, 30));
        selectionPanel.add(endCombo);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // 所有路径按钮
        JButton allPathsButton = new JButton("查询所有路径");
        allPathsButton.addActionListener(e -> {
            String start = (String) startCombo.getSelectedItem();
            String end = (String) endCombo.getSelectedItem();
            
            try {
                List<List<String>> paths = system.findAllPaths(start, end);
                
                if (paths.isEmpty()) {
                    outputArea.setText("未找到路径");
                    return;
                }
                
                StringBuilder sb = new StringBuilder("===== 所有路径查询结果 =====\n");
                sb.append("起点: ").append(start).append(" → 终点: ").append(end).append("\n");
                sb.append("共找到 ").append(paths.size()).append(" 条路径\n\n");
                
                for (int i = 0; i < Math.min(5, paths.size()); i++) {
                    sb.append("路径 ").append(i + 1).append(":\n");
                    for (String station : paths.get(i)) {
                        sb.append(station).append(" → ");
                    }
                    sb.delete(sb.length() - 3, sb.length());
                    sb.append("\n\n");
                }
                
                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("错误: " + ex.getMessage());
            }
        });
        
        // 最短路径按钮
        JButton shortestButton = new JButton("查询最短路径");
        shortestButton.addActionListener(e -> {
            String start = (String) startCombo.getSelectedItem();
            String end = (String) endCombo.getSelectedItem();
            
            try {
                List<String> path = system.findShortestPath(start, end);
                
                if (path == null || path.isEmpty()) {
                    outputArea.setText("未找到路径");
                    return;
                }
                
                StringBuilder sb = new StringBuilder("===== 最短路径查询结果 =====\n");
                sb.append("起点: ").append(start).append(" → 终点: ").append(end).append("\n");
                sb.append("总站点数: ").append(path.size()).append("\n\n");
                
                sb.append("路径详情:\n");
                for (String station : path) {
                    sb.append(station).append(" → ");
                }
                sb.delete(sb.length() - 3, sb.length());
                sb.append("\n\n");
                
                // 格式化输出
                sb.append("推荐乘车路线:\n");
                system.printFormattedPath(path);
                
                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("错误: " + ex.getMessage());
            }
        });
        
        buttonPanel.add(allPathsButton);
        buttonPanel.add(shortestButton);
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFarePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建选择面板
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // 起点选择
        selectionPanel.add(new JLabel("起点:"));
        JComboBox<String> startCombo = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            startCombo.addItem(station);
        }
        startCombo.setPreferredSize(new Dimension(150, 30));
        selectionPanel.add(startCombo);
        
        // 终点选择
        selectionPanel.add(new JLabel("终点:"));
        JComboBox<String> endCombo = new JComboBox<>();
        for (String station : system.getTransferStations().keySet()) {
            endCombo.addItem(station);
        }
        endCombo.setPreferredSize(new Dimension(150, 30));
        selectionPanel.add(endCombo);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        
        // 票种选择
        JPanel ticketPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        ticketPanel.setBorder(BorderFactory.createTitledBorder("票种选择"));
        
        ButtonGroup ticketGroup = new ButtonGroup();
        JRadioButton normalButton = new JRadioButton("普通票", true);
        JRadioButton wuhanpassButton = new JRadioButton("武汉通");
        JRadioButton daypassButton = new JRadioButton("日票");
        
        ticketGroup.add(normalButton);
        ticketGroup.add(wuhanpassButton);
        ticketGroup.add(daypassButton);
        
        ticketPanel.add(normalButton);
        ticketPanel.add(wuhanpassButton);
        ticketPanel.add(daypassButton);
        
        panel.add(ticketPanel, BorderLayout.CENTER);
        
        // 计算按钮
        JButton calculateButton = new JButton("计算票价");
        calculateButton.addActionListener(e -> {
            String start = (String) startCombo.getSelectedItem();
            String end = (String) endCombo.getSelectedItem();
            String ticketType = "normal";
            
            if (wuhanpassButton.isSelected()) ticketType = "wuhanpass";
            else if (daypassButton.isSelected()) ticketType = "daypass";
            
            try {
                List<String> path = system.findShortestPath(start, end);
                if (path == null || path.isEmpty()) {
                    outputArea.setText("未找到路径");
                    return;
                }
                
                double fare = system.calculateFare(path, ticketType);
                
                StringBuilder sb = new StringBuilder("===== 票价计算结果 =====\n");
                sb.append("起点: ").append(start).append(" → 终点: ").append(end).append("\n");
                sb.append("票种: ");
                
                switch (ticketType) {
                    case "normal": sb.append("普通票"); break;
                    case "wuhanpass": sb.append("武汉通 (9折)"); break;
                    case "daypass": sb.append("日票 (免费)"); break;
                }
                
                sb.append("\n票价: ");
                if (fare == 0) {
                    sb.append("免费");
                } else {
                    sb.append(String.format("%.2f 元", fare));
                }
                
                sb.append("\n\n路径详情:\n");
                for (int i = 0; i < Math.min(10, path.size()); i++) {
                    sb.append(path.get(i)).append(" → ");
                }
                if (path.size() > 10) {
                    sb.append("... → ");
                }
                sb.append(path.get(path.size() - 1));
                
                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("错误: " + ex.getMessage());
            }
        });
        
        panel.add(calculateButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            subwayGUI gui = new subwayGUI();
            gui.setVisible(true);
        });
    }
}

class EnhancedSubwaySystem extends SubwaySystem {
    // 增强方法：在格式化输出路径时收集结果
    public String getFormattedPath(List<String> path) {
        if (path == null || path.size() < 2) {
            return "路径无效或过短";
        }
        
        StringBuilder sb = new StringBuilder();
        String currentLine = getCommonLine(path.get(0), path.get(1));
        String segmentStart = path.get(0);
        int segmentCount = 0;
        
        for (int i = 1; i < path.size(); i++) {
            String prevStation = path.get(i-1);
            String currentStation = path.get(i);
            Set<String> commonLines = getCommonLines(prevStation, currentStation);
            
            if (!commonLines.contains(currentLine)) {
                if (segmentCount > 0) {
                    sb.append(String.format("乘坐 %s 从 %s 到 %s (%d站)%n", 
                            currentLine, segmentStart, prevStation, segmentCount));
                }
                
                currentLine = commonLines.iterator().next();
                segmentStart = prevStation;
                segmentCount = 1;
            } else {
                segmentCount++;
            }
            
            if (i == path.size() - 1) {
                sb.append(String.format("乘坐 %s 从 %s 到 %s (%d站)%n", 
                        currentLine, segmentStart, currentStation, segmentCount));
            }
        }
        
        return sb.toString();
    }
    
    // 覆盖父类方法以使用增强功能
    @Override
    public void printFormattedPath(List<String> path) {
        System.out.print(getFormattedPath(path));
    }
}