package com.example.ivars.kuldigatour.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ivars.kuldigatour.Adapters.LocationAdapter;
import com.example.ivars.kuldigatour.Objects.KuldigaLocation;
import com.example.ivars.kuldigatour.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HiddenLocationsListFragment extends Fragment
    implements LocationAdapter.LocationListItemClickListener{

    private static final String TAG = HiddenLocationsListFragment.class.getSimpleName();

    private static final int HIDDEN_LIST_TYPE = 1;
    private static final int DISCOVERED_LIST_TYPE = 2;

    //Helps us access the DB in general
    private FirebaseDatabase mFireBaseDb;
    //Helps us access the specific locations DB
    private DatabaseReference mLocationsDbRef;
    //Used to read from the DB
    private ChildEventListener mChildEventListener;

    private LocationAdapter mLocationAdapter;
    private ArrayList<KuldigaLocation> mKuldigaLocationList;

    //Interface to pass the clicks to the activity
    private LocationItemClickListener mCallback;

    @BindView(R.id.hidden_locations_rv)
    RecyclerView locationsRv;

    //obligatory empty constructor
    public HiddenLocationsListFragment() {
    }

    //Called when a fragment is first attached to its context.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Make sure the host implements the callback
        try{
            mCallback = (LocationItemClickListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnItemClickListener");
        }

    }

    //Similar to onCreate for an activity
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_hidden_locations_list, container, false);

        ButterKnife.bind(this, rootView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        locationsRv.setLayoutManager(layoutManager);
        mKuldigaLocationList = new ArrayList<KuldigaLocation>();
        mLocationAdapter = new LocationAdapter(getContext(), HIDDEN_LIST_TYPE, mKuldigaLocationList, this);
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
                //TODO: if kuldiga location is found add to found, is hidden add to hidden
                mKuldigaLocationList.add(kuldigaLocation);
                mLocationAdapter.notifyDataSetChanged();
                Log.d(TAG, kuldigaLocation.toString());
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

        //Add the changes listener to the database
        mLocationsDbRef.addChildEventListener(mChildEventListener);
        return rootView;
    }

    public interface LocationItemClickListener{
        void onLocationClicked(KuldigaLocation kuldigaLocation);
    }

    public void OnLocationClickListener(int clickedLocation) {
        //Pass the click event to the activity
        mCallback.onLocationClicked(mKuldigaLocationList.get(clickedLocation));
    }

    //Pass all location coordinates to the activity
    public ArrayList<String> getAllLocationCoordinates(){
        //go through all locations currently in the list and pass them to the activity
        ArrayList<String> coordinatesList = new ArrayList<>();
        int numberOfItemsInList = mKuldigaLocationList.size();
        for (int i = 0; i < numberOfItemsInList; i++){
            KuldigaLocation location = mKuldigaLocationList.get(i);
            coordinatesList.add(location.getCoordinates());
            Log.d("TEST", location.getCoordinates());
        }
        return coordinatesList;
    }

    //Update all list items with the distance to location
    public void updateDistancesInList (ArrayList<Double> distancesList){
        //TODO: update items in list and then call notify dataSetChanged
        int numberOfItems = mKuldigaLocationList.size();
        for (int i = 0; i < numberOfItems; i++){
            mKuldigaLocationList.get(i).setDistance(distancesList.get(i));
        }
        mLocationAdapter.notifyDataSetChanged();
    }
}
