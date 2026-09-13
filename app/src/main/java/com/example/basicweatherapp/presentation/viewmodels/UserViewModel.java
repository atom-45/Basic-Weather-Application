package com.example.basicweatherapp.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.basicweatherapp.data.models.User;
import com.example.basicweatherapp.data.repositories.UserRepository;
import com.example.basicweatherapp.di.application.WeatherApplication;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class UserViewModel extends ViewModel {

    private final UserRepository userRepository;

    @Inject
    public UserViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    public Observable<List<User>> getAllUsers(){
        return userRepository.getAllUsers();
    }

    public Completable insertUser(User user){
        return userRepository.insertUser(user);
    }
}
