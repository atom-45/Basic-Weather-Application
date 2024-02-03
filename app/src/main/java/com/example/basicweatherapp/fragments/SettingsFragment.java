package com.example.basicweatherapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.adapters.PlacesAdapter;
import com.example.basicweatherapp.databinding.FragmentSettingsBinding;
import com.example.basicweatherapp.interfaces.OnItemClickListener;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.models.User;
import com.example.basicweatherapp.viewmodels.LocationViewModel;
import com.example.basicweatherapp.viewmodels.PlaceViewModel;
import com.example.basicweatherapp.viewmodels.UserViewModel;
import com.example.basicweatherapp.viewmodels.WeatherViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentSettingsBinding settingsBinding;
    private CompositeDisposable compositeDisposable;
    private ProgressBar progressBar;

    private PlaceViewModel placeViewModel;
    private static final String TAG = "SettingsFragment";


    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        settingsBinding = FragmentSettingsBinding.inflate(inflater,container,false);
        return settingsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        compositeDisposable = new CompositeDisposable();
        progressBar = settingsBinding.settingsFragmentProgressBar;

        placeViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        WeatherViewModel weatherViewModel =  new ViewModelProvider(this,
                ViewModelProvider.Factory
                .from(WeatherViewModel.initializer))
                .get(WeatherViewModel.class);

        TextInputEditText nameAndSurnameEditText = settingsBinding.settingFragmentNameAndSurnameTextInputEditText;
        TextInputEditText locationSearchEditText = settingsBinding.settingFragmentLocationSearchTextInputEditText;

        RecyclerView locationRecyclerView = settingsBinding.settingsFragmentLocationSearchRecyclerView;
        locationRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        settingsBinding.settingFragmentNameAndSurnameTextInputLayout.setEndIconOnClickListener(v -> {
            String nameAndSurname = Objects.requireNonNull(nameAndSurnameEditText.getText())
                    .toString();


            if(!nameAndSurname.equals(""))
            {
                Disposable disposableOne = userViewModel.insertUser(new User(nameAndSurname))
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(this::accept)
                        .doOnComplete(this::run)
                        .doOnError(this::error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();

                compositeDisposable.add(disposableOne);

            } else {
                Toast.makeText(v.getContext(), "Please enter your name and surname",
                        Toast.LENGTH_SHORT).show();
            }
        });

        settingsBinding.settingFragmentLocationSearchTextInputLayout.setEndIconOnClickListener(v ->
        {
            String locationName = Objects.requireNonNull(locationSearchEditText.getText())
                    .toString();
            progressBar.setVisibility(View.VISIBLE);
            if(!locationName.equals(""))
            {
                Disposable disposableTwo = weatherViewModel.getPlaces(locationName)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(this::accept)
                        .doOnError(throwable -> {
                            Log.e(TAG, "Get Places: ",throwable );
                            compositeDisposable.dispose();})
                        .doOnComplete(() -> {
                            Log.d(TAG, "onViewCreated: Action task completed");})
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(places -> {
                            progressBar.setVisibility(View.GONE);
                            if(places!=null){
                                PlacesAdapter placesAdapter = new PlacesAdapter(places);
                                placesAdapter.setOnItemClickListener(this);
                                locationRecyclerView.setAdapter(placesAdapter);

                            } else {
                                Toast.makeText(getContext(), "The searched location is " +
                                        "unavailable or does not exist. Try searching another" +
                                        " location", Toast.LENGTH_SHORT).show();
                            }
                        });

                compositeDisposable.add(disposableTwo);
            }
        });

        settingsBinding.settingsFragmentLeftArrowImageView.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    @Override
    public void onItemClick(Place place) {


        Disposable placeDisposable = placeViewModel.addPlace(place)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::accept)
                .doOnError(this::error)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    Log.d(TAG, "onItemClick: Location Saved");
                    Toast.makeText(getContext(), "Location saved!", Toast.LENGTH_SHORT).show();
                    compositeDisposable.dispose();})
                .subscribe();

        compositeDisposable.add(placeDisposable);

    }

    private void accept(Disposable disposable) {
        if (!disposable.isDisposed()) compositeDisposable.add(disposable);
    }

    private void error(Throwable throwable) {
        Log.e(TAG, "onViewCreated/onItemClick: ", throwable);
        compositeDisposable.dispose();
    }

    private void run() {
        Log.d(TAG, "onViewCreated: Action task completed");
        compositeDisposable.dispose();
    }
}