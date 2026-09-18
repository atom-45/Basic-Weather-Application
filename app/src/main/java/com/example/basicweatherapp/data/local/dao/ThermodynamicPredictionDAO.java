package com.example.basicweatherapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.data.models.ThermodynamicPrediction;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface ThermodynamicPredictionDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(ThermodynamicPrediction prediction);

    @Query("SELECT * FROM thermodynamic_predictions ORDER BY id DESC LIMIT 1")
    Observable<List<ThermodynamicPrediction>> getLatestPrediction();

    @Query("SELECT * FROM thermodynamic_predictions WHERE verification_status = 'PENDING' OR verification_status = 'ACTIVE' ORDER BY id DESC")
    Observable<List<ThermodynamicPrediction>> getUnverifiedPredictions();

    @Query("UPDATE thermodynamic_predictions SET verification_status = :status, actual_outcome = :outcome WHERE storm_event_id = :eventId")
    Completable verifyEventGroup(String eventId, String status, String outcome);

    @Query("UPDATE thermodynamic_predictions SET verification_status = 'EXPIRED' WHERE window_end_millis < :currentTimeMillis AND verification_status = 'PENDING'")
    Completable markExpiredPredictions(long currentTimeMillis);
    
    @Query("SELECT * FROM thermodynamic_predictions WHERE storm_event_id = :eventId ORDER BY id ASC")
    List<ThermodynamicPrediction> getPredictionsByEventId(String eventId);

    @Query("SELECT * FROM thermodynamic_predictions WHERE original_timestamp >= :startDate AND original_timestamp <= :endDate ORDER BY original_timestamp ASC")
    List<ThermodynamicPrediction> getPredictionsByRange(String startDate, String endDate);
}
