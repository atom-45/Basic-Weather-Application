package com.example.basicweatherapp.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "thermodynamic_predictions")
public class ThermodynamicPrediction implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "original_timestamp")
    private String originalTimestamp;

    @ColumnInfo(name = "temp_snapshot")
    private float temperature;

    @ColumnInfo(name = "humidity_snapshot")
    private float humidity;

    @ColumnInfo(name = "pressure_snapshot")
    private float pressure;

    @ColumnInfo(name = "predicted_arrival")
    private String predictedArrival;

    @ColumnInfo(name = "storm_speed")
    private double stormSpeed;

    @ColumnInfo(name = "residual")
    private double residual;

    @ColumnInfo(name = "window_start_millis")
    private long windowStartMillis;

    @ColumnInfo(name = "window_end_millis")
    private long windowEndMillis;

    @ColumnInfo(name = "verification_status")
    private String verificationStatus; // PENDING, VERIFIED, EXPIRED

    @ColumnInfo(name = "storm_event_id")
    private String stormEventId;

    @ColumnInfo(name = "actual_outcome")
    private String actualOutcome;

    public ThermodynamicPrediction(String originalTimestamp, float temperature, float humidity, 
                                   float pressure, String predictedArrival, double stormSpeed, 
                                   double residual, long windowStartMillis, long windowEndMillis, 
                                   String verificationStatus, String stormEventId) {
        this.originalTimestamp = originalTimestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.predictedArrival = predictedArrival;
        this.stormSpeed = stormSpeed;
        this.residual = residual;
        this.windowStartMillis = windowStartMillis;
        this.windowEndMillis = windowEndMillis;
        this.verificationStatus = verificationStatus;
        this.stormEventId = stormEventId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOriginalTimestamp() { return originalTimestamp; }
    public float getTemperature() { return temperature; }
    public float getHumidity() { return humidity; }
    public float getPressure() { return pressure; }
    public String getPredictedArrival() { return predictedArrival; }
    public double getStormSpeed() { return stormSpeed; }
    public double getResidual() { return residual; }
    public long getWindowStartMillis() { return windowStartMillis; }
    public long getWindowEndMillis() { return windowEndMillis; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getStormEventId() { return stormEventId; }
    public String getActualOutcome() { return actualOutcome; }
    public void setActualOutcome(String actualOutcome) { this.actualOutcome = actualOutcome; }
}
