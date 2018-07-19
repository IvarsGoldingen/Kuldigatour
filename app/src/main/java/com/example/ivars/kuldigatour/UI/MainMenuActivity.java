package com.example.ivars.kuldigatour.UI;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ivars.kuldigatour.LocationWidgetProvider;
import com.example.ivars.kuldigatour.R;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = MainMenuActivity.class.getSimpleName();
    private static final String DISCOVERED_LIST_SELECTED_KEY = "discovered_list_key";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //shared preference for storing the number of locations found
    private static final String NUM_LOCATIONS_DISCOVERED_KEY = "number_of_locations_discovered";
    private static final String ADD_MOB_APP_ID = "ca-app-pub-3258699015124264~4246323818";
    private static final String SHARED_PREFS_NAME = "Kuldiga_tour_app_shared_preferences";
    //Number of items available in the FB db. Would be better to get this from FB istself
    private static final int NUMBER_OF_ITEMS_IN_DB = 10;

    @BindView(R.id.main_menu_discoverd_loc_btn)
    Button mDiscoveredLocBtn;
    @BindView(R.id.main_menu_hidden_loc_btn)
    Button mHiddenLocBtn;
    @BindView(R.id.main_menu_info_btn)
    Button mInfoButton;
    @BindView(R.id.main_menu_score_tv)
    TextView scoreTv;
    @BindView(R.id.main_menu_sv)
    ScrollView mainMenuSv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        ButterKnife.bind(this);

        mHiddenLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, HiddenLocationsActivity.class);
                intent.putExtra(DISCOVERED_LIST_SELECTED_KEY, false);
                startActivity(intent);
            }
        });

        mDiscoveredLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, HiddenLocationsActivity.class);
                intent.putExtra(DISCOVERED_LIST_SELECTED_KEY, true);
                startActivity(intent);
            }
        });

        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        //Initialize adds
        MobileAds.initialize(this, ADD_MOB_APP_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScoreText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset_locations:
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear().commit();
                updateScoreText();
                updateWidget();
                break;
        }
        return false;
    }

    private void updateScoreText() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        //Update the number of items discovered
        int numDiscoveredLocations = sharedPreferences.getInt(NUM_LOCATIONS_DISCOVERED_KEY, 0);
        String text = numDiscoveredLocations + "/" + NUMBER_OF_ITEMS_IN_DB;
        scoreTv.setText(text);
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, LocationWidgetProvider.class));
        LocationWidgetProvider.updateLocationWidgets(this, appWidgetManager, appWidgetIds);
    }
}
