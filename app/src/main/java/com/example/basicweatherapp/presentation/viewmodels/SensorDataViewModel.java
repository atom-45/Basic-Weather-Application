package com.example.basicweatherapp.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.repositories.SensorDataRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class SensorDataViewModel extends ViewModel {


    private final SensorDataRepository sensorDataRepository;


    @Inject
    public SensorDataViewModel(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    public Observable<List<SensorData>> getAllSensorData() {
        return sensorDataRepository.getAllSensorData();
    }

    public Completable insertSensorData(SensorData sensorData){
        return sensorDataRepository.insertSensorData(sensorData);
    }
}
