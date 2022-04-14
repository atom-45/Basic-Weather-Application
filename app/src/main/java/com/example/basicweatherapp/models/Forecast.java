package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class Forecast {

    @SerializedName("forecastday")
    @Expose
    private List<ForecastDay> forecastDays;


    public List<ForecastDay> getForecastDays() {
        return forecastDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Forecast)) return false;
        Forecast forecast = (Forecast) o;
        return getForecastDays().equals(forecast.getForecastDays());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForecastDays());
    }

    @NonNull
    @Override
    public String toString() {
        return "Forecast{" +
                "forecastDays=" + forecastDays +
                '}';
    }
}
