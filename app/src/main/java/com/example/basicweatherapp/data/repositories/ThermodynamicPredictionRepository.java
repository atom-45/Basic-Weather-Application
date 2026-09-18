package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.ThermodynamicPredictionDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.ThermodynamicPrediction;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class ThermodynamicPredictionRepository {

    private final ThermodynamicPredictionDAO dao;

    @Inject
    public ThermodynamicPredictionRepository(Application application) {
        this.dao = WeatherDatabase.getInstance(application).thermodynamicPredictionDAO();
    }

    public Completable insert(ThermodynamicPrediction prediction) {
        return dao.insert(prediction).subscribeOn(Schedulers.io());
    }

    public Observable<List<ThermodynamicPrediction>> getLatestPrediction() {
        return dao.getLatestPrediction().subscribeOn(Schedulers.io());
    }

    public Observable<List<ThermodynamicPrediction>> getUnverifiedPredictions() {
        return dao.getUnverifiedPredictions().subscribeOn(Schedulers.io());
    }

    public Completable verifyEventGroup(String eventId, String outcome) {
        return dao.verifyEventGroup(eventId, "VERIFIED", outcome).subscribeOn(Schedulers.io());
    }

    public Completable markExpiredPredictions(long currentTimeMillis) {
        return dao.markExpiredPredictions(currentTimeMillis).subscribeOn(Schedulers.io());
    }
}
