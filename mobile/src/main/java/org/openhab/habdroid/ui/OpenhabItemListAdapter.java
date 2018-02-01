package org.openhab.habdroid.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.util.Constants;
import org.openhab.habdroid.util.MyWebImage;

import java.util.ArrayList;

/**
* Created by redeye on 30.10.16.
*/
public class OpenhabItemListAdapter extends BaseAdapter{


    Context context;
    ArrayList<OpenHABItem> data;
    ArrayList<OpenHABItem> filteredData;
    private static LayoutInflater inflater = null;
    private String filter = "";

    public OpenhabItemListAdapter(Context context, ArrayList<OpenHABItem> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        this.filteredData = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setFilter(String filter){
        this.filter = filter != null ? filter.toLowerCase() : "";
        if(filter == "" || filter == null){
            this.filteredData = data;
        }else{
            this.filteredData = new ArrayList<OpenHABItem>();
            for (OpenHABItem ohi: data) {
                if(ohi.getLabel().toLowerCase().contains(this.filter) || ohi.getName().toLowerCase().contains(this.filter)){
                    this.filteredData.add(ohi);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.openhabitemlist_item, null);
        TextView name = (TextView) vi.findViewById(R.id.itemPickerName);
        name.setText(filteredData.get(position).getName());

        TextView label = (TextView) vi.findViewById(R.id.itemPickerLabel);

        if(filteredData.get(position).getLabel() != null && filteredData.get(position).getLabel() != "" ){
            label.setText(filteredData.get(position).getLabel());
        }else {
            label.setText(filteredData.get(position).getType());
        }

        return vi;
    }
}
