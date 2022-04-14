package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


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

    public String getDate() {
        return date;
    }


    public com.example.basicweatherapp.models.Day getDay() {
        return Day;
    }

    public Astro getAstro() {
        return astro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForecastDay)) return false;
        ForecastDay that = (ForecastDay) o;
        return getDate().equals(that.getDate())
                && getDay().equals(that.getDay())
                && getAstro().equals(that.getAstro());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getDay(), getAstro());
    }

    @NonNull
    @Override
    public String toString() {
        return "ForecastDay{" +
                "date='" + date + '\'' +
                ", Day=" + Day +
                ", astro=" + astro +
                '}';
    }
}
