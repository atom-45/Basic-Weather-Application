package com.example.basicweatherapp.responses;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.models.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class CurrentResponse {

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("current")
    @Expose
    private Current current;

    public Location getLocation() {
        return location;
    }

    public Current getCurrent() {
        return current;
    }


    @NonNull
    @Override
    public String toString() {
        return "CurrentResponse{" +
                "location=" + location +
                ", current=" + current +
                '}';
    }
}
