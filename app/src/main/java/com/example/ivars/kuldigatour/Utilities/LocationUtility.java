package com.example.ivars.kuldigatour.Utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.ivars.kuldigatour.LocationWidgetProvider;
import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LocationUtility {

    //States for location availibility
    public static final int LOCATION_AVAILABLE_STATE = 0;
    //When user does not grant permission or enable settings
    public static final int LOCATION_NOT_AVAILABLE_STATE = 1;
    //When waiting for first location, getLastLocation returned null
    public static final int LOCATION_PENDING_STATE = 2;
    private static final String TAG = LocationUtility.class.getSimpleName();
    //Constant for calculating distance to location
    private static final double EARTH_RADIUS_KM = 6371;
    //Key for settings check request
    private static final int REQUEST_CHECK_SETTINGS = 2;
    //key for requesting location permission
    private static final int REQUEST_LOCATION_PERMISSION = 3;
    //Location update timing
    private static final int SECOND_IN_MILLS = 1000;
    private static final int LOCATION_UPDATE_REQUEST_FREQUENCY = 10 * SECOND_IN_MILLS;
    private static final int FASTEST_LOCATION_UPDATE = 5 * SECOND_IN_MILLS;
    //To check if onActivityResult is correct
    private static final int RESULT_OK = -1;
    //Numbers after coma for calculating distance
    private static final int ACCURACY_FOR_LIST = 1;
    private static final int ACCURACY_FOR_DETAIL = 2;
    //shared preference for storing the number of locations found
    private static final String NUM_LOCATIONS_DISCOVERED_KEY = "number_of_locations_discovered";
    private static final String SHARED_PREFS_NAME = "Kuldiga_tour_app_shared_preferences";
    //used to get regular location upates
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    //Interface object to call methods in LocationActivity
    private LocationInterface mLocationInterface;
    private Activity context;
    //Listener for getting the users action from the explanation message dialog
    private DialogInterface.OnClickListener explenationMessageListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    //The user has pressed on DON'T use button, turn off asking for permission
                    mLocationInterface.differentLocationState(LOCATION_NOT_AVAILABLE_STATE);
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    //The user has allowed to enable the location permission
                    askLocationPermission();
                    break;
                default:
                    Log.e(TAG, "Unexpected dialog answer");
                    break;
            }
        }
    };
    //Main entry point for interacting with the fused location provider
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //Constructor
    public LocationUtility(Activity context, LocationInterface locationInterface) {
        this.context = context;
        mLocationInterface = locationInterface;
    }

    //Saves the location as found in shared preferences
    public static void setLocationDiscovered(KuldigaLocation kuldigaLocation, Activity activity) {
        String locationName = kuldigaLocation.getDiscoveredName();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //Use the name of the location as key. Always set to true - this does not matter in this case
        editor.putBoolean(locationName, true);
        //Update the number of items discovered
        int numDiscoveredLocations = sharedPreferences.getInt(NUM_LOCATIONS_DISCOVERED_KEY, 0);
        numDiscoveredLocations++;
        editor.putInt(NUM_LOCATIONS_DISCOVERED_KEY, numDiscoveredLocations);
        editor.commit();
        //Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(activity, LocationWidgetProvider.class));
        LocationWidgetProvider.updateLocationWidgets(activity, appWidgetManager, appWidgetIds);

    }

    public static boolean isLocationDiscovered(KuldigaLocation kuldigaLocation, Activity activity) {
        if (activity != null) {
            //Activity can be null if swithcing back and forth between activities fast
            SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            if (sharedPreferences.contains(kuldigaLocation.getDiscoveredName())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    //Gets the double values of lat and lon from the Srting saved in the database
    private static double[] getLatLonFromCoordinates(String coordinates) {
        String[] currentLocLatLong = coordinates.split(",");
        currentLocLatLong[0] = currentLocLatLong[0].trim();
        currentLocLatLong[1] = currentLocLatLong[1].trim();
        return new double[]{
                Double.valueOf(currentLocLatLong[0]),
                Double.valueOf(currentLocLatLong[1])
        };
    }

    public void startLocationRequestProcess() {
        //get the fused location provider to get location
        //used for both getting last location and regular location updates
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        //Check if permissions are granted and ask for them if not
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Permissions are not granted
            //Check if we need too show the user an explanation why the app need location
            //Method checks if the user has already once denied location permission
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Show the user a dialog box where it is explained why location is necessary
                showPermissionExplenationMessage();
            } else {
                //No need to show explenation, just ask for the permission
                askLocationPermission();
            }
        } else {
            //Permissions granted
            getLastKnownLocation();
        }
    }

    /*
     * Get the last known loation and then start regular updates
     * If the Location is disabled on the device, then the location object will be null in onSuccess()
     * Device settings will be updated when starting regular location updates
     * Permission suppressed because they are checked before calling this method
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        mFusedLocationProviderClient.getLastLocation().
                addOnSuccessListener(context,
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(android.location.Location location) {
                                if (location != null) {
                                    //Return the last known location to the activity
                                    mLocationInterface.currentLocationCallback(location);
                                } else {
                                    //Location on device probably disabled, user will be asked to
                                    //enable when regular updates are started
                                    mLocationInterface.differentLocationState(LOCATION_PENDING_STATE);
                                }
                                //Start regular updates either way
                                startRegularLocationUpdates();
                            }
                        });
    }

    //Ask for permission which is necessary for the last known location update
    //The result will be first called in the locations activity
    private void askLocationPermission() {
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    private void startRegularLocationUpdates() {
        //check if the location request has not already been started
        if (mLocationRequest == null) {
            createLocationRequestObject();
            createLocationCallback();
            checkDeviceSettingsForRegularUpdates();
        }
    }

    //cretes an object that will be used for requests to the locations provider
    private void createLocationRequestObject() {
        //Used to get regular updates of location
        mLocationRequest = new LocationRequest();
        //How often this object will try to request location
        mLocationRequest.setInterval(LOCATION_UPDATE_REQUEST_FREQUENCY);
        //Other apps may be requesting location also, so not to overload our app set this
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE);
        //Uses a lot of battery but accurate
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //A message dialog box for showing explenation for location permission
    //Is displayed when the user has already once declined to give location permission
    private void showPermissionExplenationMessage() {
        new AlertDialog.Builder(context)
                .setMessage(context.getResources().getString(R.string.location_permission_explenation))
                .setPositiveButton(R.string.enable, explenationMessageListener)
                .setNegativeButton(R.string.dont_use, explenationMessageListener)
                .create()
                .show();
    }

    /*
    onActivityResult will be called in the location activity, from there
    * this method in the locationUtility object will be called with the same fields
    * */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    //Proper settings have been setup, start location updates
                    requestLocationUpdates();
                } else {
                    //location settings are not met
                    mLocationInterface.differentLocationState(LOCATION_NOT_AVAILABLE_STATE);
                }
                break;
            default:
                Log.e(TAG, "Unknown requestCode in onActivityResult");
        }
    }

    //Request regular location updates
    //Missing Permissions surpressed because permissions are checked before this method is called
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, null);
    }

    //calculate distance if the coordinates are given directly as a string
    public double calculateDistance(String coordinates, Location location) {
        double locLatLon[] = getLatLonFromCoordinates(coordinates);
        return harvestineFormula(location.getLatitude(),
                location.getLongitude(),
                locLatLon[0],
                locLatLon[1],
                ACCURACY_FOR_LIST);
    }

    //Calculate the distance between the current location and the KuldigaLocation object
    public double calculateDistance(KuldigaLocation kuldigaLocation, Location location) {
        double locLatLon[] = getLatLonFromCoordinates(kuldigaLocation.getCoordinates());
        return harvestineFormula(location.getLatitude(),
                location.getLongitude(),
                locLatLon[0],
                locLatLon[1],
                ACCURACY_FOR_DETAIL);
    }

    private double harvestineFormula(double usersLat, double usersLong,
                                     double locLat, double locLong,
                                     int accuracy) {
        //haversine formula
        double deltaLat = Math.toRadians(usersLat - locLat);
        double deltaLon = Math.toRadians(usersLong - locLong);
        double userLatRadians = Math.toRadians(usersLat);
        double locLatRadians = Math.toRadians(locLat);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2) *
                        Math.cos(userLatRadians) * Math.cos(locLatRadians);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;
        double shortenedDouble = BigDecimal.valueOf(distance)
                .setScale(accuracy, RoundingMode.HALF_UP)
                .doubleValue();
        return shortenedDouble;
    }

    //Method takes the created location request and checks if the device's settings are appropriate
    private void checkDeviceSettingsForRegularUpdates() {
        //add the created locations request to the builder to check settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        //check whether the current location settings are satisfied
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        //respond if hte settings are appropriate or not
        task.addOnSuccessListener(context, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                requestLocationUpdates();
            }
        });

        task.addOnFailureListener(context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // KuldigaLocation settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(context,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        mLocationInterface.errorMessageCallback(context.getString(R.string.Failed_toChange_settings));
                    }
                }
            }
        });
    }

    /*
     * Get result for the location permission request
     * This method will be callled from locationsActivity, because the callback is first called there
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted, get last known location
                    getLastKnownLocation();
                } else {
                    //permission denied
                    mLocationInterface.differentLocationState(LOCATION_NOT_AVAILABLE_STATE);
                }
                break;
            default:
                Log.e(TAG, "Unknown permission result");
                break;
        }
    }

    //Callback which receives the latest location
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLocationInterface.currentLocationCallback(location);
                    }
                }
            }
        };
    }

    //Interface returns location to the activity or sends an error message
    public interface LocationInterface {
        //sends the current location to the locationsActivity
        void currentLocationCallback(Location location);

        //sends an error message to the locationsActivity
        void errorMessageCallback(String errorMessage);

        //sends a state message to locations activity in case location is not available or pending
        void differentLocationState(int locationState);
    }

}
