package org.openhab.habdroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class HomescreenWidgetSendCommandJob<Void> extends AsyncTask {
    private static final String TAG = HomescreenWidgetSendCommandJob.class.getSimpleName();

    private Context context;
    private String item;
    private String command;


    public HomescreenWidgetSendCommandJob(Context context, String item,
                                          String command){
        this.context = context;
        this.item = item;
        this.command = command;
    }


    protected Void doInBackground(Object[] params) {

            Log.d(TAG, "Sending comamnd " + command + " to item " + item);


            SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(context);
            String username = mSettings.getString(Constants.PREFERENCE_LOCAL_USERNAME, null);
            String password = mSettings.getString(Constants.PREFERENCE_LOCAL_PASSWORD, null);
            String baseURL = mSettings.getString(Constants.PREFERENCE_URL, null);


        //TODO: username + password

            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(baseURL + (!baseURL.endsWith("/") ? "/" : "" ) + "rest/items/" + item);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "text/plain");
                urlConnection.setRequestProperty("Method", "POST");


                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);

                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                //out.write(("state="+command).getBytes(Charset.forName("UTF-8")));
                final PrintStream printStream = new PrintStream(out);
                printStream.print(command);
                printStream.close();


                int status = urlConnection.getResponseCode();

                Log.d(TAG, "Result: " + status);

            }catch (Exception e){
                e.printStackTrace();
            }


            if(urlConnection != null) {
                urlConnection.disconnect();
            }


        return null;
    }



}