package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.SensorDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.SensorData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class SensorDataRepository {

    private final SensorDAO sensorDAO;


    @Inject
    public SensorDataRepository(Application application) {
        this.sensorDAO = WeatherDatabase.getInstance(application).sensorDAO();
    }

    public Observable<List<SensorData>> getAllSensorData(){
        return sensorDAO.getAllSensorData();
    }

    public Completable insertSensorData(SensorData sensorData){
        return sensorDAO.insertSensorData(sensorData);
    }

    public Completable insert(SensorData sensorData){
        return sensorDAO.insertSensorData(sensorData)
                .subscribeOn(Schedulers.io());
    }
}
