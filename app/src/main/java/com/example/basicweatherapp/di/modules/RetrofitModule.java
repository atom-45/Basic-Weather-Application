package com.example.basicweatherapp.di.modules;

import androidx.annotation.NonNull;

import com.example.basicweatherapp.data.remote.networking.APIService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitModule {


    @Provides
    @Singleton
    public Retrofit provideRetrofit()
    {

        return new Retrofit.Builder()
                .baseUrl("https://api.weatherapi.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

    }

    @Provides
    @Singleton
    APIService provideAPIService(@NonNull Retrofit retrofit){
        return  retrofit.create(APIService.class);
    }
}
