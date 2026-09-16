package com.example.basicweatherapp.physics;

/**
 * Utility to calculate Dew Point using the Magnus-Tetens approximation.
 */
public final class DewPointCalculator {
    private DewPointCalculator() {}

    /**
     * Calculates the Dew Point in Celsius.
     *
     * @param tempC Temperature in Celsius
     * @param humidityPct Relative Humidity percentage
     * @return Dew Point in Celsius
     */
    public static double calculate(float tempC, float humidityPct) {
        // Constants for Magnus-Tetens
        final double a = 17.27;
        final double b = 237.7;
        
        double humidity = Math.max(humidityPct, 1.0f) / 100.0;
        
        double gamma = ((a * tempC) / (b + tempC)) + Math.log(humidity);
        
        return (b * gamma) / (a - gamma);
    }
}
