package com.example.ivars.kuldigatour.UI;


import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.example.ivars.kuldigatour.Utilities.LocationUtility;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationDetailFragment extends Fragment {

    private static final String DISCOVERED_LIST_SELECTED_KEY = "discovered_list_key";
    //The size the refresh icon should be at the end of the animation
    private static final int LOCATION_UPDATED_ANMIMATION_LEGHT = 500;
    //Request code for storage access
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 565;


    @BindView(R.id.hidden_detail_iv)
    ImageView locationIv;
    @BindView(R.id.detail_description_tv)
    TextView descriptionTv;


    @BindView(R.id.detail_distance_tv)
    TextView distanceTv;
    @BindView(R.id.detail_view_refresh_icon)
    ImageView refreshButtonIv;
    @BindView(R.id.detail_fragment_toolbar)
    Toolbar toolbar;
    @BindView(R.id.detail_fragment_collapsing_layout)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.share_fab)
    FloatingActionButton shareFab;

    private static final String TAG = LocationDetailFragment.class.getSimpleName();
    boolean isDiscovered = false;
    private KuldigaLocation mKuldigaLocation = null;

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
        final View rootView = inflater.inflate(R.layout.fragment_locations_detail_view, container, false);
        //hide the activities action bar in the detail fragment because the toolbar is used
        //((HiddenLocationsActivity) getActivity()).getSupportActionBar().hide();

        ButterKnife.bind(this, rootView);

        setUpToolbar();

        //get the passed in locations object
        mKuldigaLocation = new KuldigaLocation(
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

        isDiscovered = getArguments().getBoolean(DISCOVERED_LIST_SELECTED_KEY, false);
        if (isDiscovered){
            Log.d(TAG, "location is discovered");
            //if location discovered, show discovered atributes
            setUiWithDiscovereAtributes();
        } else {
            Log.d(TAG, "location is hidden");
            //if location hidden show hidden attributes
            setUiWithHiddenAtributes();
        }
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.CollapsedAppBarTextStyle);
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

        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLocation();
            }
        });

        return rootView;
    }

    private void setUiWithDiscovereAtributes() {
        descriptionTv.setText(mKuldigaLocation.getDiscoveredDescription());
        Picasso.get().load(mKuldigaLocation.getLargeImageUrl())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.image_download_error)
                .into(locationIv);
        collapsingToolbar.setTitle(mKuldigaLocation.getDiscoveredName());
        setHasOptionsMenu(false);
    }

    private void setUiWithHiddenAtributes() {
        descriptionTv.setText(mKuldigaLocation.getHiddenDescription());
        Picasso.get().load(mKuldigaLocation.getHiddenLargeImageUrl())
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.image_download_error)
                .into(locationIv);
        collapsingToolbar.setTitle(mKuldigaLocation.getHiddenName());
        //enable the options menu, which allows discovering of the location
        setHasOptionsMenu(true);
    }

    private void setLocationAsDiscovered() {
        LocationUtility.setLocationDiscovered(mKuldigaLocation, getActivity());
        Toast.makeText(getActivity(), "location found", Toast.LENGTH_SHORT).show();
        //todo: small delay???
        setUiWithDiscovereAtributes();
    }

    private void shareLocation() {
        //first check for permissions
        //Check if we have storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Build version >= M");
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission NOT granted");
                //External storage permission is not granted yet
                getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            } else {
                Log.d(TAG, "Permission granted");
            }
        }
        startShareIntent(getImageUri());
    }

    private void startShareIntent(Uri imageUri) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (isDiscovered) {
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "I discovered this beautiful place: " +
                            mKuldigaLocation.getDiscoveredName() +
                            " using the Kuldiga Tour app");
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Help me find " +
                            mKuldigaLocation.getHiddenName() +
                            "in Kuldiga");
        }
        //set the type of the intent depending on if we can get the image
        if (imageUri != null) {
            // Construct a ShareIntent with link to image
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
        } else {
            shareIntent.setType("text/plain");
        }
        startActivity(Intent.createChooser(shareIntent, "Share location"));
    }

    private Uri getImageUri() {
        Drawable locationImage = locationIv.getDrawable();
        if (locationImage == null) {
            //if the image has not been loaded return
            return null;
        }
        Bitmap mBitmap = ((BitmapDrawable) locationImage).getBitmap();
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                mBitmap, "Location image", null);
        Uri uri = Uri.parse(path);
        return uri;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, share with image Uri
                    startShareIntent(getImageUri());
                } else {
                    // permission denied, share without image Uri
                    startShareIntent(null);
                }
                return;
            }
        }
    }

    private void setUpToolbar() {
        ((HiddenLocationsActivity) getActivity()).setSupportActionBar(toolbar);
        ((HiddenLocationsActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((HiddenLocationsActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    //This method gets called from the activity to update the texview with the distance
    public void updateDistanceTv(double distance){
        distanceTv.setText("Distance: " + distance + " km");
        refreshIconAnimation();
    }

    //Makes the refresh Icon rotate 180 indicating to the user that the distance has been updated
    private void refreshIconAnimation() {
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(refreshButtonIv, "rotation", 0f, 360f);
        rotateAnim.setDuration(LOCATION_UPDATED_ANMIMATION_LEGHT); // miliseconds
        AnimatorSet refreshAnimation = new AnimatorSet();
        refreshAnimation.play(rotateAnim);
        refreshAnimation.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO:Orientation changes
        /*
        if (mRequestingLocationUpdates){
            startLocationUpdates();
        }
        */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_unlock:
                Log.d(TAG, "onOptionsItemSelected action_unlock");
                setLocationAsDiscovered();
                break;
        }
        return false;
    }

}
