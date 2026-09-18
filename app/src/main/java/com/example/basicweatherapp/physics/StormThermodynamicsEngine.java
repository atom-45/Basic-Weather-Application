package com.example.basicweatherapp.physics;

import com.example.basicweatherapp.data.models.SensorData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Core engine that implements the First Law of Thermodynamics to analyze
 * storm approach and safety.
 *
 * Equation: dT/dt = (1/(rho * cp)) * dp/dt + J/cp
 */
public final class StormThermodynamicsEngine {
    private StormThermodynamicsEngine() {}

    /**
     * Performs a thermodynamic analysis based on two consecutive sensor readings.
     * Uses Backward Difference for numerical derivatives.
     *
     * @param prev The previous sensor data point
     * @param current The current sensor data point
     * @return A StormReport containing the analysis results
     */
    public static StormReport analyze(SensorData prev, SensorData current) {
        if (prev == null || current == null) {
            return new StormReport(0, -1, "N/A", 0, true, "Awaiting Data...");
        }

        try {
            // 1. Time Difference (dt) - Handles irregular intervals and resets
            LocalDateTime t1 = LocalDateTime.parse(prev.getDate());
            LocalDateTime t2 = LocalDateTime.parse(current.getDate());
            double dtSeconds = ChronoUnit.SECONDS.between(t1, t2);

            if (dtSeconds <= 0) {
                return new StormReport(0, -1, "N/A", 0, true, "Invalid Time Delta");
            }

            // 2. Air Density (rho)
            double rho = DensityCalculator.calculate(
                    current.getTemperature(),
                    current.getPressure(),
                    current.getHumidity()
            );

            // 3. Numerical Derivatives (Backward Difference)
            // dp/dt in Pa/s (Pressure converted from hPa to Pa)
            double dpPa = (current.getPressure() - prev.getPressure()) * 100.0;
            double dp_dt = dpPa / dtSeconds;

            // dT/dt in K/s (or °C/s)
            double actual_dT_dt = (current.getTemperature() - prev.getTemperature()) / dtSeconds;

            // 4. Solve for Theoretical dT/dt using the First Law
            // Using default J_heating as per user request
            double theoretical_dT_dt = (1.0 / (rho * AtmosphericConstants.CP)) * dp_dt 
                                        + (AtmosphericConstants.DEFAULT_J / AtmosphericConstants.CP);

            // 5. Calculate Residual (Actual - Predicted)
            double residual = actual_dT_dt - theoretical_dT_dt;

            // 6. Storm Speed (km/h)
            // Using Stationary Pressure Tendency vs Gradient (0.02 hPa/km)
            double dp_dt_hpa_hr = Math.abs((current.getPressure() - prev.getPressure()) / dtSeconds) * 3600.0;
            double speedKmh = dp_dt_hpa_hr / AtmosphericConstants.GRADIENT_REF;

            // 7. Arrival Time Calculation (Minutes)
            double dewPoint = DewPointCalculator.calculate(current.getTemperature(), current.getHumidity());
            double arrivalTimeMins = -1; // -1 means "Not cooling towards rain"
            String arrivalTimestamp = "N/A";

            // If we are cooling (actual or predicted), check time to hit dew point
            double coolingRate = Math.min(actual_dT_dt, theoretical_dT_dt);
            if (coolingRate < 0) {
                double tempGap = current.getTemperature() - dewPoint;
                if (tempGap > 0) {
                    arrivalTimeMins = (tempGap / Math.abs(coolingRate)) / 60.0;
                    // Project arrival timestamp
                    LocalDateTime arrivalTime = t2.plusMinutes((long) arrivalTimeMins);
                    
                    // Date-aware formatting
                    if (arrivalTime.toLocalDate().isAfter(t2.toLocalDate())) {
                        arrivalTimestamp = arrivalTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
                    } else {
                        arrivalTimestamp = arrivalTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    }
                }
            }

            // 8. Safety Determination
            // UNSAFE if:
            // - Residual is significantly negative (cooling faster than physics predicts)
            // - Arrival time is short (< 45 mins)
            // - Pressure is dropping rapidly (> 1 hPa/hr)
            boolean isSafe = true;
            String message = "Safe to walk.";

            if (dp_dt_hpa_hr > 1.0 || (arrivalTimeMins > 0 && arrivalTimeMins < 45) || residual < -0.0005) {
                isSafe = false;
                if (arrivalTimeMins > 0 && arrivalTimeMins < 45) {
                    message = String.format(Locale.ENGLISH, "Storm arriving at %s. Not safe.", arrivalTimestamp);
                } else {
                    message = "Unstable atmosphere. Caution advised.";
                }
            }

            return new StormReport(speedKmh, arrivalTimeMins, arrivalTimestamp, residual, isSafe, message);

        } catch (DateTimeParseException e) {
            return new StormReport(0, -1, "N/A", 0, true, "Timestamp Error");
        }
    }
}
