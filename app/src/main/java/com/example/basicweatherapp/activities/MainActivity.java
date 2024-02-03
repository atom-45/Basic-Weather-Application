package com.example.basicweatherapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.basicweatherapp.databinding.ActivityMainBinding;


import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final static String TAG = "MainActivity";
    private boolean isAndroidReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        ActivityMainBinding mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if(isAndroidReady){
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            dropSplashScreen();
                            return false;
                        }
                    }
                }
        );

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"It has been accepted!");
        } else {
            int requestCode = 348;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, requestCode);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void dropSplashScreen(){
        new Handler().postDelayed(()-> isAndroidReady=true,1000);
    }
}