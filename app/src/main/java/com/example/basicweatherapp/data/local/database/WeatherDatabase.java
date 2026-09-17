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
import com.example.basicweatherapp.data.models.PredictionVerification;
import com.example.basicweatherapp.data.models.SensorData;
import com.example.basicweatherapp.data.models.ThermodynamicPrediction;
import com.example.basicweatherapp.data.models.User;

@Database(entities = {User.class, Location.class, Place.class, SensorData.class, CachedForecast.class, PredictionVerification.class, ThermodynamicPrediction.class}, version = 6, exportSchema = false)
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
                           .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `prediction_verifications` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` TEXT, " +
                    "`temperature` FLOAT NOT NULL, `humidity` FLOAT NOT NULL, `pressure` FLOAT NOT NULL, " +
                    "`prediction` TEXT, `actual_outcome` TEXT)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `prediction_verifications` ADD COLUMN `residual` DOUBLE NOT NULL DEFAULT 0.0");
            db.execSQL("ALTER TABLE `prediction_verifications` ADD COLUMN `source` TEXT");
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `thermodynamic_predictions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`original_timestamp` TEXT, " +
                    "`temp_snapshot` FLOAT NOT NULL, " +
                    "`humidity_snapshot` FLOAT NOT NULL, " +
                    "`pressure_snapshot` FLOAT NOT NULL, " +
                    "`predicted_arrival` TEXT, " +
                    "`storm_speed` DOUBLE NOT NULL, " +
                    "`residual` DOUBLE NOT NULL, " +
                    "`window_start_millis` INTEGER NOT NULL, " +
                    "`window_end_millis` INTEGER NOT NULL, " +
                    "`verification_status` TEXT, " +
                    "`storm_event_id` TEXT, " +
                    "`actual_outcome` TEXT)");
        }
    };

    public abstract LocationDAO locationDAO();
    public abstract UserDAO userDAO();
    public abstract PlaceDAO placeDAO();
    public abstract SensorDAO sensorDAO();
    public abstract CachedForecastDAO cachedForecastDAO();
    public abstract com.example.basicweatherapp.data.local.dao.PredictionVerificationDAO predictionVerificationDAO();
    public abstract com.example.basicweatherapp.data.local.dao.ThermodynamicPredictionDAO thermodynamicPredictionDAO();
}
