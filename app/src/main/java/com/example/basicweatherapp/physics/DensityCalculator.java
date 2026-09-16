package com.example.basicweatherapp.physics;

/**
 * Utility to calculate air density (rho) with virtual temperature correction.
 */
public final class DensityCalculator {
    private DensityCalculator() {}

    /**
     * Calculates air density (rho) in kg/m^3.
     * Uses the Ideal Gas Law with Virtual Temperature to account for moisture.
     *
     * @param tempC Temperature in Celsius
     * @param pressureHpa Pressure in hPa
     * @param humidityPct Relative Humidity percentage
     * @return Air density in kg/m^3
     */
    public static double calculate(float tempC, float pressureHpa, float humidityPct) {
        double tempK = tempC + 273.15;
        double pressurePa = pressureHpa * 100.0;
        
        // Approximate specific humidity (q) from relative humidity
        // This is a first-order approximation for q
        double q = (humidityPct / 100.0) * 0.01;
        
        // Calculate Virtual Temperature (Tv)
        // Tv = T * (1 + 0.61 * q)
        double virtualTempK = tempK * (1.0 + 0.61 * q);
        
        // Ideal Gas Law: rho = P / (R_dry * Tv)
        return pressurePa / (AtmosphericConstants.R_DRY * virtualTempK);
    }
}
