package com.example.basicweatherapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.basicweatherapp.R;
import com.example.basicweatherapp.models.Astro;
import com.example.basicweatherapp.models.Condition;
import com.example.basicweatherapp.models.Current;
import com.example.basicweatherapp.models.Day;
import com.example.basicweatherapp.models.ForecastDay;
import com.example.basicweatherapp.models.Location;
import com.example.basicweatherapp.responses.ForecastResponse;

import java.util.List;
import java.util.Locale;

public class ForecastAdapter2 extends RecyclerView.Adapter<ForecastAdapter2.ViewHolder> {

    private final Location location;
    private final List<ForecastDay> forecastDayList;
    private final Current current;
    private final RequestManager glide;
    private final String requestType;

    public ForecastAdapter2(ForecastResponse response, RequestManager glide, String requestType){
        this.glide = glide;
        this.requestType = requestType;
        forecastDayList = response.getForecast().getForecastDays();
        location = response.getLocation();
        current = response.getCurrent();
    }


    public Astro getCurrentAstro(int position){
        return forecastDayList.get(position).getAstro();
    }

    public Condition getCurrentCondition(int position){
        return forecastDayList.get(position).getDay().getCondition();
    }

    public Day getCurrentDay(int position){
        return forecastDayList.get(position).getDay();
    }

    public String getCurrentDate(int position){
        return forecastDayList.get(position).getDate();
    }

    @NonNull
    @Override
    public ForecastAdapter2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_card,parent,false);
        return new ForecastAdapter2.ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;

        TextView cityName = cardView.findViewById(R.id.city_name);
        TextView currentDate = cardView.findViewById(R.id.current_date);
        TextView temperature = cardView.findViewById(R.id.temperature_value);
        TextView sunset = cardView.findViewById(R.id.sunset_value);
        TextView sunrise = cardView.findViewById(R.id.sunrise_value);
        TextView rain = cardView.findViewById(R.id.rain_probability);
        TextView humidity = cardView.findViewById(R.id.humidity_value);
        TextView weatherText = cardView.findViewById(R.id.weather_text);
        ImageView weatherIcon = cardView.findViewById(R.id.weather_icon);

        if(requestType.equals("current")){
            temperature.setText(String.format(Locale.UK,"%2.1f\u00B0",current.getTemp_c()));
            humidity.setText(String.format(Locale.getDefault(),"%2d%%",current.getHumidity()
            ));
        } else {
            temperature.setText(String.format(Locale.UK,"%2.1f\u00B0",
                    getCurrentDay(position).getMaxTemperature()));
            humidity.setText(String.format(Locale.getDefault(),"%2d%%",
                   getCurrentDay(position).getAverageHumidity()));
        }

        cityName.setText(location.getName());
        currentDate.setText(getCurrentDate(position));
        weatherText.setText(getCurrentCondition(position).getText());
        sunrise.setText(getCurrentAstro(position).getSunriseTime());
        sunset.setText(getCurrentAstro(position).getSunsetTime());
        rain.setText(String.format(Locale.getDefault(),"%2d%%"
                ,getCurrentDay(position).getRain_probability()));

        glide.load("https:"+getCurrentCondition(position).getIconURL()).into(weatherIcon);

    }

    @Override
    public int getItemCount() {
        return forecastDayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

       private final CardView cardView;
       public ViewHolder(@NonNull CardView cv) {
           super(cv);
           cardView = cv;
       }
   }
}
