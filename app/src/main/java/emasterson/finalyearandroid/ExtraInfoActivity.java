package emasterson.finalyearandroid;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.Calendar;

public class ExtraInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH);
        int day = calendar.get(calendar.DAY_OF_MONTH);

        DatePicker datePicker = findViewById(R.id.datePicker);
        GraphView graph = findViewById(R.id.graph);
        NumberFormat nf = NumberFormat.getInstance();
//        nf.setMinimumFractionDigits(2);
//        nf.setMinimumIntegerDigits(2);

//        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 68),
                new DataPoint(1, 78),
                new DataPoint(2, 72),
                new DataPoint(3, 84),
                new DataPoint(4, 65)
        });
        graph.addSeries(series);

        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            }
        });
    }
}
