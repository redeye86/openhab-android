package org.openhab.habdroid.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openhab.habdroid.R;
import org.openhab.habdroid.util.Constants;
import org.openhab.habdroid.util.MySmartImageView;

import java.util.ArrayList;


public class IconPickerAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<CharSequence> data = new ArrayList<CharSequence>();


    public IconPickerAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (MySmartImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String username = mSettings.getString(Constants.PREFERENCE_LOCAL_USERNAME, null);
        String password = mSettings.getString(Constants.PREFERENCE_LOCAL_PASSWORD, null);
        String baseURL = mSettings.getString(Constants.PREFERENCE_URL, null);

        CharSequence item = data.get(position);
        holder.imageTitle.setText(item);
        holder.image.setImageUrl(baseURL + (!baseURL.endsWith("/") ? "/" : "" ) + "icon/" + item, username, password);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        MySmartImageView image;
    }
}