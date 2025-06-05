package model;

import java.util.*;

public class Line {
    private String name;
    private List<String> stations = new ArrayList<>();

    public Line(String name) {
        this.name = name;
    }

    public void addStation(String stationName) {
        stations.add(stationName);
    }

    public String getName() {
        return name;
    }

    public List<String> getStations() {
        return stations;
    }
}
