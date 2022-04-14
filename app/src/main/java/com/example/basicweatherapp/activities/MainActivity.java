package com.example.basicweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.basicweatherapp.R;
import com.example.basicweatherapp.adapters.ForecastAdapter2;
import com.example.basicweatherapp.models.Constants;
import com.example.basicweatherapp.responses.ForecastResponse;
import com.example.basicweatherapp.viewmodels.WeatherViewModel;

import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final static String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG,"It has been accepted!");

        } else {
            int requestCode = 348;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, requestCode);
        }

        WeatherViewModel weatherViewModel = new ViewModelProvider(this)
                .get(WeatherViewModel.class);
        ViewPager2 viewPager2 = findViewById(R.id.weatherViewPager);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);


        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(5));
        compositePageTransformer.addTransformer((page, position) -> {
            page.setScaleX(0.75f);
            page.setScaleY(0.90f);
            float r = 1 - Math.abs(position);
        });
        /*compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setPadding(10,10,10,10);
            page.setScaleX(0.70f);
            page.setScaleY(0.85f);
            page.animate();
        });**/

        viewPager2.setPageTransformer(compositePageTransformer);


        RadioButton currentRadioButton = findViewById(R.id.current_radiobutton);
        RadioButton forecastRadioButton = findViewById(R.id.forecast_radiobutton);
        RadioButton searchRadioButton = findViewById(R.id.search_radiobutton);
        EditText days = findViewById(R.id.days_editText);
        EditText searchEditText = findViewById(R.id.search_editText);
        ProgressBar progressBar = findViewById(R.id.progressBar_circular);


        findViewById(R.id.search_imageview).setOnClickListener(view -> {

            String[] locations = searchEditText.getText()
                    .toString().toLowerCase().trim().split(",");
            Log.e(TAG, Arrays.toString(locations));


            if(currentRadioButton.isChecked()){
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);

                weatherViewModel.getForecasts(1,locations[0])
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ForecastResponse>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(@NonNull ForecastResponse forecastResponse) {
                                Log.d(TAG, "onNext() returned: " +
                                        forecastResponse);
                                viewPager2.setAdapter(new ForecastAdapter2(forecastResponse,
                                        Glide.with(MainActivity.this),"current"));

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Log.e(TAG, "onError: ",e);
                                Toast.makeText(MainActivity.this,
                                        "Error in loading/retrieving current weather data",
                                        Toast.LENGTH_SHORT).show();
                                progressBar.setIndeterminate(false);
                                progressBar.setVisibility(View.INVISIBLE);

                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: Forecast for today founded");
                                progressBar.setIndeterminate(false);
                                progressBar.setVisibility(View.INVISIBLE);
                                compositeDisposable.clear();
                            }
                        })
                ;
            } else if(forecastRadioButton.isChecked()){
                String dayString = days.getText().toString();

                if (dayString.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Enter a number from 1 to 3", Toast.LENGTH_SHORT).show();
                } else {
                    int dayNumber = Integer.parseInt(dayString);
                    if((dayNumber>3)||(dayNumber)==0){
                        Toast.makeText(getApplicationContext(),
                                "Enter a number from 1 to 3", Toast.LENGTH_SHORT).show();
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setIndeterminate(true);
                        weatherViewModel.getForecasts(dayNumber,locations[0])
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<ForecastResponse>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        compositeDisposable.add(d);
                                    }

                                    @Override
                                    public void onNext(@NonNull ForecastResponse forecastResponse) {
                                        Log.d(TAG, "onNext() returned: " + forecastResponse);
                                        viewPager2.setAdapter(new ForecastAdapter2(forecastResponse,
                                                Glide.with(MainActivity.this),"forecast"));
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        Log.e(TAG, "onError: ",e);
                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.d(TAG, "onComplete: "
                                                +locations[0]+" Forecasts complete");
                                        progressBar.setIndeterminate(false);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        compositeDisposable.clear();
                                    }
                                })
                        ;
                    }

                }

            } else if(searchRadioButton.isChecked()){

                if(locations.length==1||locations.length==2){
                    Log.d(TAG, "Locations: "+Arrays.toString(locations));
                    startActivity(new Intent(getApplicationContext(),SearchResultsActivity.class)
                            .putExtra(Constants.LOCATION,locations));

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter a location/two locations you" +
                                    "want to find nearby places",Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getApplicationContext(),
                        "No option selected",Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}