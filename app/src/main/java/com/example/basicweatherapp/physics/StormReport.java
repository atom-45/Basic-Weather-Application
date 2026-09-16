package com.example.basicweatherapp.physics;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Encapsulates the results of a thermodynamic storm analysis.
 */
public record StormReport(double stormSpeedKmh, double arrivalTimeMinutes,
                          String arrivalTimestamp,
                          double thermodynamicResidual, boolean isSafeToWalk,
                          String statusMessage) {

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "StormReport{speed=%.2f, arrival=%.2f, safe=%b, msg='%s'}",
                stormSpeedKmh, arrivalTimeMinutes, isSafeToWalk, statusMessage);
    }
}
