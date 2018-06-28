package com.example.ivars.kuldigatour.UI;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.example.ivars.kuldigatour.Utilities.LocationUtility;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationDetailFragment extends Fragment {


    @BindView(R.id.hidden_detail_iv)
    ImageView locationIv;
    @BindView(R.id.hidden_detail_title_tv)
    TextView titleTv;
    @BindView(R.id.detail_description_tv)
    TextView descriptionTv;
    @BindView(R.id.detail_distance_tv)
    TextView distanceTv;
    @BindView(R.id.unlock_location_b)
    Button testLocationB;

    private static final String TAG = LocationDetailFragment.class.getSimpleName();
    //to use firebase storage
    private FirebaseStorage mFirebaseStorage;
    //to get a certain storage part
    private StorageReference mLocPhotosStorageRef;

    private static final String DISCOVERED_LIST_SELECTED_KEY = "discovered_list_key";

    DetailFragmentInterface detailFragmentsCallback;
    interface DetailFragmentInterface{
        int getLocationUtilitiesState();
    }

    public LocationDetailFragment() {
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_hidden_locations_detail_view, container, false);

        ButterKnife.bind(this, rootView);

        //get the passed in locations object
        final KuldigaLocation mKuldigaLocation = new KuldigaLocation(
                getArguments().getString(KuldigaLocation.COORDINATES_KEY),
                getArguments().getString(KuldigaLocation.DESCRIPTION_KEY),
                getArguments().getString(KuldigaLocation.NAME_KEY),
                getArguments().getString(KuldigaLocation.HIDDEN_DESCRIPTION_KEY),
                getArguments().getString(KuldigaLocation.HIDDEN_NAME_KEY),
                getArguments().getString(KuldigaLocation.WORKING_HOURS_KEY),
                getArguments().getString(KuldigaLocation.LARGE_IMAGE_KEY),
                getArguments().getString(KuldigaLocation.SMALL_IMAGE_KEY),
                getArguments().getString(KuldigaLocation.HIDDEN_SMALL_IMAGE_KEY),
                getArguments().getString(KuldigaLocation.HIDDEN_LARGE_IMAGE_KEY)
        );



        testLocationB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationUtility.setLocationDiscovered(mKuldigaLocation, getActivity());
                Toast.makeText(getActivity(), "location found", Toast.LENGTH_SHORT).show();
            }
        });

        mFirebaseStorage = FirebaseStorage.getInstance();
        mLocPhotosStorageRef = mFirebaseStorage.getReference().child("location_photos");


        boolean isDiscovered = getArguments().getBoolean(DISCOVERED_LIST_SELECTED_KEY, false);
        if (isDiscovered){
            Log.d(TAG, "location is discovered");
            //if location discovered, show discovered name
            titleTv.setText(mKuldigaLocation.getDiscoveredName());
            descriptionTv.setText(mKuldigaLocation.getDiscoveredDescription());
            Picasso.get().load(mKuldigaLocation.getLargeImageUrl()).into(locationIv);
            //TODO: if working hours is not null show them here
        } else {
            Log.d(TAG, "location is hidden");
            //if location hidden show hidden name
            titleTv.setText(mKuldigaLocation.getHiddenName());
            descriptionTv.setText(mKuldigaLocation.getHiddenDescription());
            Picasso.get().load(mKuldigaLocation.getHiddenLargeImageUrl()).into(locationIv);
        }

        //check if distance was calculated in the list fragment
        if (getArguments().containsKey(KuldigaLocation.DISTANCE_KEY)){
            double distance = getArguments().getDouble(KuldigaLocation.DISTANCE_KEY);
            distanceTv.setText("Distance: " + distance + " km");
        } else {
            // if the distance was not passed from the list fragment determine what message
            //should be displayed
            //Get the state of utility and show the appropriate message to user
            detailFragmentsCallback = (DetailFragmentInterface)getActivity();
            int state = detailFragmentsCallback.getLocationUtilitiesState();
            switch (state){
                case LocationUtility.LOCATION_AVAILABLE_STATE:
                    distanceTv.setText("Getting distance...");
                    break;
                case LocationUtility.LOCATION_NOT_AVAILABLE_STATE:
                    distanceTv.setText("Enable location permission in settings to see distance to location");
                    break;
                case LocationUtility.LOCATION_PENDING_STATE:
                    distanceTv.setText("Getting distance...");
                    break;
                default:
                    Log.e(TAG, "unknown location state");
                    break;
            }
        }
        return rootView;
    }

    //This method gets called from the activity to update the texview with the distance
    public void updateDistanceTv(double distance){
        distanceTv.setText("Distance: " + distance + " km");
    }

    //This method gets called from the activity to update the texview with the distance
    public void setDistanceTv(String text){
        distanceTv.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO:
        /*
        if (mRequestingLocationUpdates){
            startLocationUpdates();
        }
        */
    }

    //TODO
    public void showToastTest(String text, android.location.Location location){
        Toast.makeText(getActivity(), text + location.getLatitude(), Toast.LENGTH_SHORT).show();
    }

}
