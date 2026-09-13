package com.example.basicweatherapp.di.components;

import android.app.Application;

import com.example.basicweatherapp.di.modules.AppModule;
import com.example.basicweatherapp.di.modules.RetrofitModule;
import com.example.basicweatherapp.di.modules.ViewModelModule;
import com.example.basicweatherapp.presentation.activities.MainActivity;
import com.example.basicweatherapp.services.WeatherBLEService;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;


@Singleton
@Component(modules = {RetrofitModule.class, AppModule.class, ViewModelModule.class})
public interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        ApplicationComponent build();
    }

    void inject(WeatherBLEService service);
    void inject(MainActivity activity);
}
