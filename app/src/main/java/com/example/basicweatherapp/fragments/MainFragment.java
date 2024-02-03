package com.example.basicweatherapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.basicweatherapp.R;
import com.example.basicweatherapp.adapters.ForecastDayAdapter;
import com.example.basicweatherapp.adapters.HourAdapter;
import com.example.basicweatherapp.databinding.FragmentMainBinding;
import com.example.basicweatherapp.models.Astro;
import com.example.basicweatherapp.models.Current;
import com.example.basicweatherapp.models.ForecastDay;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.models.User;
import com.example.basicweatherapp.responses.ForecastResponse;
import com.example.basicweatherapp.viewmodels.PlaceViewModel;
import com.example.basicweatherapp.viewmodels.UserViewModel;
import com.example.basicweatherapp.viewmodels.WeatherViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentMainBinding mainBinding;
    private WeatherViewModel weatherViewModel;
    private PlaceViewModel placeViewModel;
    private UserViewModel userViewModel;
    private CompositeDisposable compositeDisposable;



    private TextView locationNameTextView;
    private TextView introTextView;
    private TextView dateAndTimeTextView;
    private TextView temperatureTextView;
    private TextView moonPhaseTextView;
    private TextView weatherDescriptionTextView;
    private TextView feelsLikeTempTextView;
    private TextView windAndSpeedTextView;
    private TextView humdityTextView;
    private TextView rainProbTextView;
    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private TextView moonriseTimeTextView;
    private TextView moonsetTimeTextView;
    private RecyclerView hourlyRecyclerView;
    private RecyclerView forecastRecyclerView;

     private ImageView currentWeatherIcon;
    private ImageView currentMoonPhaseIcon;

    private final String DEFAULT_LOCATION = "Vosloorus";

    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainBinding = FragmentMainBinding.inflate(inflater, container, false);
        return mainBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        compositeDisposable = new CompositeDisposable();

        weatherViewModel = new ViewModelProvider(this, ViewModelProvider.Factory
                .from(WeatherViewModel.initializer))
                .get(WeatherViewModel.class);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        placeViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);





       /*Disposable disposable = Observable.zip(placeObservable, userObservable,
                        (BiFunction<List<Place>, List<User>, Object>) (places, users) ->
                        {
                            Map<String, List<?>> listMap = new HashMap<>();
                            listMap.put("places", places);
                            listMap.put("users", users);
                            return listMap;})
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::subscribe)
                .doOnNext(o -> Log.d(TAG, "accept: "+o))
                .doOnError(this::error)
                .doOnComplete(this::complete)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();**/

        MainFragmentArgs mainFragmentArgs = MainFragmentArgs.fromBundle(requireArguments());

        String placeName = mainFragmentArgs.getPlaceName();
        String placeRegion = mainFragmentArgs.getRegion();
        String placeCountry = mainFragmentArgs.getCountry();

        Log.d(TAG, "onViewCreated: "+placeName+" "+placeName+" "+placeCountry);


        showWeatherOfLocation(placeName,placeRegion,placeCountry);


        mainBinding.mainFragmentSettingsImageView.setOnClickListener(v -> {
            @NonNull NavDirections mainFragmentDirections =
                    MainFragmentDirections.actionMainFragmentToSettingsFragment();

            Navigation.findNavController(v).navigate(mainFragmentDirections);

        });

        mainBinding.mainFragmentLocationIconImageView.setOnClickListener(v ->
        {
            NavDirections navDirections = MainFragmentDirections
                    .actionMainFragmentToLocationSelectionFragment();

            Navigation.findNavController(v).navigate(navDirections);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void showWeatherOfLocation(String placeName, String region, String country) {

        locationNameTextView = mainBinding.mainFragmentLocationNameTextView;
        introTextView = mainBinding.mainFragmentIntroductionTextView;
        dateAndTimeTextView = mainBinding.mainFragmentCurrentDateAndTimeTextView;
        temperatureTextView = mainBinding.mainFragmentCurrentWeatherTempTextView;
        moonPhaseTextView = mainBinding.mainFragmentMoonShapePhaseTextView;
        weatherDescriptionTextView = mainBinding.mainFragmentCurrentWeatherDescriptionTextView;
        feelsLikeTempTextView = mainBinding.mainFragmentCurrentWeatherFeelsLikeTextView;
        windAndSpeedTextView = mainBinding.mainFragmentCWWindSpeedAndDirectionTextView;
        humdityTextView = mainBinding.mainFragmentCurrentWeatherHumidityTextView;
        rainProbTextView = mainBinding.mainFragmentCurrentWeatherRainProbabilityTextView;
        sunriseTextView = mainBinding.mainFragmentSunriseTextView;
        sunsetTextView = mainBinding.mainFragmentSunsetTextView;
        moonriseTimeTextView = mainBinding.mainFragmentMoonriseTextView;
        moonsetTimeTextView = mainBinding.mainFragmentMoonsetTextView;

        LinearLayoutManager linearLayoutManagerOne = new LinearLayoutManager(getContext());
        linearLayoutManagerOne.setOrientation(LinearLayoutManager.HORIZONTAL);

        LinearLayoutManager linearLayoutManagerTwo = new LinearLayoutManager(getContext());
        linearLayoutManagerTwo.setOrientation(LinearLayoutManager.HORIZONTAL);

        hourlyRecyclerView = mainBinding.mainFragmentHourlyWeatherRecyclerView;
        hourlyRecyclerView.setLayoutManager(linearLayoutManagerOne);

        forecastRecyclerView = mainBinding.mainFragmentDailyForecastWeatherRecyclerView;
        forecastRecyclerView.setLayoutManager(linearLayoutManagerTwo);

        currentWeatherIcon = mainBinding.mainFragmentCurrentWeatherIconImageView;
        currentMoonPhaseIcon = mainBinding.mainFragmentMoonShapeImageView;

        introTextView.setText(R.string.location_selection);
        locationNameTextView.setText(String.format("%s (Default)", DEFAULT_LOCATION));

        if(!placeName.equals("")&&
                !region.equals("")&&
                !country.equals("")) {

            Disposable weatherDisposable = userViewModel.getAllUsers()
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::onSubscribe)
                    .flatMap(users -> {
                        if(users!=null && users.size()>0){
                            introTextView.setText(String.format("Have a look at today's weather %s ",
                                            users.get(users.size()-1).getName_and_surname()));
                            locationNameTextView
                                    .setText(String.format("%1s, %2s, %3s",placeName,
                                            region, country));

                        }
                        return weatherViewModel.getForecasts(3, placeName);
                    })
                    .doOnError(this::onError)
                    .doOnComplete(this::onComplete)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setWeather);


            compositeDisposable.add(weatherDisposable);

        } else {
            Disposable disposable = Observable.zip(placeViewModel.getAllPlaces(),
                            userViewModel.getAllUsers(), (places, users) ->
                            {
                                List<Object> combinedArrayList = new ArrayList<>();
                                combinedArrayList.addAll(places);
                                combinedArrayList.addAll(users);

                                return combinedArrayList;

                            })
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(this::onSubscribe)
                    .flatMap(objects ->
                    {
                        String locationName = DEFAULT_LOCATION;

                        if(objects != null&&objects.size()>0)
                        {
                            Place placeOne = (Place) objects.get(0);
                            User user = (User) objects.get(objects.size()-1);

                            locationName = placeOne.getName()+", "+placeOne.getRegion()+", "
                                    +placeOne.getCountry();

                            locationNameTextView.setText(locationName);
                            introTextView.setText(String.format("Have a look at today's weather %s ",
                                    user.getName_and_surname()));

                        }
                        return weatherViewModel.getForecasts(3, locationName);
                    })
                    .doOnError(this::onError)
                    .doOnComplete(this::onComplete)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setWeather);

            compositeDisposable.add(disposable);

        }
    }

    private void onSubscribe(Disposable disposable) {
        if(!disposable.isDisposed())
            compositeDisposable.add(disposable);
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "error: ", throwable);
        compositeDisposable.dispose();
    }

    private void onComplete() {
        Log.d(TAG, "onComplete: Complete!");
        compositeDisposable.dispose();
    }

    private void forecastResponse(ForecastResponse forecastResponse){

    }

    private String convertLocalDate(String date)
    {
        String[] dateSplit = date.split(" ");

        LocalDate localDate = LocalDate.parse(dateSplit[0]);
        String month = localDate.getMonth().toString().toUpperCase().charAt(0)
                +localDate.getMonth().toString().toLowerCase().substring(1);

        return String.format("%1s %2s %3s",localDate.getDayOfMonth(), month,
                localDate.getYear());
    }

    private void setMoonPhase(String moonPhase, ImageView imageView)
    {
        switch (moonPhase)
        {
            case "New Moon" -> imageView.setImageResource(R.drawable.new_moon);
            case "Waxing Crescent" -> imageView.setImageResource(R.drawable.waxing_crescent);
            case "First Quarter" -> imageView.setImageResource(R.drawable.first_quarter);
            case "Waxing Gibbous" -> imageView.setImageResource(R.drawable.waxing_gibbous);
            case "Full Moon" -> imageView.setImageResource(R.drawable.full_moon);
            case "Waning Moon" -> imageView.setImageResource(R.drawable.waning_gibbous);
            case "Last Quarter" -> imageView.setImageResource(R.drawable.last_quarter);
            default -> imageView.setImageResource(R.drawable.waning_crescent);
        }
    }

    private void setWeather(ForecastResponse forecastResponse) {
        if (forecastResponse != null) {
            ForecastDay forecastDay = forecastResponse.getForecast()
                    .getForecastDays().get(0);
            Current current = forecastResponse.getCurrent();
            Astro astro = forecastDay.getAstro();

            dateAndTimeTextView.setText(convertLocalDate(current.getLastUpdated()));

            temperatureTextView
                    .setText(String.format("%s°", current.getTemp_c()));

            Glide.with(currentWeatherIcon.getContext()).
                    load("https:" + current.getCondition().getIconURL()).
                    into(currentWeatherIcon);

            weatherDescriptionTextView.setText(current.getCondition().getText());

            feelsLikeTempTextView.setText(String.format(" Feels like %s°",
                    current.getFeelslikeTempC()));

            windAndSpeedTextView.setText(String.format("Wind Spe. & Dir. %1s kph %2s",
                    current.getWindSpeed(), current.getWindDirection()));

            humdityTextView.setText(String.format("Humidity %s%%", current.getHumidity()));

            rainProbTextView.setText(String.format("Chance of Rain %s%%",
                    forecastDay.getDay().getRain_probability()));

            moonPhaseTextView.setText(astro.getMoonPhase());
            setMoonPhase(astro.getMoonPhase(), currentMoonPhaseIcon);

            sunriseTextView.setText(String.format("Sunrise at %s", astro.getSunriseTime()));
            sunsetTextView.setText(String.format("Sunset at %s", astro.getSunsetTime()));
            moonriseTimeTextView.setText(String.format("Moonrise at %s", astro.getMoonriseTime()));
            moonsetTimeTextView.setText(String.format("Moonset at %s", astro.getMoonsetTime()));

            hourlyRecyclerView.setAdapter(new HourAdapter(forecastResponse));
            forecastRecyclerView.setAdapter(new ForecastDayAdapter(forecastResponse));

        }
    }
}