package emasterson.finalyearandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PatientProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
