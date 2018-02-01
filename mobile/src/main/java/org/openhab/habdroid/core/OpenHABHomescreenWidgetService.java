/*
 * Copyright (c) 2010-2018, openHAB.org and others.
 *
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.habdroid.core;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;


import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.habdroid.ui.HomescreenWidgetProvider;
import org.openhab.habdroid.util.Constants;
import org.openhab.habdroid.util.HomescreenWidgetUpdateJob;
import org.openhab.habdroid.util.HomescreenWidgetUtils;
import org.openhab.habdroid.util.MyAsyncHttpClient;
import org.openhab.habdroid.util.MyHttpClient;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;


import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;


public class OpenHABHomescreenWidgetService extends Service{

    private static final String TAG = OpenHABHomescreenWidgetService.class.getSimpleName();

    private MyAsyncHttpClient mAsyncHttpClient;
    private String mAtmosphereTrackingId;
    private ServerSentEvent sse;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String username = mSettings.getString(Constants.PREFERENCE_LOCAL_USERNAME, null);
        String password = mSettings.getString(Constants.PREFERENCE_LOCAL_PASSWORD, null);
        String baseURL = mSettings.getString(Constants.PREFERENCE_URL, null);
        subscribeForChangesSSE(baseURL + (!baseURL.endsWith("/") ? "/" : "" ) + "rest/events", username, password);
    }

    public void subscribeForChangesSSE(final String url, final String username, final String password){
        Request request = new Request.Builder().url(url).build();

        String credential = Credentials.basic(username, password);
        request = request.newBuilder().header("Authorization", credential).build();

        OkSse okSse = new OkSse();

        sse = okSse.newServerSentEvent(request, new ServerSentEvent.Listener() {
            @Override
            public void onOpen(ServerSentEvent sse, Response response) {
                // When the channel is opened
                Log.d("SSE-open", "OPEN");
            }

            @Override
            public void onMessage(ServerSentEvent sse, String id, String event, String message) {

                JSONObject jObject;
                try {
                    jObject = new JSONObject(message);

                    int ids[] = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(),HomescreenWidgetProvider.class));
                    String wigetName;

                    for(int i : ids){
                        wigetName = HomescreenWidgetUtils.loadWidgetPrefs(getApplicationContext(), i, "name");
                        if(wigetName != null && jObject.getString("topic").equals("smarthome/items/" + wigetName + "/state")) {
                            String lastState = HomescreenWidgetUtils.loadWidgetPrefs(getApplicationContext(), i, "lastState");
                            JSONObject payload = jObject.getJSONObject("payload");
                            if(!payload.getString("value").equals(lastState)) {
                                new HomescreenWidgetUpdateJob(getApplicationContext(), i).execute();
                            }
                        }
                    }

                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }



            }

            @WorkerThread
            @Override
            public void onComment(ServerSentEvent sse, String comment) {
                Log.d("SSE-comment", comment);
            }

            @WorkerThread
            @Override
            public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                return true; // True to use the new retry time received by SSE
            }

            @WorkerThread
            @Override
            public boolean onRetryError(ServerSentEvent sse, Throwable throwable, Response response) {
                return true; // True to retry, false otherwise
            }

            @WorkerThread
            @Override
            public void onClosed(ServerSentEvent sse) {
                // Channel closed
            }

            @Override
            public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
                return null;
            }
        });
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}


}
