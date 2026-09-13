package com.example.basicweatherapp.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;


@Entity(tableName = "sensor_data")
public class SensorData implements Serializable {


    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "date") private String date;
    @ColumnInfo(name = "humidity") private float humidity;
    @ColumnInfo(name = "temperature") private float temperature;
    @ColumnInfo(name = "pressure") private float pressure;
    @ColumnInfo(name = "altitude") private float altitude;

    public SensorData(String date, float humidity, float temperature,
                      float pressure, float altitude)
    {
        this.date = date;
        this.humidity = humidity;
        this.temperature = temperature;
        this.pressure = pressure;
        this.altitude = altitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SensorData that)) return false;
        return getId() == that.getId() && Float.compare(getHumidity(),
                that.getHumidity()) == 0 && Float.compare(getTemperature(),
                that.getTemperature()) == 0 && Float.compare(getPressure(),
                that.getPressure()) == 0 && Float.compare(getAltitude(),
                that.getAltitude()) == 0 && Objects.equals(getDate(), that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getHumidity(),
                getTemperature(), getPressure(), getAltitude());
    }

    @NonNull
    @Override
    public String toString()
    {
        return "SensorData{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", humidity=" + humidity +
                ", temperature=" + temperature +
                ", pressure=" + pressure +
                ", altitude=" + altitude +
                '}';
    }
}
