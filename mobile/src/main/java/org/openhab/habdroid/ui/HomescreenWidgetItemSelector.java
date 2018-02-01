package org.openhab.habdroid.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.openhab.habdroid.R;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.util.HomescreenWidgetUtils;

import java.util.ArrayList;

public class HomescreenWidgetItemSelector extends Activity implements View.OnKeyListener{

    private ListView itemSearchResults;
    private EditText itemSearch;
    ArrayList<OpenHABItem> itemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen_widget_item_selector);
        setResult(RESULT_CANCELED);


        itemSearch = (EditText) findViewById(R.id.itemSearch);
        itemSearchResults = (ListView) findViewById(R.id.itemSearchResults);


        itemSearch.setOnKeyListener(this);

        new DownloadJSON().execute();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        ((OpenhabItemListAdapter)itemSearchResults.getAdapter()).setFilter(itemSearch.getText().toString());

        return true;
    }


    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        JSONArray jsonarray;

        @Override
        protected Void doInBackground(Void... params) {


            itemList = new ArrayList<OpenHABItem>();

            jsonarray = HomescreenWidgetUtils
                    .getJSONArrayFromURL(getApplicationContext(), "rest/items?type=Switch&recursive=true");

            try {

                for (int i = 0; i < jsonarray.length(); i++) {
                    itemList.add(new OpenHABItem(jsonarray.getJSONObject(i)));
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            // Spinner adapter
            itemSearchResults
                    .setAdapter(new OpenhabItemListAdapter(getApplicationContext(),
                            itemList));

            itemSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent resultIntent = new Intent();

                    resultIntent.putExtra("name", itemList.get(position).getName());
                    resultIntent.putExtra("icon", itemList.get(position).getCategory());
                    resultIntent.putExtra("link", itemList.get(position).getLink());
                    resultIntent.putExtra("type", itemList.get(position).getType());
                    resultIntent.putExtra("label", itemList.get(position).getLabel());


                    setResult(RESULT_OK, resultIntent);

                    finish();
                }


            });
        }
    }
}
