package com.example.basicweatherapp.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.basicweatherapp.data.models.Place;
import com.example.basicweatherapp.data.repositories.PlaceRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class PlaceViewModel extends ViewModel {


    private final PlaceRepository placeRepository;

    @Inject
    public PlaceViewModel(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public Observable<List<Place>> getAllPlaces(){
        return placeRepository.getAllPlaces();
    }

    public Completable deleteAllSavedPlaces(){
        return placeRepository.deleteAllSavedPlaces();
    }

    public Completable addPlace(Place place){
        return placeRepository.addPlace(place);
    }

    public Completable deletePlace(Place place) {
        return placeRepository.deletePlace(place);
    }
}
