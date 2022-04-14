package com.example.basicweatherapp.activities;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.adapters.PlacesAdapter;
import com.example.basicweatherapp.models.Constants;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.viewmodels.WeatherViewModel;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchResultsActivity extends AppCompatActivity {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final static String TAG = "SearchResultsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView headerTextView = findViewById(R.id.header_title_textView);
        ProgressBar progressSearchBar = findViewById(R.id.search_activity_progressbar);

        RecyclerView placesRecyclerView = findViewById(R.id.placeSearchRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        placesRecyclerView.setLayoutManager(linearLayoutManager);


        WeatherViewModel weatherViewModel = new ViewModelProvider(this)
                .get(WeatherViewModel.class);

        //Stupid mistakes, The below is what I have written.
        //String[] locations = getIntent().getStringArrayExtra(Constants.LOCATION);

        //Below is the correct method of getting arrays.
        String[] locations = getIntent().getExtras().getStringArray(Constants.LOCATION);
        Log.d(TAG,"getIntent: "+ Arrays.toString(locations));

        /*String formattedText = getString(R.string.header_text,locations[0],locations[1]);
         headerTextView.setText(formattedText);**/

        progressSearchBar.setVisibility(View.VISIBLE);
        progressSearchBar.setIndeterminate(true);


        String formattedText;
        if(locations.length==1){
            formattedText = getString(R.string.header_text_2,locations[0]);
            headerTextView.setText(formattedText);
            weatherViewModel.getPlaces(locations[0])
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Place>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(@NonNull List<Place> places) {
                            Log.d(TAG, "onNext: Number of places found "+places.size());
                            Log.d(TAG,"onNext: "+places);
                            placesRecyclerView.setAdapter(new PlacesAdapter(places));
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getApplicationContext(),
                                    "Cannot retrieve/display " +
                                            "nearby places", Toast.LENGTH_SHORT).show();
                            Log.e(TAG,e.toString());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG,"onComplete: Nearby places at" +
                                    " this location has been found");
                            progressSearchBar.setVisibility(View.INVISIBLE);
                            progressSearchBar.setIndeterminate(false);
                            compositeDisposable.clear();
                        }
                    })
            ;

        } else {
            formattedText = getString(R.string.header_text,locations[0],locations[1]);
            headerTextView.setText(formattedText);
            weatherViewModel.getCombinedPlaces(
                    weatherViewModel.getPlaces(locations[0]),
                    weatherViewModel.getPlaces(locations[1]))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Place>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(@NonNull List<Place> places) {
                            Log.d(TAG, "onNext: Number of places found "+places.size());
                            Log.d(TAG,"onNext: "+places);
                            placesRecyclerView.setAdapter(new PlacesAdapter(places));
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(getApplicationContext(),
                                    "Cannot retrieve/display " +
                                            "nearby places", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onError: "+e.toString());
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG,"onComplete: Search of nearby places is complete.");
                            progressSearchBar.setVisibility(View.INVISIBLE);
                            progressSearchBar.setIndeterminate(false);
                            compositeDisposable.clear();
                        }
                    })
            ;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}