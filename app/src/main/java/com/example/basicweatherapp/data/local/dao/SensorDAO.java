package com.example.basicweatherapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.data.models.SensorData;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface SensorDAO {


    @Query("SELECT * FROM sensor_data ORDER BY date ASC")
    Observable<List<SensorData>> getAllSensorData();

    @Query("SELECT * FROM sensor_data WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    List<SensorData> getSensorDataByRange(String startDate, String endDate);

    @Query("SELECT * FROM sensor_data ORDER BY id DESC LIMIT 2")
    Observable<List<SensorData>> getLastTwoEntries();

    @Query("SELECT * FROM sensor_data ORDER BY id DESC LIMIT 1")
    List<SensorData> getLatestSensorData();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertSensorData(SensorData sensorData);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertAll(List<SensorData> sensorDataList);
}
