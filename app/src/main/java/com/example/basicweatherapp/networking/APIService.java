package com.example.basicweatherapp.networking;

import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.responses.AstronomyResponse;
import com.example.basicweatherapp.responses.CurrentResponse;
import com.example.basicweatherapp.responses.ForecastResponse;
import com.example.basicweatherapp.responses.LocationResponse;
import com.example.basicweatherapp.responses.SearchResponse;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface APIService {

    /**
     * @param apiKey WeatherAPI apiKey to access and retrieve data from the database.
     * @param days Number of days that should be forecasted (Limit: 3)
     * @param location String location of a place to be queried/searched.
     * @param aqiBool A yes or no string which allows aqi to be present in the data.
     * @return an Observable which has a deserialized ForecastResponse java object.
     */
    @GET("forecast.json")
    Observable<ForecastResponse> getForecast(@Query("key") String apiKey,
                                             @Query("days") Integer days,
                                             @Query("q") String location,
                                             @Query("aqi") String aqiBool);

    /**
     * @param apiKey WeatherAPI apiKey to access and retrieve data from the database.
     * @param location accepts a string location name.
     * @param aqiBool A yes or no string which allows aqi to be present in the data.
     * @return an Observable which has a deserialized CurrentResponse java object.
     */
    @GET("current.json")
    Observable<CurrentResponse> getCurrentWeather(@Query("key") String apiKey,
                                                  @Query("q") String location,
                                                  @Query("aqi") String aqiBool);

    /**
     * @param apiKey WeatherAPI apiKey to access and retrieve data from the database.
     * @param location String location of a place to be queried/searched.
     * @return an Observable which has a deserialized java object
     *         of the list of places found near the searched location
     */
    @GET("search.json")
    Observable<List<Place>> getPlaces(@Query("key") String apiKey,
                                      @Query("q") String location);


    /**
     * @param apiKey WeatherAPI apiKey to access and retrieve data from the database.
     * @param options Requires a Map object which has
     *                two parameters to be sent, location and date (in the form of YYYY-MM-DD)
     * @return an Observable which has a deserialized AstronomyResponse java object.
     */
    @GET("astronomy.json")
    Observable<AstronomyResponse> getAstronomy(@Query("key") String apiKey,
                                               @QueryMap Map<String, String> options);

    /**
     * @param apiKey WeatherAPI apiKey to access and retrieve data from the database.
     * @param location String location of a place to be searched
     * @return a Single which has a deserialized LocationResponse java object.
     */
    @GET("timezone.json")
    Single<LocationResponse> getLocation(@Query("key") String apiKey, @Query("q") String location);
}
