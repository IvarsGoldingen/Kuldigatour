package com.example.ivars.kuldigatour.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationAdapterViewHolder>{

    private static final String TAG = LocationAdapter.class.getSimpleName();

    private static final int HIDDEN_LIST_TYPE = 1;
    private static final int DISCOVERED_LIST_TYPE = 2;

    private Context mContext;
    private ArrayList<KuldigaLocation> mLocationsList;
    //used to detect of the current list is supposed to show hidden or discovered locations
    private int listType;
    private LocationListItemClickListener mOnClickListener;

    public interface LocationListItemClickListener{
        void OnLocationClickListener(int clickedLocation);
    }

    public LocationAdapter(Context context, int listType,
                           ArrayList<KuldigaLocation> list, LocationListItemClickListener listener){
        mContext = context;
        mLocationsList = list;
        this.listType = listType;
        mOnClickListener = listener;
    }

    @NonNull
    @Override
    public LocationAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.hidden_location_item, parent, false);
        return new LocationAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapterViewHolder holder, int position) {
        switch (listType){
            case HIDDEN_LIST_TYPE:
                KuldigaLocation currentKuldigaLocation = mLocationsList.get(position);
                holder.listDescriptionTv.setText(currentKuldigaLocation.getHiddenDescription());
                holder.listTitleTv.setText(currentKuldigaLocation.getHiddenName());
                if (currentKuldigaLocation.getDistance() != null){
                    holder.listDistanceToTv.setText(
                            String.valueOf(currentKuldigaLocation.getDistance()) + "\n km");
                } else {
                    //TODO possibly use a loading indicator
                    //distance has not been calculated yet
                    holder.listDistanceToTv.setText("x km");
                }
                //TODO get image from FB
                break;
            case DISCOVERED_LIST_TYPE:
                break;
            default:
                Log.e(TAG, "Unknown list type");
                    break;
        }
    }


    @Override
    public int getItemCount() {
        if (mLocationsList != null){
            return mLocationsList.size();
        }
        return 0;
    }

    class LocationAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            Log.d("Viewholder", "Onclick registered");
            mOnClickListener.OnLocationClickListener(locationClicked);
        }
    }

}
