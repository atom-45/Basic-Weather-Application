package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Astro {

    @SerializedName("sunrise")
    @Expose
    private String sunriseTime;

    @SerializedName("sunset")
    @Expose
    private String sunsetTime;

    @SerializedName("moonrise")
    @Expose
    private String moonriseTime;

    @SerializedName("moonset")
    @Expose
    private String moonsetTime;

    @SerializedName("moon_phase")
    @Expose
    private String moonPhase;

    public String getSunriseTime() {
        return sunriseTime;
    }

    public String getSunsetTime() {
        return sunsetTime;
    }

    public String getMoonriseTime() {
        return moonriseTime;
    }

    public String getMoonsetTime() {
        return moonsetTime;
    }

    public String getMoonPhase() {
        return moonPhase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Astro)) return false;
        Astro astro = (Astro) o;
        return getSunriseTime().equals(astro.getSunriseTime())
                && getSunsetTime().equals(astro.getSunsetTime())
                && getMoonriseTime().equals(astro.getMoonriseTime())
                && getMoonsetTime().equals(astro.getMoonsetTime())
                && getMoonPhase().equals(astro.getMoonPhase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSunriseTime(), getSunsetTime(),
                getMoonriseTime(), getMoonsetTime(), getMoonPhase());
    }

    @NonNull
    @Override
    public String toString() {
        return "Astro{" +
                "sunriseTime='" + sunriseTime + '\'' +
                ", sunsetTime='" + sunsetTime + '\'' +
                ", moonriseTime='" + moonriseTime + '\'' +
                ", moonsetTime='" + moonsetTime + '\'' +
                ", moonPhase='" + moonPhase + '\'' +
                '}';
    }
}
