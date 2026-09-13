package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.LocationDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.Location;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Singleton
public class LocationRepository {

    private final LocationDAO locationDAO;

    @Inject
    public LocationRepository(Application application) {
        this.locationDAO = WeatherDatabase.getInstance(application).locationDAO();
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
