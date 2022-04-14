package com.example.basicweatherapp.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Condition {

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("icon")
    @Expose
    private String iconURL;

    @SerializedName("code")
    @Expose
    private int code;


    public String getText() {
        return text;
    }
    public String getIconURL() {
        return iconURL;
    }
    public int getCode() {
        return code;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condition)) return false;
        Condition condition = (Condition) o;
        return getCode() == condition.getCode() &&
                getText().equals(condition.getText())
                && getIconURL().equals(condition.getIconURL());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getText(), getIconURL(), getCode());
    }

    @NonNull
    @Override
    public String toString() {
        return "Condition{" +
                "text='" + text + '\'' +
                ", iconURL='" + iconURL + '\'' +
                ", code=" + code +
                '}';
    }
}
