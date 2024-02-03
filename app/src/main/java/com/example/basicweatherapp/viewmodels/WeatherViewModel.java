package com.example.basicweatherapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.example.basicweatherapp.application.WeatherApplication;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.repositories.WeatherRepository;
import com.example.basicweatherapp.responses.AstronomyResponse;
import com.example.basicweatherapp.responses.CurrentResponse;
import com.example.basicweatherapp.responses.ForecastResponse;
import com.example.basicweatherapp.responses.LocationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import kotlin.jvm.functions.Function1;

public class WeatherViewModel extends AndroidViewModel {

    private final WeatherRepository weatherRepository;

    @Inject
    public WeatherViewModel(@NonNull Application application, WeatherRepository weatherRepository) {
        super(application);
        this.weatherRepository = weatherRepository;
    }

    public static final ViewModelInitializer<WeatherViewModel> initializer =
            new ViewModelInitializer<>(WeatherViewModel.class, creationExtras ->
            {
                WeatherApplication application = new WeatherApplication();
                return  new WeatherViewModel(application,
                        application.applicationComponent.weatherRepository());

            });

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

}
