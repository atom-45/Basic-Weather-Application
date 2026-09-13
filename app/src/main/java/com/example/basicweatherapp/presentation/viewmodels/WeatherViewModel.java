package com.example.basicweatherapp.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.example.basicweatherapp.di.application.WeatherApplication;
import com.example.basicweatherapp.data.models.Place;
import com.example.basicweatherapp.data.repositories.WeatherRepository;
import com.example.basicweatherapp.data.remote.responses.AstronomyResponse;
import com.example.basicweatherapp.data.remote.responses.CurrentResponse;
import com.example.basicweatherapp.data.remote.responses.ForecastResponse;
import com.example.basicweatherapp.data.remote.responses.LocationResponse;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class WeatherViewModel extends ViewModel {


    private final WeatherRepository weatherRepository;


    @Inject
    public WeatherViewModel(WeatherRepository weatherRepository) {
      this.weatherRepository = weatherRepository;
    }

    public Observable<List<Place>> getPlaces(String location){
        return weatherRepository.getPlaces(location);
    }

    public Observable<CurrentResponse> getCurrentWeather(String location){
        return weatherRepository.getCurrentWeather(location);
    }

    public Observable<ForecastResponse> getForecasts(Integer days, String location){
        return weatherRepository.getForecasts(days,location);
    }

    public Observable<AstronomyResponse> getAstronomy(Map<String, String> options){
        return weatherRepository.getAstronomy(options);
    }

    public Observable<List<Place>> getCombinedPlaces(@NonNull Observable<List<Place>> listObservable,
                                                    @NonNull Observable<List<Place>> listObservable2)
    {
        if(listObservable.equals(listObservable2)){
            return null;
        }
        return weatherRepository.getCombinedPlaces(listObservable,listObservable2);
    }

    public Single<LocationResponse> getLocation(String location){
        return weatherRepository.getLocation(location);
    }

    public Observable<ForecastResponse> getCachedForecast() {
        return weatherRepository.getCachedForecast();
    }

    public Completable cacheForecast(ForecastResponse response) {
        return weatherRepository.cacheForecast(response);
    }

}
