package com.example.basicweatherapp.data.repositories;

import android.app.Application;

import com.example.basicweatherapp.data.local.dao.UserDAO;
import com.example.basicweatherapp.data.local.database.WeatherDatabase;
import com.example.basicweatherapp.data.models.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Singleton
public class UserRepository {

    private final UserDAO userDAO;

    @Inject
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
