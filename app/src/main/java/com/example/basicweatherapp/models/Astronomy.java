package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Astronomy {

    @SerializedName("astro")
    @Expose
    private Astro astro;

    public Astro getAstro() {
        return astro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Astronomy)) return false;
        Astronomy astronomy = (Astronomy) o;
        return getAstro().equals(astronomy.getAstro());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAstro());
    }

    @NonNull
    @Override
    public String toString() {
        return "Astronomy{" +
                "astro=" + astro +
                '}';
    }
}
