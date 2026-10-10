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

    @Query("UPDATE thermodynamic_predictions SET verification_status = :status, actual_outcome = :outcome, verified_timestamp = :verifiedTimestamp " +
           "WHERE storm_event_id = :eventId " +
           "AND verification_status = 'PENDING' " +
           "AND window_start_millis <= :windowEnd " +
           "AND window_end_millis >= :windowStart")
    Completable verifyTargetedEventGroup(String eventId, String status, String outcome, String verifiedTimestamp, long windowStart, long windowEnd);

    @Query("UPDATE thermodynamic_predictions SET verification_status = :status, actual_outcome = :outcome, verified_timestamp = :verifiedTimestamp WHERE id = :id")
    Completable verifySinglePrediction(int id, String status, String outcome, String verifiedTimestamp);

    @Query("UPDATE thermodynamic_predictions SET verification_status = 'EXPIRED' WHERE window_end_millis < :currentTimeMillis AND verification_status = 'PENDING'")
    Completable markExpiredPredictions(long currentTimeMillis);

    @Query("SELECT * FROM thermodynamic_predictions " +
           "WHERE verification_status = 'PENDING' " +
           "AND notified_45m = 0 " +
           "AND window_start_millis > 0 " +
           "AND (window_start_millis - 40 * 60 * 1000) <= :currentTimeMillis " +
           "AND window_end_millis >= :currentTimeMillis " +
           "ORDER BY id DESC")
    List<ThermodynamicPrediction> getPredictionsNeeding45mAlert(long currentTimeMillis);

    @Query("UPDATE thermodynamic_predictions SET notified_45m = 1 WHERE id = :id")
    Completable mark45mAlertSent(int id);
    
    @Query("SELECT * FROM thermodynamic_predictions WHERE storm_event_id = :eventId ORDER BY id ASC")
    List<ThermodynamicPrediction> getPredictionsByEventId(String eventId);

    @Query("SELECT * FROM thermodynamic_predictions WHERE original_timestamp >= :startDate AND original_timestamp <= :endDate ORDER BY original_timestamp ASC")
    List<ThermodynamicPrediction> getPredictionsByRange(String startDate, String endDate);
}
