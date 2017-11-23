package emasterson.finalyearandroid;

import android.os.Bundle;

public class GeofenceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
