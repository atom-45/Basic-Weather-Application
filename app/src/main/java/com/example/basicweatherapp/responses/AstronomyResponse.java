package com.example.basicweatherapp.responses;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.models.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AstronomyResponse {

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("astronomy")
    private Astronomy astronomy;

    public Location getLocation() {
        return location;
    }

    public Astronomy getAstronomy() {
        return astronomy;
    }

    @NonNull
    @Override
    public String toString() {
        return "AstronomyResponse{" +
                "location=" + location +
                ", astronomy=" + astronomy +
                '}';
    }
}
