package com.example.basicweatherapp.data.local.database;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.SQLiteConnection;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.basicweatherapp.data.local.dao.CachedForecastDAO;
import com.example.basicweatherapp.data.local.dao.LocationDAO;
import com.example.basicweatherapp.data.local.dao.PlaceDAO;
import com.example.basicweatherapp.data.local.dao.SensorDAO;
import com.example.basicweatherapp.data.local.dao.UserDAO;
import com.example.basicweatherapp.data.models.CachedForecast;
import com.example.basicweatherapp.data.models.Location;
import com.example.basicweatherapp.data.models.Place;
import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.models.User;

@Database(entities = {User.class, Location.class, Place.class, SensorData.class, CachedForecast.class}, version = 3, exportSchema = false)
public abstract class WeatherDatabase extends RoomDatabase {
    private static volatile WeatherDatabase instance;

    public static WeatherDatabase getInstance(final Context context)
    {
        if(instance==null)
        {
            synchronized (WeatherDatabase.class)
            {
               if(instance==null){
                   instance = Room.databaseBuilder(context,WeatherDatabase.class,
                           "weather_database")
                           .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                           .build();
               }
            }
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1 , 2)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `sensor_data` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,  `date` TEXT, " +
                    "`humidity` FLOAT NOT NULL, `temperature` FLOAT NOT NULL, `pressure` FLOAT NOT NULL, " +
                    "`altitude` FLOAT NOT NULL)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `cached_forecast` " +
                    "(`id` TEXT NOT NULL, `json_data` TEXT, PRIMARY KEY(`id`))");
        }
    };

    public abstract LocationDAO locationDAO();
    public abstract UserDAO userDAO();
    public abstract PlaceDAO placeDAO();
    public abstract SensorDAO sensorDAO();
    public abstract CachedForecastDAO cachedForecastDAO();
}
