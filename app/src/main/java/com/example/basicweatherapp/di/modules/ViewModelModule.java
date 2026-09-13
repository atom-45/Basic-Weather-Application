package com.example.basicweatherapp.di.modules;

import androidx.lifecycle.ViewModel;


import com.example.basicweatherapp.factories.ViewModelKey;
import com.example.basicweatherapp.presentation.viewmodels.LocationViewModel;
import com.example.basicweatherapp.presentation.viewmodels.PlaceViewModel;
import com.example.basicweatherapp.presentation.viewmodels.SensorDataViewModel;
import com.example.basicweatherapp.presentation.viewmodels.UserViewModel;
import com.example.basicweatherapp.presentation.viewmodels.WeatherViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel.class)
    public abstract ViewModel bindWeatherViewModel(WeatherViewModel weatherViewModel);


    @Binds
    @IntoMap
    @ViewModelKey(SensorDataViewModel.class)
    public abstract ViewModel bindSensorDataViewModel(SensorDataViewModel sensorDataViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlaceViewModel.class)
    public abstract ViewModel bindPlaceViewModel(PlaceViewModel placeViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel.class)
    public abstract ViewModel bindUserViewModel(UserViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LocationViewModel.class)
    public abstract ViewModel bindLocationViewModel(LocationViewModel locationViewModel);



}
