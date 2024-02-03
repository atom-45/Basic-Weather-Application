package com.example.basicweatherapp.application;

import android.app.Application;

import com.example.basicweatherapp.components.ApplicationComponent;
import com.example.basicweatherapp.components.DaggerApplicationComponent;


public class WeatherApplication extends Application {

    public ApplicationComponent applicationComponent = DaggerApplicationComponent.create();


}
