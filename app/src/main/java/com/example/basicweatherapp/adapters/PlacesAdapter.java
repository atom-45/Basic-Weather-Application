package com.example.basicweatherapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basicweatherapp.R;
import com.example.basicweatherapp.models.Place;

import java.util.List;
import java.util.Locale;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    /**
     * The error coming from this adapter has to do with the my setText accepting an integer.
     * android.content.res.Resources$NotFoundException: String resource ID, is the
     * error I am getting which means that the issue is how setText reads integers.
     * It uses integers to get a resource file which I did not know.
     */

    private final List<Place> places;

    public PlacesAdapter(List<Place> places){
        this.places = places;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public PlacesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_container_cardview_2,parent,false);

        return new PlacesAdapter.ViewHolder(cv);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;

        TextView location = cardView.findViewById(R.id.placeNameValue);
        TextView region = cardView.findViewById(R.id.placeRegionValue);
        TextView country = cardView.findViewById(R.id.placeCountryValue);
        TextView latitude = cardView.findViewById(R.id.placeLatitudeValue);
        TextView longitude = cardView.findViewById(R.id.placeLongitudeValue);

        location.setText(places.get(position).getName());
        region.setText(places.get(position).getRegion());
        country.setText(places.get(position).getCountry());
        latitude.setText(String.format(Locale.UK,"%2.2f", places.get(position).getLat()));
        longitude.setText(String.format(Locale.UK,"%2.2f",places.get(position).getLon()));

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final CardView cardView;
        public ViewHolder(@NonNull CardView cv) {
            super(cv);
            cardView = cv;
        }
    }
}
