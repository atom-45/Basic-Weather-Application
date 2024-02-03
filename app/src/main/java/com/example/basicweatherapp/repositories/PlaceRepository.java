package com.example.basicweatherapp.repositories;

import android.app.Application;

import com.example.basicweatherapp.dao.PlaceDAO;
import com.example.basicweatherapp.database.WeatherDatabase;
import com.example.basicweatherapp.models.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class PlaceRepository {

    private final PlaceDAO placeDAO;

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
}
