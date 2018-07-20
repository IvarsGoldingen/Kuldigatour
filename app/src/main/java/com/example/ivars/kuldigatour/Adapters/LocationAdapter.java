package com.example.ivars.kuldigatour.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationAdapterViewHolder> {

    private static final String TAG = LocationAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<KuldigaLocation> mLocationsList;
    //used to detect of the current list is supposed to show hidden or discovered locations
    private boolean isDiscoveredList;
    private LocationListItemClickListener mOnClickListener;

    public LocationAdapter(Context context,
                           boolean isDiscoveredList,
                           ArrayList<KuldigaLocation> list,
                           LocationListItemClickListener listener) {
        mContext = context;
        mLocationsList = list;
        this.isDiscoveredList = isDiscoveredList;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public LocationAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.location_card, parent, false);
        return new LocationAdapterViewHolder(view);
    }

    //Binds a viewholder to the data and a certain position
    @Override
    public void onBindViewHolder(@NonNull LocationAdapterViewHolder holder, int position) {
        KuldigaLocation currentKuldigaLocation = mLocationsList.get(position);
        if (isDiscoveredList) {
            //displaying the discovered atributes
            holder.listDescriptionTv.setText(currentKuldigaLocation.getDiscoveredDescription());
            holder.listTitleTv.setText(currentKuldigaLocation.getDiscoveredName());
            //cancel the previous load request, so the wrong image does not get loaded here
            Picasso.get().cancelRequest(holder.listImageIv);
            Picasso.get().load(currentKuldigaLocation.getSmallImageUrl())
                    .fit()
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.image_download_error)
                    .into(holder.listImageIv);
        } else {
            //Displaying the hidden atributes
            holder.listDescriptionTv.setText(currentKuldigaLocation.getHiddenDescription());
            holder.listTitleTv.setText(currentKuldigaLocation.getHiddenName());
            //cancel the previous load request, so the wrong image does not get loaded here
            Picasso.get().cancelRequest(holder.listImageIv);
            Picasso.get().load(currentKuldigaLocation.getHiddenSmallImageUrl())
                    .fit()
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.image_download_error)
                    .into(holder.listImageIv);
        }

        //set the distance for both lists
        if (currentKuldigaLocation.getDistance() != null) {
            String text = currentKuldigaLocation.getDistance() + "\n"
                    + mContext.getResources().getString(R.string.km);
            holder.listDistanceToTv.setText(text);
        } else {
            String text = mContext.getResources().getString(R.string.no_distance_placeholder)
                    + "\n" + mContext.getResources().getString(R.string.km);
            holder.listDistanceToTv.setText(text);
        }

    }

    @Override
    public int getItemCount() {
        if (mLocationsList != null) {
            return mLocationsList.size();
        }
        return 0;
    }

    //Allows the list to be changed from discovered to hidden locations
    public void changeList(ArrayList list) {
        mLocationsList = list;
        isDiscoveredList = !isDiscoveredList;
    }

    public interface LocationListItemClickListener {
        void OnLocationClickListener(int clickedLocation);
    }

    class LocationAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.hidden_list_title_tv)
        TextView listTitleTv;
        @BindView(R.id.hidden_list_description_tv)
        TextView listDescriptionTv;
        @BindView(R.id.list_distance_to_tv)
        TextView listDistanceToTv;
        @BindView(R.id.hidden_list_iv)
        ImageView listImageIv;

        public LocationAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int locationClicked = getAdapterPosition();
            mOnClickListener.OnLocationClickListener(locationClicked);
        }
    }

}
