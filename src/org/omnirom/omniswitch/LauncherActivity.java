package org.omnirom.omniswitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent launch = new Intent(this, org.omnirom.omniswitch.SettingsActivity.class);
        startActivity(launch);
        finish();
    }
}
