package com.example.basicweatherapp;

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

import com.example.basicweatherapp.adapters.LocationSelectionAdapter;
import com.example.basicweatherapp.databinding.FragmentLocationSelectionBinding;
import com.example.basicweatherapp.databinding.FragmentSettingsBinding;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.viewmodels.PlaceViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationSelectionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentLocationSelectionBinding locationSelectionBinding;
    private CompositeDisposable compositeDisposable;
    private PlaceViewModel placeViewModel;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private static final String TAG = "LocationSelectionFragment";

    public LocationSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationSelectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationSelectionFragment newInstance(String param1, String param2) {
        LocationSelectionFragment fragment = new LocationSelectionFragment();
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
        locationSelectionBinding = FragmentLocationSelectionBinding.inflate(inflater, container,false);
        return locationSelectionBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        compositeDisposable = new CompositeDisposable();
        progressBar = locationSelectionBinding.locationSelectionFragmentProgressBar;
        placeViewModel = new ViewModelProvider(this).get(PlaceViewModel.class);

        recyclerView = locationSelectionBinding.locationSelectionFragmentRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        Disposable disposable = placeViewModel.getAllPlaces()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(this::onSubscribe)
                .doOnError(this::onError)
                .doOnComplete(this::conComplete)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSetLocationAdapter);

        locationSelectionBinding.locationSelectionFragmentLeftArrowImageView.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void onSubscribe(Disposable disposable1) {
        if (!disposable1.isDisposed()) {
            compositeDisposable.add(disposable1);
        }
    }

    private void onError(Throwable throwable) {
        Log.e(TAG, "onError: ", throwable);
        compositeDisposable.dispose();
    }

    private void conComplete() {
        compositeDisposable.dispose();
        Log.d(TAG, "onComplete: Completed!");
    }

    private void onSetLocationAdapter(List<Place> places) {
        recyclerView.setAdapter(new LocationSelectionAdapter(places));
    }
}