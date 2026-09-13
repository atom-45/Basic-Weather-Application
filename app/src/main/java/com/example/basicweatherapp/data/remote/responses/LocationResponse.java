package com.example.basicweatherapp.data.remote.responses;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.data.models.Location;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationResponse {

    @SerializedName("location")
    @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationResponse{" +
                "location=" + location +
                '}';
    }
}
