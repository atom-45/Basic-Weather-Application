package com.example.basicweatherapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.databinding.HourCardBinding;
import com.example.basicweatherapp.models.ForecastDay;
import com.example.basicweatherapp.models.Hour;
import com.example.basicweatherapp.responses.ForecastResponse;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private final List<ForecastDay> forecastDays;
    private final List<Hour> hours;

    public HourAdapter(ForecastResponse forecastResponse) {
        LocalTime localTime = LocalTime.now();
        this.forecastDays = forecastResponse.getForecast().getForecastDays();

        this.hours = forecastDays.get(0).getHours()
                .stream()
                .filter(hour -> {
                    String[] timeSplit = hour.getTime().split(" ");
                    int hr = Integer.parseInt(timeSplit[1].substring(0,2));

                    return hr>=localTime.getHour();
                })
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(inflater==null){
            inflater = LayoutInflater.from(parent.getContext());
        }
        HourCardBinding hourCardBinding = DataBindingUtil.inflate(inflater,
                R.layout.hour_card,parent,false);

        return new ViewHolder(hourCardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindHour(hours.get(position));

    }

    @Override
    public int getItemCount() {
        return hours.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        HourCardBinding hourCardBinding;
        public ViewHolder(@NonNull HourCardBinding hourCardBinding) {
            super(hourCardBinding.getRoot());
            this.hourCardBinding = hourCardBinding;
        }

        public void bindHour(Hour hour){
            hourCardBinding.setHour(hour);
            hourCardBinding.executePendingBindings();
        }
    }
}
