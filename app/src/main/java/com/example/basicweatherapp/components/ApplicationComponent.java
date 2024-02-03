package com.example.basicweatherapp.components;

import com.example.basicweatherapp.modules.RetrofitModule;
import com.example.basicweatherapp.repositories.WeatherRepository;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {RetrofitModule.class})
public interface ApplicationComponent {

    WeatherRepository weatherRepository();

}
