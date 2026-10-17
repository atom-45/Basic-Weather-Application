package com.example.basicweatherapp.data.models;

import java.io.Serializable;

/**
 * Data class representing a raw offline sensor reading received from the Arduino BLE buffer.
 */
public record HistoricalSensorRecord(long sampleMillis, float temperature, float humidity,
                                     float pressure, float altitude) implements Serializable {

}
