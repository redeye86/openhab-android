/*
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.habdroid.ui;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.openhab.habdroid.core.OpenHABHomescreenWidgetService;
import org.openhab.habdroid.util.HomescreenWidgetSendCommandJob;
import org.openhab.habdroid.util.HomescreenWidgetUpdateJob;
import org.openhab.habdroid.util.HomescreenWidgetUtils;

/**
 * Implementation of App Widget functionality.
 */
public class HomescreenWidgetProvider extends AppWidgetProvider {
    private static final String TAG = HomescreenWidgetProvider.class.getSimpleName();

    public final static String ACTION_BUTTON_CLICKED = "ACTION_BUTTON_CLICKED";
    public final static String ACTION_STATUS_CHANGED = "ACTION_STATUS_CHANGED";



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        if(appWidgetIds.length > 0 && !isMyServiceRunning(context, OpenHABHomescreenWidgetService.class)){
            context.startService(new Intent(context, OpenHABHomescreenWidgetService.class));
        }else if(appWidgetIds.length == 0 && isMyServiceRunning(context, OpenHABHomescreenWidgetService.class)){
            context.stopService(new Intent(context, OpenHABHomescreenWidgetService.class));
        }

        for (int appWidgetId : appWidgetIds) {
            if(HomescreenWidgetUtils.loadWidgetPrefs(context, appWidgetId, "name") != null) {
                new HomescreenWidgetUpdateJob(context, appWidgetManager, appWidgetId).execute();
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "STARTNG WIDGET SERVICE");

        context.startService(new Intent(context, OpenHABHomescreenWidgetService.class));
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "STOPPING WIDGET SERVICE");

        context.stopService(new Intent(context, OpenHABHomescreenWidgetService.class));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");

        for (int appWidgetId : appWidgetIds) {
            SharedPreferences prefs = context.getSharedPreferences("widget_prefs", 0);
            prefs.edit().remove("widget_"+appWidgetId+"_label");
            prefs.edit().remove("widget_"+appWidgetId+"_name");
            prefs.edit().remove("widget_"+appWidgetId+"_icon");
            prefs.edit().remove("widget_"+appWidgetId+"_pin");
        }
        super.onDeleted(context, appWidgetIds);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WIDGET APP", intent.getAction());

        if (intent.getAction().equals(ACTION_BUTTON_CLICKED)){

            int appWidgetId = Integer.parseInt(intent.getData().getLastPathSegment());


            if(intent.hasExtra("item_name")) {

                String item = intent.getStringExtra("item_name");
                String command = intent.getStringExtra("item_command");

                String pin = HomescreenWidgetUtils.loadWidgetPrefs(context, appWidgetId, "pin");
                String pinMode = HomescreenWidgetUtils.loadWidgetPrefs(context, appWidgetId, "pinmode");


                if( pin != null && !pin.equals("")  && !pinMode.equals("Never") &&
                        (
                                (pinMode.equals("OnEnable") && command.equals("ON")) ||
                                        (pinMode.equals("OnDisable") && command.equals("OFF")) ||
                                        pinMode.equals("OnEnableAndDisable")
                        )
                    )
                {
                    Intent active = new Intent(context, PinDialogActivity.class);
                    active.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    active.setData(intent.getData());
                    active.putExtra("item_name", item);
                    active.putExtra("item_command", command);

                    context.startActivity(active);
                }else{
                    new HomescreenWidgetSendCommandJob(context, item, command).execute();
                    new HomescreenWidgetUpdateJob(context, Integer.parseInt(intent.getData().getLastPathSegment())).execute();
                }
            }



            if(!isMyServiceRunning(context, OpenHABHomescreenWidgetService.class)){
                context.startService(new Intent(context, OpenHABHomescreenWidgetService.class));
            }

        }else {
            super.onReceive(context, intent);
        }


    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {


        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}