package de.devmil.parrotzik2supercharge.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;


/**
 * Implementation of App Widget functionality.
 */
public class WidgetBase extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WidgetUpdateService.triggerUpdate(context);
    }


    @Override
    public void onEnabled(Context context) {
        WidgetUpdateService.triggerUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        WidgetUpdateService.stop(context);
    }

}


