package service;

public class FareCalculator {
    public static double calculateFare(double distance, String type) {
        double price = 0;
        if (distance <= 6) price = 2;
        else if (distance <= 12) price = 3;
        else if (distance <= 18) price = 4;
        else if (distance <= 24) price = 5;
        else price = 6;

        switch (type) {
            case "normal": return price;
            case "wuhanpass": return price * 0.9;
            case "daypass": return 0;
            default: return -1;
        }
    }
}
