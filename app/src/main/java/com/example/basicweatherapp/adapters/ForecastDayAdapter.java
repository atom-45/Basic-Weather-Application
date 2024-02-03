package com.example.basicweatherapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.databinding.ForecastCardBinding;
import com.example.basicweatherapp.models.ForecastDay;
import com.example.basicweatherapp.responses.ForecastResponse;

import java.util.List;

public class ForecastDayAdapter extends RecyclerView.Adapter<ForecastDayAdapter.ViewHolder> {

    private LayoutInflater inflater;

    private final List<ForecastDay> forecastDays;


    public ForecastDayAdapter(ForecastResponse forecastResponse){
        this.forecastDays = forecastResponse.getForecast().getForecastDays();
    }


    @NonNull
    @Override
    public ForecastDayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(inflater==null){
            inflater = LayoutInflater.from(parent.getContext());
        }
        ForecastCardBinding forecastCardBinding = DataBindingUtil.inflate(inflater, R.layout.forecast_card,
                parent,false);

        return new ViewHolder(forecastCardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastDayAdapter.ViewHolder holder, int position) {
        holder.bindForecastDay(forecastDays.get(position));

    }

    @Override
    public int getItemCount() {
        return forecastDays.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ForecastCardBinding forecastCardBinding;
        public ViewHolder(@NonNull ForecastCardBinding forecastCardBinding) {
            super(forecastCardBinding.getRoot());
            this.forecastCardBinding = forecastCardBinding;
        }

        public void bindForecastDay(ForecastDay forecastDay){
            forecastCardBinding.setForecast(forecastDay);
            forecastCardBinding.executePendingBindings();
        }
    }
}
