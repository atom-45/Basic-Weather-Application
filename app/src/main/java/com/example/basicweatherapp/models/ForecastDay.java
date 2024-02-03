package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


import java.util.List;
import java.util.Objects;


public class ForecastDay {

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("day")
    @Expose
    private Day Day;

    @SerializedName("astro")
    @Expose
    private Astro astro;

    @SerializedName("hour")
    @Expose
    private List<Hour> hours;

    public String getDate() {
        return date;
    }


    public com.example.basicweatherapp.models.Day getDay() {
        return Day;
    }

    public Astro getAstro() {
        return astro;
    }

    public List<Hour> getHours() {
        return hours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForecastDay that)) return false;
        return Objects.equals(getDate(), that.getDate()) &&
                Objects.equals(getDay(), that.getDay()) &&
                Objects.equals(getAstro(), that.getAstro()) &&
                Objects.equals(getHours(), that.getHours());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getDay(), getAstro(), getHours());
    }

    @NonNull
    @Override
    public String toString() {
        return "ForecastDay{" +
                "date='" + date + '\'' +
                ", Day=" + Day +
                ", astro=" + astro +
                ", hours=" + hours +
                '}';
    }
}
