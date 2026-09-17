package com.example.basicweatherapp.physics;

/**
 * Utility to predict rain probability based on static temperature-dewpoint spread.
 */
public final class RainAnalysisEngine {
    private RainAnalysisEngine() {}

    public static String predict(float temp, float dewPoint) {
        double spread = temp - dewPoint;
        if (spread < 4) {
            return "Heavy Rain Imminent";
        } else if (spread < 5.5) {
            return "Rain Expected Soon";
        } else if (spread < 10) {
            return "Unlikely To Rain";
        } else {
            return "Very Dry - No Rain";
        }
    }

    public static boolean isLikely(float temp, float dewPoint) {
        return (temp - dewPoint) < 5.5;
    }
}
