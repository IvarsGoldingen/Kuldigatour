package com.example.ivars.kuldigatour.UI;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.example.ivars.kuldigatour.Utilities.LocationUtility;

import java.util.ArrayList;

public class HiddenLocationsActivity extends AppCompatActivity
        implements HiddenLocationsListFragment.LocationItemClickListener,
        LocationUtility.LocationInterface,
        LocationDetailFragment.DetailFragmentInterface{

    private static final String TAG = HiddenLocationsActivity.class.getSimpleName();

    private LocationUtility mLocationUtility;
    private LocationDetailFragment mLocationDetailFragment;
    private KuldigaLocation locationOpenedInDetailFragment;
    private HiddenLocationsListFragment mHiddenLocationsListFragment;

    //Variable to store current location availability. Set default as pending.
    private int mCurrentLocationAvailability = LocationUtility.LOCATION_PENDING_STATE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_location_view);

        mHiddenLocationsListFragment = new HiddenLocationsListFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.locations_container, mHiddenLocationsListFragment).commit();
        mLocationUtility = new LocationUtility(this, this);
        //Start receiving location updates
        mLocationUtility.startLocationRequestProcess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        mLocationUtility.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        mLocationUtility.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationClicked(KuldigaLocation kuldigaLocation) {
        locationOpenedInDetailFragment = kuldigaLocation;
        //Add extras for the details frgment
        Bundle args = new Bundle();
        args.putString(KuldigaLocation.NAME_KEY, kuldigaLocation.getDiscoveredName());
        args.putString(KuldigaLocation.DESCRIPTION_KEY, kuldigaLocation.getDiscoveredDescription());
        args.putString(KuldigaLocation.HIDDEN_NAME_KEY, kuldigaLocation.getHiddenName());
        args.putString(KuldigaLocation.HIDDEN_DESCRIPTION_KEY, kuldigaLocation.getHiddenDescription());
        args.putString(KuldigaLocation.WORKING_HOURS_KEY, kuldigaLocation.getWorkingHours());
        args.putString(KuldigaLocation.COORDINATES_KEY, kuldigaLocation.getCoordinates());
        args.putString(KuldigaLocation.LARGE_IMAGE_KEY, kuldigaLocation.getLargeImageUrl());
        args.putString(KuldigaLocation.SMALL_IMAGE_KEY, kuldigaLocation.getSmallImageUrl());

        //TODO: comment this
        //When a KuldigaLocation is clicked open the details fragment
        mLocationDetailFragment = new LocationDetailFragment();
        mLocationDetailFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //TODO:make back button go to list
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.locations_container, mLocationDetailFragment).commit();

    }

    private void showSnackBarMessage(String text){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.locations_container), text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    //New location received from location utility
    @Override
    public void currentLocationCallback(Location location) {
        //TODO: change this to something better
        showSnackBarMessage("Location updated");
        //TODO: how to check what is visible
        if (mLocationDetailFragment != null){
            Log.d(TAG, "detail fragment exists");
            //If locations detail fragment is visible
            double distance = mLocationUtility.calculateDistance(locationOpenedInDetailFragment, location);
            mLocationDetailFragment.updateDistanceTv(distance);
        }
        if (mHiddenLocationsListFragment.locationsRv != null){
            Log.d(TAG, "list fragment exists");

            //get list of all coordinates for KuldigaLocation objects in the visible list
            ArrayList<String> coordinatesList = mHiddenLocationsListFragment.getAllLocationCoordinates();
            ArrayList<Double> distancesList = getDistancesFromCoordinates(coordinatesList, location);
            mHiddenLocationsListFragment.updateDistancesInList(distancesList);
            //The list in which the distances will be saved

        }
    }

    private ArrayList<Double> getDistancesFromCoordinates(ArrayList<String> coordinatesList, Location location){
        //TODO: do this in AsyncTask to fulfill the rubric
        ArrayList<Double> distanceList = new ArrayList<>();
        int numberOfCoordinates = coordinatesList.size();
        //go through all items and calculate distance
        for (int i = 0; i < numberOfCoordinates; i++){
            distanceList.add(mLocationUtility.calculateDistance(coordinatesList.get(i),location));
            Log.d(TAG, "Distances list: "+ distanceList.get(i));
        }
        return distanceList;
    }

    //Error message from location utility
    @Override
    public void errorMessageCallback(String errorMessage) {
        showSnackBarMessage(errorMessage);
    }

    //A different message from the utility for the detail fragment
    @Override
    public void differentLocationState(int locationState) {
        mCurrentLocationAvailability = locationState;

        //TODO: test if this is really not necessary
        /*
        String locationDistanceMessage;
        switch (locationState){
            case LocationUtility.LOCATION_NOT_AVAILABLE_STATE:
                locationDistanceMessage = "Enable location permission in settings to see distance to location";
                break;
            case LocationUtility.LOCATION_PENDING_STATE:
                locationDistanceMessage = "Getting distance...";
                break;
            default:
                locationDistanceMessage = "Error";
                Log.e(TAG, "Unknown location state message from locationsUtility");
                break;
        }
        if (mLocationDetailFragment != null){
            mLocationDetailFragment.setDistanceTv(locationDistanceMessage);
        }
        */
    }

    //This gets called from the details fragment on initialization
    @Override
    public int getLocationUtilitiesState() {
        return mCurrentLocationAvailability;
    }
}
