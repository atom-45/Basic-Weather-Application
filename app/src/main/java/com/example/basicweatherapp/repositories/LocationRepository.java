package com.example.basicweatherapp.repositories;

import android.app.Application;

import com.example.basicweatherapp.dao.LocationDAO;
import com.example.basicweatherapp.database.WeatherDatabase;
import com.example.basicweatherapp.models.Location;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class LocationRepository {

    private final LocationDAO locationDAO;

    public LocationRepository(Application application){
        this.locationDAO = WeatherDatabase.getInstance(application
                .getApplicationContext()).locationDAO();
    }

    public Observable<List<Location>> getAllSavedLocations(){
        return locationDAO.getAllSavedLocations();
    }

    public Completable deleteAllSavedLocations(){
        return locationDAO.deleteAllSavedLocations();
    }

    public Completable saveLocation(Location location){
        return locationDAO.saveLocation(location);
    }
}
