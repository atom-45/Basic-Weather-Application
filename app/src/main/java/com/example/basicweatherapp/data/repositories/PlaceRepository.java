package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.PlaceDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.Place;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Singleton
public class PlaceRepository {

    private final PlaceDAO placeDAO;


    @Inject
    public PlaceRepository(Application application){
        this.placeDAO = WeatherDatabase.getInstance(application).placeDAO();
    }

    public Observable<List<Place>> getAllPlaces(){
        return placeDAO.getAllPlaces();
    }

    public Completable deleteAllSavedPlaces(){
        return placeDAO.deleteAllSavedPlaces();
    }

    public Completable addPlace(Place place){
        return placeDAO.addPlace(place);
    }

    public Completable deletePlace(Place place) {
        return placeDAO.deletePlace(place);
    }
}
