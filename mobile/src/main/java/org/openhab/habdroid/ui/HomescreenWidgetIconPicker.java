package org.openhab.habdroid.ui;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import org.openhab.habdroid.R;

import java.util.ArrayList;
import java.util.Arrays;

public class HomescreenWidgetIconPicker extends AppCompatActivity implements AdapterView.OnItemClickListener {

    GridView iconGrid;
    ArrayList<CharSequence> icons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen_widget_icon_picker);

        iconGrid = (GridView) findViewById(R.id.icon_picker_container);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.openhab_icons, android.R.layout.simple_spinner_item);

        icons = new ArrayList<CharSequence>(Arrays.asList(getResources().getStringArray(R.array.openhab_icons)));
        iconGrid.setAdapter(new IconPickerAdapter(this,R.layout.icon_picker_item_layout,icons));

        iconGrid.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent resultIntent = new Intent();
        resultIntent.putExtra("icon", icons.get(position));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
