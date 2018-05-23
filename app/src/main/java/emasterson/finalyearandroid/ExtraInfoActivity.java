package emasterson.finalyearandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

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
import java.util.Date;

/*
    @Reference
    https://github.com/halfhp/androidplot
 */
/*
    This acitivty is responsible for displaying any extra information about the watch associated with the user
 */
public class ExtraInfoActivity extends BaseActivity {
    // Declaration of variables
    private UserInfo userInfo;
    private JSONArray heartRateInfo = new JSONArray();
    private XYPlot plot;

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        plot = findViewById(R.id.graph);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 2);

        userInfo = new UserInfo();
        userInfo.getUserData();
        /*
            Custom event listener to retrieve the latest information associated with the user from Firebase DB
            In this case we are calling getHeartRateInfo method
         */
        userInfo.setEventListener(new UserInfoListener() {
            @Override
            public void onEvent() {
                heartRateInfo = userInfo.getHeartRateInfo();
                ArrayList<Integer> heartRate = new ArrayList<>();
                ArrayList<Date> dateTime = new ArrayList<>();
                if(heartRateInfo != null) {
                    for (int i = 0; i < heartRateInfo.length(); i++) {
                        try {
                            int hr = Integer.valueOf(heartRateInfo.getJSONObject(i).getString("heart_rate"));
                            String dt = heartRateInfo.getJSONObject(i).getString("date_time");
                            long epoch = Long.parseLong(dt);
                            Date date = new Date(epoch * 1000);

                            heartRate.add(hr);
                            dateTime.add(date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(heartRate.size() >= 2880){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 240);
                    } else if(heartRate.size() >= 1440){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 120);
                    } else if(heartRate.size() >= 720){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 60);
                    } else if(heartRate.size() >= 360){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 30);
                    } else if(heartRate.size() >= 180){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 15);
                    } else if(heartRate.size() >= 90){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 8);
                    } else if(heartRate.size() >= 45){
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 4);
                    } else {
                        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 2);
                    }
                    updateGraph(heartRate, dateTime);
                } else {
                    Toast.makeText(getApplicationContext(), "No data recorded.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
        Responsible for updating the graph plot whenever the values change
        Called from inside the setEventListener for UserInfo when updated
        Clears the current plot and redefines series given ArrayLists passed in then redraws graph
     */
    public void updateGraph(ArrayList<Integer> heartRate, final ArrayList<Date> dateTime){
        plot.clear();
        XYSeries series = new SimpleXYSeries(heartRate, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series");
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.RED, Color.DKGRAY, null, null);
        seriesFormat.getVertexPaint().setStrokeWidth(30f);
        seriesFormat.setInterpolationParams(new CatmullRomInterpolator.Params(20, CatmullRomInterpolator.Type.Centripetal));
        /*
            Custom formatter to override default configuration
            Replaces Y axis label values of 1,2,3... etc with the Time value associated with each X value point
         */
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
