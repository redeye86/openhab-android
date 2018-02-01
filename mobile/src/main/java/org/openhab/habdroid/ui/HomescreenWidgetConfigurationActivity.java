package org.openhab.habdroid.ui;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.util.Constants;
import org.openhab.habdroid.util.HomescreenWidgetUpdateJob;
import org.openhab.habdroid.util.HomescreenWidgetUtils;
import org.openhab.habdroid.util.MyWebImage;

import java.util.ArrayList;

public class HomescreenWidgetConfigurationActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    private static final String TAG = HomescreenWidgetConfigurationActivity.class.getSimpleName();


    int mAppWidgetId = -1;
    private Spinner itemName;
    private EditText itemNameSearchField;
    private EditText itemLabel;
    private Spinner iconSpinner;
    private Spinner pinModeSpinner;
    private EditText pinText;

    private String selectedIcon = "switch";
    private String selectedItem = "";

    private int REQUEST_CODE_ITEM = 3;
    private int REQUEST_CODE_ICON = 4;
    private ImageButton iconSelectorButton;

    ArrayList<OpenHABItem> itemList;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String baseURL = mSettings.getString(Constants.PREFERENCE_URL, null);
        String username = mSettings.getString(Constants.PREFERENCE_LOCAL_USERNAME, null);
        String password = mSettings.getString(Constants.PREFERENCE_LOCAL_PASSWORD, null);


        if (requestCode == REQUEST_CODE_ITEM) {

            if (resultCode == Activity.RESULT_OK) {
                selectedItem = data.getStringExtra("name");

                String label = data.getStringExtra("label");
                if(!label.equals("")) {
                    itemLabel.setText(label);
                }else{
                    itemLabel.setText(data.getStringExtra("type"));
                }

                if(data.getStringExtra("icon").equals("")){
                    selectedIcon = "switch";
                }else {
                    selectedIcon = data.getStringExtra("icon");
                }

                MyWebImage iconImg = new MyWebImage(baseURL + (!baseURL.endsWith("/") ? "/" : "" ) + "icon/"+selectedIcon, username, password);
                iconSelectorButton.setImageBitmap(iconImg.getBitmap(this));



            } else if (resultCode == Activity.RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }else if(requestCode == REQUEST_CODE_ICON){
            if (resultCode == Activity.RESULT_OK) {
                if(data.getStringExtra("icon").equals("")){
                    selectedIcon = "switch";
                }else {
                    selectedIcon = data.getStringExtra("icon");
                }


                MyWebImage iconImg = new MyWebImage(baseURL + (!baseURL.endsWith("/") ? "/" : "" ) + "icon/" + selectedIcon, username, password);
                iconSelectorButton.setImageBitmap(iconImg.getBitmap(this));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setResult(RESULT_CANCELED);


        setContentView(R.layout.activity_homescreen_widget_configuration);


        //itemName = (EditText) findViewById(R.id.itemName);
        itemLabel = (EditText) findViewById(R.id.itemLabel);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }


        Button b = (Button) findViewById(R.id.buttonSaveWidget);
        b.setOnClickListener(this);

        pinText = (EditText) findViewById(R.id.pinText);




        iconSelectorButton = (ImageButton) findViewById(R.id.icon_picker_button);
        iconSelectorButton.setOnClickListener(this);



        pinModeSpinner = (Spinner) findViewById(R.id.pinMode);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.pin_modes, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pinModeSpinner.setAdapter(adapter2);
        pinModeSpinner.setOnItemSelectedListener(this);



        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        String baseURL = mSettings.getString(Constants.PREFERENCE_URL, null);
        String username = mSettings.getString(Constants.PREFERENCE_LOCAL_USERNAME, null);
        String password = mSettings.getString(Constants.PREFERENCE_LOCAL_PASSWORD, null);


        Intent i = new Intent(this, HomescreenWidgetItemSelector.class);
        startActivityForResult(i,REQUEST_CODE_ITEM);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.icon_picker_button:
                Intent i = new Intent(this, HomescreenWidgetIconPicker.class);
                startActivityForResult(i,REQUEST_CODE_ICON);

                break;
            case R.id.buttonSaveWidget:

                final Context context = HomescreenWidgetConfigurationActivity.this;

                HomescreenWidgetUtils.saveWidgetPrefs(context, mAppWidgetId, "name", selectedItem);
                HomescreenWidgetUtils.saveWidgetPrefs(context, mAppWidgetId, "label", itemLabel.getText().toString());
                HomescreenWidgetUtils.saveWidgetPrefs(context, mAppWidgetId, "icon", selectedIcon);
                HomescreenWidgetUtils.saveWidgetPrefs(context, mAppWidgetId, "pin", pinText.getText().toString());
                HomescreenWidgetUtils.saveWidgetPrefs(context, mAppWidgetId, "pinmode", pinModeSpinner.getSelectedItem().toString());


                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

                new HomescreenWidgetUpdateJob(context, appWidgetManager, mAppWidgetId).execute();

                //TODO: save max widget id



                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();

                break;


        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pinText.setEnabled(position > 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        pinText.setEnabled(false);
    }

}
