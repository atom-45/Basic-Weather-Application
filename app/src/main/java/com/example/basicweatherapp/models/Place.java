package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Place {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("region")
    @Expose
    private String region;

    @SerializedName("country")
    @Expose
    private String country;

    @SerializedName("lat")
    @Expose
    private double lat;

    @SerializedName("lon")
    @Expose
    private double lon;

    @SerializedName("url")
    @Expose
    private String url;

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getCountry() {
        return country;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;
        Place place = (Place) o;
        return Double.compare(place.getLat(), getLat()) == 0
                && Double.compare(place.getLon(), getLon()) == 0
                && getName().equals(place.getName())
                && getRegion().equals(place.getRegion())
                && getCountry().equals(place.getCountry())
                && getUrl().equals(place.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRegion(),
                getCountry(), getLat(), getLon(), getUrl());
    }

    @NonNull
    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", url='" + url + '\'' +
                '}';
    }
}
