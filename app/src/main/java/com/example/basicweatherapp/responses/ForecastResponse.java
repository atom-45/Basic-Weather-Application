package com.example.basicweatherapp.responses;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.models.Current;
import com.example.basicweatherapp.models.Forecast;
import com.example.basicweatherapp.models.Location;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForecastResponse {

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("current")
    @Expose
    private Current current;

    @SerializedName("forecast")
    private Forecast forecast;

    public Location getLocation() {
        return location;
    }

    public Current getCurrent() {
        return current;
    }

    public Forecast getForecast() {
        return forecast;
    }

    @NonNull
    @Override
    public String toString() {
        return "ForecastResponse{" +
                "location=" + location +
                ", current=" + current +
                ", forecast=" + forecast +
                '}';
    }
}
