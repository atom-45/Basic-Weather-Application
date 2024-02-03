package com.example.basicweatherapp.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.basicweatherapp.models.User;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface UserDAO {

    @Query("SELECT * FROM users")
    Observable<List<User>> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(User user);
}
