package com.example.ivars.kuldigatour.UI;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ivars.kuldigatour.Adapters.LocationAdapter;
import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.example.ivars.kuldigatour.Utilities.LocationUtility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationsListFragment extends Fragment
    implements LocationAdapter.LocationListItemClickListener{

    private static final String TAG = LocationsListFragment.class.getSimpleName();
    private static final String DISCOVERED_LIST_SELECTED_INTENT_KEY = "discovered_list_key";
    private static final String LIST_TYPE_STATE_KEY = "list_type_state_key";
    private static final String RECYCLER_VIEW_STATE_KEY = "recycler_view_save_state_key";
    //Delay after which the scroll position of the recycler view is restored
    private static final int SCROLL_TO_STORED_POSITION_DELAY_MS = 200;
    //Number of items available in the FB db. Would be better to get this from FB istself
    private static final int NUMBER_OF_ITEMS_IN_DB = 10;
    //STATIC variables which save the recyclerView position for restoring when returning to the list
    //A variable to store the last position of the recycler view
    private static int lastFirstVisiblePosition = -1;

    //Helps us access the DB in general
    private FirebaseDatabase mFireBaseDb;
    //Helps us access the specific locations DB
    private DatabaseReference mLocationsDbRef;
    //Used to read from the DB
    private ChildEventListener mChildEventListener;

    private LocationAdapter mLocationAdapter;
    //A variable to store how many items were in the list the previous time it was opened
    private static int numberOfItemsInList = -1;
    Parcelable rvState;
    @BindView(R.id.list_fragment_toolbar)
    Toolbar toolbar;

    //Interface to pass the clicks to the activity
    private LocationItemClickListener mCallback;
    @BindView(R.id.empty_list_tv)
    TextView emptyListTv;
    @BindView(R.id.list_progress_bar)
    ProgressBar listProgressBar;
    private ArrayList<KuldigaLocation> mKuldigaDiscoveredLocationList;

    @BindView(R.id.hidden_locations_rv)
    RecyclerView locationsRv;
    private ArrayList<KuldigaLocation> mKuldigaHiddenLocationList;
    private Boolean isDiscoveredList;
    //VARIABLE TO CHECK WHEN ALL LOCATIONS HAVE BEEN DOWNLOADED FROM FBdb
    private int loacationsDownloaded = 0;

    //Called when a fragment is first attached to its context.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        //Make sure the host implements the callback
        try{
            mCallback = (LocationItemClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnItemClickListener");
        }
    }

    ListFragmentsInterface detailsFragmentCallback;

    //obligatory empty constructor
    public LocationsListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    //Similar to onCreate for an activity
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View rootView = inflater.inflate(R.layout.fragment_locations_list, container, false);
        ButterKnife.bind(this, rootView);

        //Show the preogress bar while the app connects to FBdb
        listProgressBar.setVisibility(View.VISIBLE);

        //get this value only when the fragment is first created
        if (isDiscoveredList == null) {
            isDiscoveredList = getArguments().getBoolean(DISCOVERED_LIST_SELECTED_INTENT_KEY, false);
        }

        Log.d(TAG, "is discovered list: " + isDiscoveredList);

        //get the interface
        detailsFragmentCallback = (ListFragmentsInterface) getActivity();

        setUpToolbar(isDiscoveredList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        locationsRv.setLayoutManager(layoutManager);
        mKuldigaDiscoveredLocationList = new ArrayList<KuldigaLocation>();
        mKuldigaHiddenLocationList = new ArrayList<KuldigaLocation>();
        if (isDiscoveredList) {
            mLocationAdapter = new LocationAdapter(getContext(), isDiscoveredList,
                    mKuldigaDiscoveredLocationList, this);
        } else {
            mLocationAdapter = new LocationAdapter(getContext(), isDiscoveredList,
                    mKuldigaHiddenLocationList, this);
        }

        locationsRv.setAdapter(mLocationAdapter);

        mFireBaseDb = FirebaseDatabase.getInstance();
        mLocationsDbRef = mFireBaseDb.getReference().child("Locations");

        mChildEventListener = new ChildEventListener() {
            //Triggered for every child message when the listener is attached
            //Triggered for every new child message
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //dataSnapshot contains data from the FB DB.
                //If an object has the same fields as the DB, it can be dirrectly read into it
                KuldigaLocation kuldigaLocation = dataSnapshot.getValue(KuldigaLocation.class);
                //add discovered locations to the discovered list and hidden to hidden list
                if (LocationUtility.isLocationDiscovered(kuldigaLocation, getActivity())) {
                    Log.d(TAG, "This location is discovered in preferences");
                    mKuldigaDiscoveredLocationList.add(kuldigaLocation);
                    //make the activity calculate distances when a new object to the list gets added
                    detailsFragmentCallback.calculatePreviousLocation();
                } else {
                    Log.d(TAG, "This location is NOT discovered in preferences");
                    mKuldigaHiddenLocationList.add(kuldigaLocation);
                    //make the activity calculate distances when a new object to the list gets added
                    detailsFragmentCallback.calculatePreviousLocation();
                }
                checkForEmptyLists();
                mLocationAdapter.notifyDataSetChanged();
                //As soon as the first location has been found set the progress bar to invisible
                listProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            //Error when making changes, no permission to read data
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //enable the options menu to allow switching between lists
        setHasOptionsMenu(true);

        //Add the changes listener to the database
        mLocationsDbRef.addChildEventListener(mChildEventListener);
        return rootView;
    }

    //Checks if all locations hae been downloaded ffrom FB db and if are then set the empty list
    //text in case it is necessary when all locations are found or hidden
    private void checkForEmptyLists() {
        loacationsDownloaded++;
        if (loacationsDownloaded >= NUMBER_OF_ITEMS_IN_DB) {
            //All locations have been downloaded
            setListUi();
            //Reset the locations count for the next time the fragment will be opened
            loacationsDownloaded = 0;
        }
    }

    private void setListUi() {
        if (isDiscoveredList) {
            //If the discovered list is open check if that is empty
            if (mKuldigaDiscoveredLocationList.size() <= 0) {
                //If the discovered list is empty show text indicating that
                emptyListTv.setVisibility(View.VISIBLE);
                emptyListTv.setText("There are no discovered locations\nSee the hidden locations list");
            } else {
                emptyListTv.setVisibility(View.INVISIBLE);
            }
        } else {
            //If the hidden list is open check if that is empty
            if (mKuldigaHiddenLocationList.size() <= 0) {
                //If the hidden list is empty show text indicating that
                emptyListTv.setVisibility(View.VISIBLE);
                emptyListTv.setText("Congratulations, all locations found!\nReset all locations to hidden from the main menu");
            } else {
                emptyListTv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setUpToolbar(boolean isDiscoveredList) {
        //Set the title in the toolbar depending on the list
        if (isDiscoveredList) {
            toolbar.setTitle("Discovered list");
        } else {
            toolbar.setTitle("Hidden list");
        }
        ((HiddenLocationsActivity) getActivity()).setSupportActionBar(toolbar);
        ((HiddenLocationsActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((HiddenLocationsActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void OnLocationClickListener(int clickedLocation) {
        //Pass the click event to the activity
        if (isDiscoveredList) {
            mCallback.onLocationClicked(mKuldigaDiscoveredLocationList.get(clickedLocation));
        } else {
            mCallback.onLocationClicked(mKuldigaHiddenLocationList.get(clickedLocation));
        }
    }

    public interface LocationItemClickListener {
        void onLocationClicked(KuldigaLocation kuldigaLocation);
    }

    //Pass all location coordinates to the activity from the list of Locations
    public ArrayList<String> getAllLocationCoordinates(){
        //go through all locations currently in the list and pass them to the activity
        ArrayList<String> coordinatesList = new ArrayList<>();
        int numberOfItemsInList;
        if (isDiscoveredList) {
            numberOfItemsInList = mKuldigaDiscoveredLocationList.size();
        } else {
            numberOfItemsInList = mKuldigaHiddenLocationList.size();
        }
        Log.d(TAG, "getAllLocationCoordinates() nr of: " + numberOfItemsInList);
        for (int i = 0; i < numberOfItemsInList; i++){
            KuldigaLocation location;
            if (isDiscoveredList) {
                location = mKuldigaDiscoveredLocationList.get(i);
            } else {
                location = mKuldigaHiddenLocationList.get(i);
            }
            coordinatesList.add(location.getCoordinates());
            Log.d("TEST", location.getCoordinates());
        }

        return coordinatesList;
    }

    //Update all list items with the distance to location
    public void updateDistancesInList (ArrayList<Double> distancesList){
        //It is possible that more locations have been found after the coordinates list is created
        //That is why distancesList is checked for size and not mKuldigaDiscoveredLocationList
        int numberOfItems = distancesList.size();
        Log.d(TAG, "updateDistancesInList number of items:" + numberOfItems);
        for (int i = 0; i < numberOfItems; i++){
            if (isDiscoveredList) {
                mKuldigaDiscoveredLocationList.get(i).setDistance(distancesList.get(i));
            } else {
                mKuldigaHiddenLocationList.get(i).setDistance(distancesList.get(i));
            }
        }
        mLocationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_lists:
                Log.d(TAG, "onOptionsItemSelected switch_lists");
                switchDisplayedList();
                break;
        }
        return false;
    }

    //Change if the hidden or discovered locations are displayed
    private void switchDisplayedList() {
        if (isDiscoveredList) {
            toolbar.setTitle("Hidden list");
            mLocationAdapter.changeList(mKuldigaHiddenLocationList);
        } else {
            toolbar.setTitle("Discovered list");
            mLocationAdapter.changeList(mKuldigaDiscoveredLocationList);
        }
        isDiscoveredList = !isDiscoveredList;
        detailsFragmentCallback.listTypeChanged(isDiscoveredList);
        //Chech if the empty list text needs to be displayed
        setListUi();
        mLocationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        lastFirstVisiblePosition = ((LinearLayoutManager) locationsRv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        rvState = locationsRv.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //restoreRecyclerViewScrollPosition();
    }

    //RECYCLER_VIEW_STATE_KEY

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");
        if (rvState != null) {
            Log.d(TAG, "Saved state != null");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    locationsRv.getLayoutManager().onRestoreInstanceState(rvState);
                }
            }, SCROLL_TO_STORED_POSITION_DELAY_MS);
        } else {
            Log.d(TAG, "Saved state == null");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        //save the state of the recyclerView
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY, locationsRv.getLayoutManager().onSaveInstanceState());
        //Save the list type opened
        outState.putBoolean(LIST_TYPE_STATE_KEY, isDiscoveredList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    interface ListFragmentsInterface {
        void calculatePreviousLocation();

        void listTypeChanged(boolean isDiscoveredList);
    }
}
