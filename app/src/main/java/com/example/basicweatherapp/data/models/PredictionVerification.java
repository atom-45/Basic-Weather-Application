package com.example.basicweatherapp.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "prediction_verifications")
public class PredictionVerification implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "temperature")
    private float temperature;

    @ColumnInfo(name = "humidity")
    private float humidity;

    @ColumnInfo(name = "pressure")
    private float pressure;

    @ColumnInfo(name = "prediction")
    private String prediction;

    @ColumnInfo(name = "actual_outcome")
    private String actualOutcome;

    @ColumnInfo(name = "residual")
    private double residual;

    @ColumnInfo(name = "source")
    private String source;

    public PredictionVerification(String timestamp, float temperature, float humidity, float pressure, 
                                  String prediction, String actualOutcome, double residual, String source) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.prediction = prediction;
        this.actualOutcome = actualOutcome;
        this.residual = residual;
        this.source = source;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public String getActualOutcome() {
        return actualOutcome;
    }

    public void setActualOutcome(String actualOutcome) {
        this.actualOutcome = actualOutcome;
    }

    public double getResidual() {
        return residual;
    }

    public void setResidual(double residual) {
        this.residual = residual;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
