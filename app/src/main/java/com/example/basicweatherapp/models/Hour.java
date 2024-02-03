package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Hour {

    @SerializedName("time")
    @Expose
    private String time;

    @SerializedName("condition")
    @Expose
    private Condition condition;

    @SerializedName("temp_c")
    @Expose
    private double temp_c;

    @SerializedName("feelslike_c")
    @Expose
    private double feelsLikeTempC;

    @SerializedName("wind_kph")
    @Expose
    private double windKph;

    @SerializedName("wind_dir")
    @Expose
    private String windDirection;

    @SerializedName("humidity")
    @Expose
    private int humidity;

    @SerializedName("chance_of_rain")
    @Expose
    private double chanceOfRain;



    public String getTime() {
        return time;
    }

    public Condition getCondition() {
        return condition;
    }

    public double getTemp_c() {
        return temp_c;
    }

    public double getFeelsLikeTempC() {
        return feelsLikeTempC;
    }

    public double getWindKph() {
        return windKph;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getChanceOfRain() {
        return chanceOfRain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hour hour)) return false;
        return Double.compare(hour.getTemp_c(), getTemp_c()) == 0 &&
                Double.compare(hour.getFeelsLikeTempC(), getFeelsLikeTempC()) == 0 &&
                Double.compare(hour.getWindKph(), getWindKph()) == 0 &&
                getHumidity() == hour.getHumidity() &&
                Double.compare(hour.getChanceOfRain(), getChanceOfRain()) == 0 &&
                Objects.equals(getTime(), hour.getTime()) &&
                Objects.equals(getCondition(), hour.getCondition()) &&
                Objects.equals(getWindDirection(), hour.getWindDirection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getCondition(), getTemp_c(),
                getFeelsLikeTempC(), getWindKph(), getWindDirection(),
                getHumidity(), getChanceOfRain());
    }

    @NonNull
    @Override
    public String toString() {
        return "Hour{" +
                "time='" + time + '\'' +
                ", condition=" + condition +
                ", temp_c=" + temp_c +
                ", feelsLikeTempC=" + feelsLikeTempC +
                ", windKph=" + windKph +
                ", windDirection='" + windDirection + '\'' +
                ", humidity=" + humidity +
                ", chanceOfRain=" + chanceOfRain +
                '}';
    }
}
