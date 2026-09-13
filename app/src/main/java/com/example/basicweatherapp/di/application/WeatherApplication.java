package com.example.basicweatherapp.di.application;

import android.app.Application;

import com.example.basicweatherapp.di.components.ApplicationComponent;
import com.example.basicweatherapp.di.components.DaggerApplicationComponent;



public class WeatherApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerApplicationComponent.builder()
                .application(this)
                .build();
    }

    public ApplicationComponent getApplicationComponent(){
        return applicationComponent;
    }

}
