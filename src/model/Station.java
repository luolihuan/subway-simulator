package model;

import java.util.*;

public class Station {
    private String name;
    private Set<String> lines = new HashSet<>(); // 经过的线路

    public Station(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<String> getLines() {
        return lines;
    }

    public void addLine(String lineName) {
        lines.add(lineName);
    }

    @Override
    public String toString() {
        return name + "站 " + lines;
    }
}