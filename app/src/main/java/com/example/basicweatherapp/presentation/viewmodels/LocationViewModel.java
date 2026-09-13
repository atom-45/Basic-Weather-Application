package com.example.basicweatherapp.presentation.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.basicweatherapp.data.models.Location;
import com.example.basicweatherapp.data.repositories.LocationRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class LocationViewModel extends ViewModel {


    private final LocationRepository locationRepository;

    @Inject
    public LocationViewModel(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;

    }

    public Observable<List<Location>> getAllSavedLocations() {
        return locationRepository.getAllSavedLocations();
    }

    public Completable deleteAllSavedLocations() {
        return locationRepository.deleteAllSavedLocations();
    }

    public Completable saveLocation(Location location) {
        return locationRepository.saveLocation(location);
    }
}
