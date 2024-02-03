package com.example.basicweatherapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.repositories.PlaceRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class PlaceViewModel extends AndroidViewModel {

    private final PlaceRepository placeRepository;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        this.placeRepository = new PlaceRepository(application);
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
}
