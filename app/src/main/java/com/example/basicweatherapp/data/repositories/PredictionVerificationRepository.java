package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.PredictionVerificationDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.PredictionVerification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Singleton
public class PredictionVerificationRepository {

    private final PredictionVerificationDAO predictionVerificationDAO;

    @Inject
    public PredictionVerificationRepository(Application application) {
        WeatherDatabase db = WeatherDatabase.getInstance(application);
        predictionVerificationDAO = db.predictionVerificationDAO();
    }

    public Completable insert(PredictionVerification verification) {
        return predictionVerificationDAO.insert(verification);
    }

    public Flowable<List<PredictionVerification>> getAllVerifications() {
        return predictionVerificationDAO.getAllVerifications();
    }

    public List<PredictionVerification> getVerificationsByRange(String startDate, String endDate) {
        return predictionVerificationDAO.getVerificationsByRange(startDate, endDate);
    }
}
