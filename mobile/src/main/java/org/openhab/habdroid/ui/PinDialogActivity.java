package org.openhab.habdroid.ui;

import android.app.Activity;
import android.os.Bundle;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.PinDialogListener;
import org.openhab.habdroid.util.HomescreenWidgetSendCommandJob;
import org.openhab.habdroid.util.HomescreenWidgetUpdateJob;
import org.openhab.habdroid.util.HomescreenWidgetUtils;

public class PinDialogActivity extends Activity implements PinDialogListener {

    private PinDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_dialog);

        int appWidgetId = Integer.parseInt(getIntent().getData().getLastPathSegment());

        String pin = HomescreenWidgetUtils.loadWidgetPrefs(getApplicationContext(), appWidgetId, "pin");

        pd = new PinDialog(this, pin);
        pd.show();
    }

    @Override
    public void onPinEntered(String pin) {

        String item = getIntent().getStringExtra("item_name");
        String command = getIntent().getStringExtra("item_command");

        new HomescreenWidgetSendCommandJob(getApplicationContext(), item, command).execute();
        new HomescreenWidgetUpdateJob(getApplicationContext(), Integer.parseInt(getIntent().getData().getLastPathSegment())).execute();


        this.finish();
    }

    @Override
    public void onPinAborted() {
        this.finish();
    }
}
