package emasterson.finalyearandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExtraInfoActivity extends BaseActivity {
    private UserInfo userInfo;
    private JSONArray heartRateInfo = new JSONArray();
    private XYPlot plot;

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
        plot = findViewById(R.id.graph);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);

        userInfo = new UserInfo();
        userInfo.getUserData();
        userInfo.setEventListener(new UserInfoListener() {
            @Override
            public void onEvent() {
                heartRateInfo = userInfo.getHeartRateInfo();
                ArrayList<Integer> heartRate = new ArrayList<>();
                ArrayList<Date> dateTime = new ArrayList<>();
                for(int i=0; i<heartRateInfo.length(); i++){
                    try {
                        int hr = Integer.valueOf(heartRateInfo.getJSONObject(i).getString("heart_rate"));
                        String dt = heartRateInfo.getJSONObject(i).getString("date_time");
                        long epoch = Long.parseLong(dt);
                        Date date = new Date(epoch*1000);

                        heartRate.add(hr);
                        dateTime.add(date);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                updateGraph(heartRate, dateTime);
            }
        });

        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            }
        });
    }

    public void updateGraph(ArrayList<Integer> heartRate, final ArrayList<Date> dateTime){
        plot.clear();
        XYSeries series = new SimpleXYSeries(heartRate, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series");
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.RED, Color.DKGRAY, null, null);
        seriesFormat.getVertexPaint().setStrokeWidth(30f);
        seriesFormat.setInterpolationParams(new CatmullRomInterpolator.Params(20, CatmullRomInterpolator.Type.Centripetal));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                Date date = dateTime.get(i);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                return sdf.format(date, toAppendTo, pos);
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        plot.addSeries(series, seriesFormat);
        plot.redraw();
    }
}
