package com.example.basicweatherapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basicweatherapp.LocationSelectionFragmentDirections;
import com.example.basicweatherapp.R;
import com.example.basicweatherapp.databinding.LocationSelectCardBinding;
import com.example.basicweatherapp.models.Place;

import java.util.List;

public class LocationSelectionAdapter extends RecyclerView.Adapter<LocationSelectionAdapter.ViewHolder> {

    private final List<Place> places;
    private LayoutInflater inflater;


    public LocationSelectionAdapter(List<Place> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(inflater==null){
            inflater = LayoutInflater.from(parent.getContext());
        }
        LocationSelectCardBinding selectCardBinding = DataBindingUtil.inflate(inflater,
                R.layout.location_select_card, parent, false);
        return new ViewHolder(selectCardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = places.get(position);
        holder.selectCardBinding.setPlace(place);

        holder.selectCardBinding.locationSelectCardView.setOnClickListener(v -> {
            LocationSelectionFragmentDirections.ActionLocationSelectionFragmentToMainFragment
                    navDirections = LocationSelectionFragmentDirections
                    .actionLocationSelectionFragmentToMainFragment();

            navDirections.setPlaceName(place.getName());
            navDirections.setRegion(place.getRegion());
            navDirections.setCountry(place.getCountry());


            Navigation.findNavController(v).navigate(navDirections);

        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final LocationSelectCardBinding selectCardBinding;

        public ViewHolder(@NonNull LocationSelectCardBinding selectCardBinding) {
            super(selectCardBinding.getRoot());
            this.selectCardBinding = selectCardBinding;
        }

        public void bindPlace(Place place){
            selectCardBinding.setPlace(place);
            selectCardBinding.executePendingBindings();
        }
    }
}
