package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Day {

    @SerializedName("maxtemp_c")
    @Expose
    private double maxTemperature;

    @SerializedName("mintemp_c")
    @Expose
    private double minTemperature;

    @SerializedName("avghumidity")
    @Expose
    private int averageHumidity;

    @SerializedName("maxwind_kph")
    @Expose
    private double maxWind;

    @SerializedName("condition")
    @Expose
    private Condition condition;

    @SerializedName("uv")
    @Expose
    private int uv;

    @SerializedName("daily_chance_of_rain")
    @Expose
    private int rain_probability;

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public int getAverageHumidity() {
        return averageHumidity;
    }

    public double getMaxWind() {
        return maxWind;
    }

    public Condition getCondition() {
        return condition;
    }

    public int getUv() {
        return uv;
    }

    public int getRain_probability() {
        return rain_probability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Day)) return false;
        Day day = (Day) o;
        return Double.compare(day.getMaxTemperature(), getMaxTemperature()) == 0
                && Double.compare(day.getMinTemperature(), getMinTemperature()) == 0
                && Double.compare(day.getAverageHumidity(), getAverageHumidity()) == 0
                && Double.compare(day.getMaxWind(), getMaxWind()) == 0
                && getUv() == day.getUv()
                && getRain_probability() == day.getRain_probability()
                && getCondition().equals(day.getCondition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaxTemperature(), getMinTemperature(), getAverageHumidity(),
                getMaxWind(), getCondition(), getUv(), getRain_probability());
    }

    @NonNull
    @Override
    public String toString() {
        return "Day{" +
                "maxTemperature=" + maxTemperature +
                ", minTemperature=" + minTemperature +
                ", averageHumidity=" + averageHumidity +
                ", maxWind=" + maxWind +
                ", condition=" + condition +
                ", uv=" + uv +
                ", rain_probability=" + rain_probability +
                '}';
    }
}
