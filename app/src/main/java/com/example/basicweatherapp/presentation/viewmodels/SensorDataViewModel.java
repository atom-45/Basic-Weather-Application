package com.example.basicweatherapp.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.basicweatherapp.data.models.PredictionVerification;
import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.models.ThermodynamicPrediction;
import com.example.basicweatherapp.data.repositories.PredictionVerificationRepository;
import com.example.basicweatherapp.data.repositories.SensorDataRepository;
import com.example.basicweatherapp.data.repositories.ThermodynamicPredictionRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;
import com.example.basicweatherapp.physics.StormReport;
import com.example.basicweatherapp.physics.StormThermodynamicsEngine;
import com.example.basicweatherapp.workers.ExportWorker;

import java.util.List;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class SensorDataViewModel extends ViewModel {


    private final SensorDataRepository sensorDataRepository;
    private final PredictionVerificationRepository predictionVerificationRepository;
    private final ThermodynamicPredictionRepository thermodynamicPredictionRepository;
    private final Application application;


    @Inject
    public SensorDataViewModel(SensorDataRepository sensorDataRepository, 
                               PredictionVerificationRepository predictionVerificationRepository,
                               ThermodynamicPredictionRepository thermodynamicPredictionRepository,
                               Application application) {
        this.sensorDataRepository = sensorDataRepository;
        this.predictionVerificationRepository = predictionVerificationRepository;
        this.thermodynamicPredictionRepository = thermodynamicPredictionRepository;
        this.application = application;
    }

    public Observable<List<SensorData>> getAllSensorData() {
        return sensorDataRepository.getAllSensorData();
    }

    /**
     * Provides a real-time thermodynamic analysis of the storm based on sensor data trends.
     * 
     * @return An Observable emitting the latest StormReport derived from the First Law of Thermodynamics.
     */
    public Observable<StormReport> getStormAnalysis() {
        return sensorDataRepository.getAllSensorData()
                .filter(list -> list.size() >= 2)
                .map(list -> {
                    SensorData prev = list.get(list.size() - 2);
                    SensorData current = list.get(list.size() - 1);
                    return StormThermodynamicsEngine.analyze(prev, current);
                });
    }

    /**
     * Exposes all unverified predictions that are currently in their "Audit Window" or PENDING.
     */
    public Observable<List<ThermodynamicPrediction>> getUnverifiedPredictions() {
        return thermodynamicPredictionRepository.getUnverifiedPredictions();
    }

    public Completable verifyPrediction(String eventId, String outcome) {
        return thermodynamicPredictionRepository.verifyEventGroup(eventId, outcome);
    }

    public Completable insertSensorData(SensorData sensorData){
        return sensorDataRepository.insertSensorData(sensorData);
    }

    public Completable insertVerification(PredictionVerification verification) {
        return predictionVerificationRepository.insert(verification);
    }

    public void exportData(String startDate, String endDate) {
        Data inputData = new Data.Builder()
                .putString(ExportWorker.KEY_START_DATE, startDate)
                .putString(ExportWorker.KEY_END_DATE, endDate)
                .build();

        OneTimeWorkRequest exportRequest = new OneTimeWorkRequest.Builder(ExportWorker.class)
                .setInputData(inputData)
                .addTag(ExportWorker.TAG)
                .build();

        WorkManager.getInstance(application).enqueue(exportRequest);
    }
}
