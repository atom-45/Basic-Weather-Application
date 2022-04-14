package com.example.basicweatherapp.repositories;



import androidx.annotation.NonNull;

import com.example.basicweatherapp.models.Constants;
import com.example.basicweatherapp.models.Current;
import com.example.basicweatherapp.models.ForecastDay;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.networking.APIClient;
import com.example.basicweatherapp.networking.APIService;
import com.example.basicweatherapp.responses.AstronomyResponse;
import com.example.basicweatherapp.responses.CurrentResponse;
import com.example.basicweatherapp.responses.ForecastResponse;
import com.example.basicweatherapp.responses.LocationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;


public class WeatherRepository {

    private final APIService apiService;

    public WeatherRepository(){
        apiService = APIClient.getRetrofit().create(APIService.class);
    }

    /**
     * @param location String location of a name to be searched or queried.
     * @return an Observable of a deserialized java object List<Place>
     */
    public Observable<List<Place>> getPlaces(String location){
        return apiService.getPlaces(Constants.API_KEY,location);
    }

    //The function below has a lambda function which combines two observables.
    /**
     * @param list1 an Observable wrapping List<Place> object.
     * @param list2 an Observable wrapping List<Place> object.
     * @return an Observable of the combined inputted Observables list1 and list2
     */
    public Observable<List<Place>> getCombinedPlaces(Observable<List<Place>> list1,
                                                     Observable<List<Place>> list2){

        return Observable.combineLatest(list1,list2,
                (placesList1, placesList2) -> {
                    final List<Place> combinedPlaceList = new ArrayList<>();
                    combinedPlaceList.addAll(placesList1);
                    combinedPlaceList.addAll(placesList2);
                    return combinedPlaceList;
                });

    }

    /**
     *
     * @param forecastResponse1 first forecast response of the first location.
     * @param forecastResponse2 second forecast response of the second location.
     * @return A single arraylist that has both ForecastDays of the responses
     */

    public Observable<List<ForecastDay>> getCombinedForecasts(
            Observable<ForecastResponse> forecastResponse1,
            Observable<ForecastResponse> forecastResponse2){

        return Observable.combineLatest(forecastResponse1,forecastResponse2,
                (response1,response2) -> {
            final List<ForecastDay> combinedForecastList = new ArrayList<>();

            combinedForecastList.addAll(response1.getForecast().getForecastDays());
            combinedForecastList.addAll(response2.getForecast().getForecastDays());
            return combinedForecastList;
        });

    }

    /**
     * @param days numbers of days to be forecasted (up to 3 days)
     * @param location String location of a place to be queried/searched.
     * @return an Observable of a deserialized  ForecastResponse java object
     */
    public Observable<ForecastResponse> getForecasts(Integer days, String location){
        return apiService.getForecast(Constants.API_KEY,days,location,"no");
    }

    /**
     * @param location String location of a name to be searched or queried.
     * @return an Observable of a deserialized CurrentResponse java object
     */
    public Observable<CurrentResponse> getCurrentWeather(String location){
        return apiService.getCurrentWeather(Constants.API_KEY,location,"no");
    }

    /**
     * @param location String location of a name to be searched or queried.
     * @return a Single of a deserialized LocationResponse java object.
     */
    public Single<LocationResponse> getLocation(String location){
        return apiService.getLocation(Constants.API_KEY,location);
    }

    /**
     * @param options a Map object which a String location and date dt in the format
     *                'YYYY-MM-DD' is required.
     * @return an Observable of a deserialized AstronomyResponse java object
     */
    public Observable<AstronomyResponse> getAstronomy(Map<String, String> options){
        return apiService.getAstronomy(Constants.API_KEY,options);
    }

}
