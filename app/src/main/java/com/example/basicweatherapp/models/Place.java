package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "places")
public class Place {

    @PrimaryKey(autoGenerate = true)
    private int placeID;

    @ColumnInfo(name = "name")
    @SerializedName("name")
    @Expose
    private String name;

    @ColumnInfo(name = "region")
    @SerializedName("region")
    @Expose
    private String region;

    @ColumnInfo(name = "country")
    @SerializedName("country")
    @Expose
    private String country;

    @ColumnInfo(name = "lat")
    @SerializedName("lat")
    @Expose
    private double lat;

    @ColumnInfo(name = "lon")
    @SerializedName("lon")
    @Expose
    private double lon;

    @ColumnInfo(name = "url")
    @SerializedName("url")
    @Expose
    private String url;



    public Place(String name, String region, String country,
                 double lat, double lon, String url)
    {
        this.name = name;
        this.region = region;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.url = url;
    }


    public int getPlaceID() {
        return placeID;
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

    public String getUrl() {
        return url;
    }


    public void setPlaceID(int placeID) {
        this.placeID = placeID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setUrl(String url) {
        this.url = url;
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
