package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "locations")
public class Location {

    @PrimaryKey(autoGenerate = true)
    private int locationID;

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

    @ColumnInfo(name = "localtime")
    @SerializedName("localtime")
    @Expose
    private String localtime;

    @ColumnInfo(name = "tz_id")
    @SerializedName("tz_id")
    @Expose
    private String tz_id;


    public Location(String name, String region, String country,
                    double lat, double lon, String localtime, String tz_id)
    {
        this.name = name;
        this.region = region;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
        this.localtime = localtime;
        this.tz_id = tz_id;
    }

    public int getLocationID() {
        return locationID;
    }
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

    public void setLocationID(int locationID) {
        this.locationID = locationID;
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

    public void setLocaltime(String localtime) {
        this.localtime = localtime;
    }

    public void setTz_id(String tz_id) {
        this.tz_id = tz_id;
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
