package com.example.basicweatherapp.repositories;

import android.app.Application;

import com.example.basicweatherapp.dao.UserDAO;
import com.example.basicweatherapp.database.WeatherDatabase;
import com.example.basicweatherapp.models.User;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class UserRepository {

    private final UserDAO userDAO;

    public UserRepository(Application application){
        this.userDAO = WeatherDatabase.getInstance(application).userDAO();
    }

    public Observable<List<User>> getAllUsers(){
        return userDAO.getAllUsers();
    }

    public Completable insertUser(User user){
        return userDAO.insertUser(user);
    }
}
