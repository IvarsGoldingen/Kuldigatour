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
    //key for requesting location permission
    private static final int REQUEST_LOCATION_PERMISSION = 3;
    //Request code for storage access
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 565;
    //Tags for the fragments so it can be accessed after orentation changes for location updates
    private static final String FRAGMENT_LIST_TAG = "list_fragment_tag";
    private static final String FRAGMENT_DETAIL_TAG = "detail_fragment_tag";

    //Save instance keys:
    private static final String STATE_LAST_KNOWN_LOCATION = "last_nown_location_state_key";
    private static final String STATE_IS_DISCOVERED = "is_discovered_state_key";
    private static final String STATE_CURRENT_LOCATION_AVAILIBILITY = "current_location_availability_state_key";
    private static final String STATE_OPENED_KULDIGA_LOCATION = "detail_fragment_location_state_key";

    private LocationUtility mLocationUtility;
    private LocationDetailFragment mLocationDetailFragment;
    private LocationsListFragment mHiddenLocationsListFragment;

    private KuldigaLocation locationOpenedInDetailFragment;
    //a private boolean to tell if a clicked list item should be opened as hidden or discovered
    private Boolean isDiscoveredList;
    //Variable to store current location availability. Set default as pending.
    private int mCurrentLocationAvailability = LocationUtility.LOCATION_PENDING_STATE;
    //A last known location object, so the listview can be updated when returning from detail view
    private Location lastKnownLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_view);
        //if is discovered search shared pref for discovered locations otherwise search for hidden ones
        isDiscoveredList = getIntent().getBooleanExtra(DISCOVERED_LIST_SELECTED_KEY, false);
        //start new fragments only if there is no previously saved state
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putBoolean(DISCOVERED_LIST_SELECTED_KEY, isDiscoveredList);
            mHiddenLocationsListFragment = new LocationsListFragment();
            mHiddenLocationsListFragment.setArguments(args);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.locations_container, mHiddenLocationsListFragment,
                    FRAGMENT_LIST_TAG).commit();
        } else {
            //if the fragments are savid in the instanceState get references to them, so the
            //distance updates can be continued
            mHiddenLocationsListFragment = (LocationsListFragment) getSupportFragmentManager().
                    findFragmentByTag(FRAGMENT_LIST_TAG);
            mLocationDetailFragment = (LocationDetailFragment) getSupportFragmentManager().
                    findFragmentByTag(FRAGMENT_DETAIL_TAG);
        }
        mLocationUtility = new LocationUtility(this, this);
        //Start receiving location updates
        mLocationUtility.startLocationRequestProcess();
    }

    //This is called when the location utility checks settings for location on the device
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //forward result to the locations utility
        mLocationUtility.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                //If request code is for location handle it in the LocationUtility
                mLocationUtility.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            case WRITE_EXTERNAL_STORAGE_PERMISSION:
                //If the request code is for storage handle it int the details fragment
                mLocationDetailFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            default:
                Log.e(TAG, "Unknown permission result: " + requestCode);
                break;
        }
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
        args.putBoolean(DISCOVERED_LIST_SELECTED_KEY, isDiscoveredList);
        //When a KuldigaLocation is clicked open the details fragment
        mLocationDetailFragment = new LocationDetailFragment();
        mLocationDetailFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //Add the transaction to the back stack so the back button works correctly
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.locations_container, mLocationDetailFragment,
                FRAGMENT_DETAIL_TAG).commit();
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
        //Check which fragments exists for the update
        if (mLocationDetailFragment != null){
            //If locations detail fragment is visible
            double distance = mLocationUtility.calculateDistance(locationOpenedInDetailFragment, location);
            mLocationDetailFragment.updateDistanceTv(distance);
        }
        if (mHiddenLocationsListFragment != null) {
            //Check if the list fragment's views have been initialized
            //This fixes crashing on orientation change in details fragment
            if (mHiddenLocationsListFragment.locationsRv != null) {
                //get list of all coordinates for KuldigaLocation objects in the visible list
                ArrayList<String> coordinatesList = mHiddenLocationsListFragment.getAllLocationCoordinates();
                //Distances are calculated in an asyncTask, because there might be an unknown number of them
                new CalculateDistancesTask(this).execute(coordinatesList, location);
            }

        }
    }

    /*
    * Method called from list fragment when a new location gets added from firebase
    * Makes the activity calculate distances from the previously received location
     * Gets distances for the list from the previously gotten location
     *check if the previous locatin was saved
    * */
    @Override
    public void calculatePreviousLocation() {
        if (lastKnownLocation != null && mHiddenLocationsListFragment != null) {
            ArrayList<String> coordinatesList = mHiddenLocationsListFragment.getAllLocationCoordinates();
            new CalculateDistancesTask(this).execute(coordinatesList, lastKnownLocation);
        }
    }

    //Called from the list fragment when the user changes the list from the options menu
    @Override
    public void listTypeChanged(boolean isDiscoveredList) {
        this.isDiscoveredList = isDiscoveredList;
    }

    //Calculated the distances to locations from String coordinates
    private static class CalculateDistancesTask extends AsyncTask<Object,Void, ArrayList<Double>>{

        private WeakReference<HiddenLocationsActivity> activityWeakReference;

        public CalculateDistancesTask(HiddenLocationsActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected ArrayList<Double> doInBackground(Object... params) {
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
            }
            return distanceList;
        }

        @Override
        protected void onPostExecute(ArrayList<Double> distances) {
            HiddenLocationsActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            //update the distances in the hidden locationsList fragment
            if (activity.mHiddenLocationsListFragment != null) {
                activity.mHiddenLocationsListFragment.updateDistancesInList(distances);
            }
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
    }

    //This gets called from the details fragment on initialization
    @Override
    public int getLocationUtilitiesState() {
        return mCurrentLocationAvailability;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_DISCOVERED, isDiscoveredList);
        outState.putInt(STATE_CURRENT_LOCATION_AVAILIBILITY, mCurrentLocationAvailability);
        if (lastKnownLocation != null) {
            outState.putParcelable(STATE_LAST_KNOWN_LOCATION, lastKnownLocation);
        }
        if (locationOpenedInDetailFragment != null) {
            outState.putSerializable(STATE_OPENED_KULDIGA_LOCATION, locationOpenedInDetailFragment);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isDiscoveredList = savedInstanceState.getBoolean(STATE_IS_DISCOVERED);
        mCurrentLocationAvailability = savedInstanceState.getInt(STATE_CURRENT_LOCATION_AVAILIBILITY);
        if (savedInstanceState.containsKey(STATE_LAST_KNOWN_LOCATION)) {
            lastKnownLocation = savedInstanceState.getParcelable(STATE_LAST_KNOWN_LOCATION);
        }
        if (savedInstanceState.containsKey(STATE_OPENED_KULDIGA_LOCATION)) {
            locationOpenedInDetailFragment = (KuldigaLocation) savedInstanceState
                    .getSerializable(STATE_OPENED_KULDIGA_LOCATION);
        }
    }
}
