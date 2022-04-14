package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Location {

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

    @SerializedName("localtime")
    @Expose
    private String localtime;

    @SerializedName("tz_id")
    @Expose
    private String tz_id;

    public String getTz_id() {
        return tz_id;
    }

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

    public String getLocaltime() {
        return localtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return Double.compare(location.getLat(), getLat()) == 0
                && Double.compare(location.getLon(), getLon()) == 0
                && getName().equals(location.getName())
                && getRegion().equals(location.getRegion())
                && getCountry().equals(location.getCountry())
                && getLocaltime().equals(location.getLocaltime())
                && getTz_id().equals(location.getTz_id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRegion(),
                getCountry(), getLat(), getLon(), getLocaltime(), getTz_id());
    }

    @NonNull
    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", localtime='" + localtime + '\'' +
                ", tz_id='" + tz_id + '\'' +
                '}';
    }
}
