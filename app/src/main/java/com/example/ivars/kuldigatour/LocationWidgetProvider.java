package com.example.ivars.kuldigatour;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.ivars.kuldigatour.UI.MainMenuActivity;

/**
 * Implementation of App Widget functionality.
 */
public class LocationWidgetProvider extends AppWidgetProvider {

    //shared preference for storing the number of locations found
    private static final String NUM_LOCATIONS_DISCOVERED_KEY = "number_of_locations_discovered";
    private static final String SHARED_PREFS_NAME = "Kuldiga_tour_app_shared_preferences";
    private static final int NUMBER_OF_ITEMS_IN_DB = 10;

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.location_widget_provider);
        //

        //Make clicking on the widget bring the user to the app
        Intent intent = new Intent(context, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);


        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME,
                Context.MODE_PRIVATE);
        //Update the number of items discovered
        int numDiscoveredLocations = sharedPreferences.getInt(NUM_LOCATIONS_DISCOVERED_KEY, 0);
        views.setTextViewText(R.id.widget_text, numDiscoveredLocations + "/" + NUMBER_OF_ITEMS_IN_DB);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    //A method than initiates the widget update
    public static void updateLocationWidgets(Context context, AppWidgetManager appWidgetManager,
                                             int[] widgetIds) {
        for (int appWidgetId : widgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    //CALLED WHEN NEW WIDGET IS CREATED OR WHEN THE UPDATE PERIOD EXPIRES
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

