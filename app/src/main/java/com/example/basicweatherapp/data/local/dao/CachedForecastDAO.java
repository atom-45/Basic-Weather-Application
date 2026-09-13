package com.example.basicweatherapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.data.models.CachedForecast;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface CachedForecastDAO {

    @Query("SELECT * FROM cached_forecast WHERE id = 'last_forecast'")
    Observable<CachedForecast> getCachedForecast();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertCachedForecast(CachedForecast cachedForecast);
}
