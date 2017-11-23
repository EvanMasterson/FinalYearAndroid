package emasterson.finalyearandroid;

import android.os.Bundle;

public class ExtraInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
