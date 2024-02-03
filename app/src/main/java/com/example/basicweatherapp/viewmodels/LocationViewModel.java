package com.example.basicweatherapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.basicweatherapp.models.Location;
import com.example.basicweatherapp.repositories.LocationRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class LocationViewModel extends AndroidViewModel {

    private final LocationRepository locationRepository;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        this.locationRepository = new LocationRepository(application);
    }

    public Observable<List<Location>> getAllSavedLocations(){
        return locationRepository.getAllSavedLocations();
    }

    public Completable deleteAllSavedLocations(){
        return locationRepository.deleteAllSavedLocations();
    }

    public Completable saveLocation(Location location){
        return locationRepository.saveLocation(location);
    }
}
