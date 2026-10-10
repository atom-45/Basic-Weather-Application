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

    public Completable verifyTargetedPrediction(ThermodynamicPrediction prediction, String outcome) {
        long toleranceMillis = 5 * 60 * 1000; // 5-minute tolerance around arrival window
        long windowStart = prediction.getWindowStartMillis() - toleranceMillis;
        long windowEnd = prediction.getWindowEndMillis() + toleranceMillis;
        String verifiedTimestamp = java.time.LocalDateTime.now().toString();

        return dao.verifyTargetedEventGroup(
                prediction.getStormEventId(),
                "VERIFIED",
                outcome,
                verifiedTimestamp,
                windowStart,
                windowEnd
        ).subscribeOn(Schedulers.io());
    }

    public Completable markExpiredPredictions(long currentTimeMillis) {
        return dao.markExpiredPredictions(currentTimeMillis).subscribeOn(Schedulers.io());
    }

    public List<ThermodynamicPrediction> getPredictionsNeeding45mAlert(long currentTimeMillis) {
        return dao.getPredictionsNeeding45mAlert(currentTimeMillis);
    }

    public Completable mark45mAlertSent(int id) {
        return dao.mark45mAlertSent(id).subscribeOn(Schedulers.io());
    }
}
