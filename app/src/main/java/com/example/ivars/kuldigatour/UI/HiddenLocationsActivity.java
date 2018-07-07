package com.example.ivars.kuldigatour.UI;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HiddenLocationsActivity extends AppCompatActivity
        implements LocationsListFragment.LocationItemClickListener,
        LocationUtility.LocationInterface,
        LocationDetailFragment.DetailFragmentInterface,
        LocationsListFragment.ListFragmentsInterface{

    private static final String TAG = HiddenLocationsActivity.class.getSimpleName();
    private static final String DISCOVERED_LIST_SELECTED_KEY = "discovered_list_key";

    private LocationUtility mLocationUtility;
    private LocationDetailFragment mLocationDetailFragment;
    private KuldigaLocation locationOpenedInDetailFragment;
    private LocationsListFragment mHiddenLocationsListFragment;

    //a private boolean to tell if aclicked list item should be opened as hidden or discovered
    private Boolean isDiscovered;
    //Variable to store current location availability. Set default as pending.
    private int mCurrentLocationAvailability = LocationUtility.LOCATION_PENDING_STATE;
    //A last known location object, so the listview can be updated when returning from detail view
    private Location lastKnownLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_view);

        //if is discovered search shared pref for discovered locations otherwise search for hidden ones
        isDiscovered = getIntent().getBooleanExtra(DISCOVERED_LIST_SELECTED_KEY, false);
        Bundle args = new Bundle();
        args.putBoolean(DISCOVERED_LIST_SELECTED_KEY, isDiscovered);

        mHiddenLocationsListFragment = new LocationsListFragment();
        mHiddenLocationsListFragment.setArguments(args);
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
        args.putString(KuldigaLocation.HIDDEN_SMALL_IMAGE_KEY, kuldigaLocation.getHiddenSmallImageUrl());
        args.putString(KuldigaLocation.HIDDEN_LARGE_IMAGE_KEY, kuldigaLocation.getHiddenLargeImageUrl());

        if (kuldigaLocation.getDistance() != null){
            //if distance has been calculated in the list pass it to the detail fragment
            args.putDouble(KuldigaLocation.DISTANCE_KEY, kuldigaLocation.getDistance());
        }
        //Indicate if the element should be displayed as discovered or hidden in detail_v
        args.putBoolean(DISCOVERED_LIST_SELECTED_KEY, isDiscovered);
        //When a KuldigaLocation is clicked open the details fragment
        mLocationDetailFragment = new LocationDetailFragment();
        mLocationDetailFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //Add the transaction to the back stack so the back button works correctily
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.locations_container, mLocationDetailFragment).commit();
    }

    private void showSnackBarMessage(String text){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.locations_container), text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    //New location received from location utility
    @Override
    public void currentLocationCallback(Location location) {
        //Save the last known location to have it available when returning from detail to list view
        lastKnownLocation = location;
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
            //Distances are calculated in an asyncTask, because there might be an unknown number of them
            new CalculateDistancesTask(this).execute(coordinatesList, location);
        }
    }

    //Gets distances for the list from the previously gotten location
    //check if the previous locatin was saved
    public void getDistancesFromLastLocation(){
        if (lastKnownLocation != null) {
            ArrayList<String> coordinatesList = mHiddenLocationsListFragment.getAllLocationCoordinates();
            new CalculateDistancesTask(this).execute(coordinatesList, lastKnownLocation);
        }
    }

    /*
    * Method called from list fragment when a new location gets added from firebase
    * Makes the activity calculate distances from the previously received location
    * */
    @Override
    public void calculatePreviousLocation() {
        getDistancesFromLastLocation();
    }

    //Calculated the distances to locations from String coordinates
    private static class CalculateDistancesTask extends AsyncTask<Object,Void, ArrayList<Double>>{

        private WeakReference<HiddenLocationsActivity> activityWeakReference;

        public CalculateDistancesTask(HiddenLocationsActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected ArrayList<Double> doInBackground(Object... params) {
            Log.d(TAG, "AsyncTask start");
            //The firs parameter is the list of coordinate strings
            ArrayList<String> coordinatesList = (ArrayList<String>)params[0];
            //The list in which the result will be returned as doubles
            ArrayList<Double> distanceList = new ArrayList<>();
            //Number of coordinates in the list
            int numberOfCoordinates = coordinatesList.size();
            //The location object will be the second parameter
            Location location = (Location)params[1];

            //get the reference to the activity
            HiddenLocationsActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }

            //go through all items and calculate distance
            for (int i = 0; i < numberOfCoordinates; i++){
                //go through all the coordinates and calculate the distances
                distanceList.add(activity.mLocationUtility.calculateDistance(coordinatesList.get(i),location));
                Log.d(TAG, "Distances list: "+ distanceList.get(i));
            }
            return distanceList;
        }

        @Override
        protected void onPostExecute(ArrayList<Double> distances) {
            HiddenLocationsActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            Log.d(TAG, "AsyncTask end");
            //TODO: check if the fragment exists
            //update the distances in the hidden locationsList fragment
            activity.mHiddenLocationsListFragment.updateDistancesInList(distances);
        }
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
