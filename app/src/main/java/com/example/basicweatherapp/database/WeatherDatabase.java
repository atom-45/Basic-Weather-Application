package com.example.basicweatherapp.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.basicweatherapp.dao.LocationDAO;
import com.example.basicweatherapp.dao.PlaceDAO;
import com.example.basicweatherapp.dao.UserDAO;
import com.example.basicweatherapp.models.Location;
import com.example.basicweatherapp.models.Place;
import com.example.basicweatherapp.models.User;

@Database(entities = {User.class, Location.class, Place.class}, version = 1, exportSchema = false)
public abstract class WeatherDatabase extends RoomDatabase {
    private static volatile WeatherDatabase instance;

    public static WeatherDatabase getInstance(final Context context){
        if(instance==null)
        {
            synchronized (WeatherDatabase.class)
            {
               if(instance==null){
                   instance = Room.databaseBuilder(context,WeatherDatabase.class,
                           "weather_database").build();
               }
            }
        }
        return instance;
    }

    public abstract LocationDAO locationDAO();
    public abstract UserDAO userDAO();
    public abstract PlaceDAO placeDAO();
}
