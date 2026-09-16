package com.example.basicweatherapp.physics;

/**
 * Constants used for thermodynamic and atmospheric calculations.
 * Units are standard SI where applicable.
 */
public final class AtmosphericConstants {
    private AtmosphericConstants() {}

    /** Specific heat of dry air at constant pressure (J/kg·K) */
    public static final double CP = 1005.0;

    /** Gas constant for dry air (J/kg·K) */
    public static final double R_DRY = 287.058;

    /** Standard atmospheric pressure at sea level (hPa) */
    public static final double P0 = 1013.25;

    /** Standard storm pressure gradient (hPa/km) */
    public static final double GRADIENT_REF = 0.02;

    /** Default diabatic heating rate J = dq/dt (J/kg·s) 
     *  Note: User will calibrate this via Python.
     */
    public static final double DEFAULT_J = 0.05;
}
