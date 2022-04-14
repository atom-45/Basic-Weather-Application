package com.example.basicweatherapp.responses;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.models.Place;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {

    @SerializedName(value = "")
    @Expose
    private List<Place> places;

    public List<Place> getPlaces() {
        return places;
    }

    @NonNull
    @Override
    public String toString() {
        return "SearchResponse{" +
                "places=" + places +
                '}';
    }
}
