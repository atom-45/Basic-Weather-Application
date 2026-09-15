package com.example.basicweatherapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.data.models.PredictionVerification;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PredictionVerificationDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(PredictionVerification verification);

    @Query("SELECT * FROM prediction_verifications ORDER BY timestamp DESC")
    Flowable<List<PredictionVerification>> getAllVerifications();

    @Query("SELECT * FROM prediction_verifications WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp ASC")
    List<PredictionVerification> getVerificationsByRange(String startDate, String endDate);
}
