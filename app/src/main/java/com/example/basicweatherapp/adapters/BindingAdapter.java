package com.example.basicweatherapp.adapters;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.time.LocalDate;
import java.util.Locale;

public class BindingAdapter {

    @androidx.databinding.BindingAdapter("time")
    public static void setTime(TextView textView, String time){
        String[] timeSplit = time.split(" ");
        textView.setText(timeSplit[1]);
    }

    @androidx.databinding.BindingAdapter("convertDate")
    public static void setConvertDate(TextView textView, String date){
        LocalDate localDate = LocalDate.parse(date);
        String month = localDate.getMonth().toString().toUpperCase().charAt(0)
                +localDate.getMonth().toString().toLowerCase().substring(1);

        textView.setText(String.format("%1s %2s %3s",localDate.getDayOfMonth(), month,
                localDate.getYear()));


    }

    @androidx.databinding.BindingAdapter("weatherIcon")
    public static void setWeatherIcon(ImageView imageView, String link){
        RequestManager glide = Glide.with(imageView.getContext());
        String fullLink = "https:"+link;
        glide.load(fullLink).into(imageView);

    }

    @androidx.databinding.BindingAdapter(value = {"name", "region", "country"})
    public static void setLocation(TextView textView, String name, String region, String country){

        String locationName = String.format(Locale.ENGLISH,"%1s\n%2s\n%3s",name, region, country);

        textView.setText(locationName);
    }
}
