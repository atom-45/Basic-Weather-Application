package com.example.basicweatherapp.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.models.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface PlaceDAO {

    @Query("SELECT * FROM places")
    Observable<List<Place>> getAllPlaces();

    @Query("DELETE FROM places")
    Completable deleteAllSavedPlaces();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable addPlace(Place place);


}
