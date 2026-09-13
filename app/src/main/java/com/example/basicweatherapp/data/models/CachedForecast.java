package com.example.basicweatherapp.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_forecast")
public class CachedForecast {

    @PrimaryKey
    @NonNull
    private String id = "last_forecast";

    @ColumnInfo(name = "json_data")
    private String jsonData;

    public CachedForecast(@NonNull String jsonData) {
        this.jsonData = jsonData;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
