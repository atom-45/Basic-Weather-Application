package com.example.basicweatherapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.basicweatherapp.models.User;
import com.example.basicweatherapp.repositories.UserRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class UserViewModel extends AndroidViewModel {


    private final UserRepository userRepository;


    public UserViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public Observable<List<User>> getAllUsers(){
        return userRepository.getAllUsers();
    }

    public Completable insertUser(User user){
        return userRepository.insertUser(user);
    }
}
