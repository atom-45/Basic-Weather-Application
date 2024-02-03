package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.Objects;

public class Current {

    @SerializedName("temp_c")
    @Expose
    private double temp_c;

    @SerializedName("last_updated")
    @Expose
    private String lastUpdated;

    @SerializedName("humidity")
    @Expose
    private int humidity;

    @SerializedName("pressure_mb")
    @Expose
    private double pressure;

    @SerializedName("condition")
    @Expose
    private Condition condition;

    @SerializedName("wind_kph")
    @Expose
    private double windSpeed;

    @SerializedName("wind_dir")
    @Expose
    private String windDirection;

    @SerializedName("feelslike_c")
    @Expose
    private double feelslikeTempC;

    public String getWindDirection() {
        return windDirection;
    }
    public double getFeelslikeTempC() {
        return feelslikeTempC;
    }

    public double getWindSpeed() { return windSpeed; }
    public double getTemp_c() {
        return temp_c;
    }
    public String getLastUpdated() {
        return lastUpdated;
    }
    public int getHumidity() {
        return humidity;
    }
    public double getPressure() {
        return pressure;
    }
    public Condition getCondition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Current current)) return false;
        return Double.compare(current.getTemp_c(), getTemp_c()) == 0 &&
                getHumidity() == current.getHumidity() &&
                Double.compare(current.getPressure(), getPressure()) == 0 &&
                Double.compare(current.getWindSpeed(), getWindSpeed()) == 0 &&
                Double.compare(current.getFeelslikeTempC(), getFeelslikeTempC()) == 0 &&
                Objects.equals(getLastUpdated(), current.getLastUpdated()) &&
                Objects.equals(getCondition(), current.getCondition()) &&
                Objects.equals(getWindDirection(), current.getWindDirection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTemp_c(), getLastUpdated(), getHumidity(),
                getPressure(), getCondition(), getWindSpeed(),
                getWindDirection(), getFeelslikeTempC());
    }

    @NonNull
    @Override
    public String toString() {
        return "Current{" +
                "temp_c=" + temp_c +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", condition=" + condition +
                ", windSpeed=" + windSpeed +
                ", windDirection='" + windDirection + '\'' +
                ", feelslikeTempC=" + feelslikeTempC +
                '}';
    }
}
