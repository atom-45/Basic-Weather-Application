package com.example.basicweatherapp.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.models.Location;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface LocationDAO {

    @Query("SELECT * FROM locations")
    Observable<List<Location>> getAllSavedLocations();

    @Query("DELETE FROM locations")
    Completable deleteAllSavedLocations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable saveLocation(Location location);
}
