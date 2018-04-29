package emasterson.finalyearandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements OnMapReadyCallback{
    private Button geofenceBtn, extraInfoBtn;
    private Double latitude, longitude;
    private GoogleMap gMap;
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userInfo = new UserInfo();
        userInfo.getUserData();

        geofenceBtn = findViewById(R.id.geofenceBtn);
        extraInfoBtn = findViewById(R.id.extraInfoBtn);

        geofenceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GeofenceActivity.class);
                startActivity(i);
            }
        });

        extraInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ExtraInfoActivity.class);
                startActivity(i);
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setIndoorEnabled(true);
        userInfo.setEventListener(new UserInfoListener() {
            @Override
            public void onEvent() {
                latitude = userInfo.getLatitude();
                longitude = userInfo.getLongitude();
                completeZoneList = userInfo.getUserZones();

                if(latitude != null && longitude != null) {
                    gMap.clear();
                    LatLng watch = new LatLng(latitude, longitude);
                    gMap.addMarker(new MarkerOptions().position(watch).title("Current Watch Location"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 16.0f));
                }

                if(!completeZoneList.isEmpty()){
                    for(int i=0; i<completeZoneList.size(); i++){
                        createPolygon(completeZoneList.get(i));
                    }
                }
            }
        });
    }

    public void createPolygon(ArrayList<LatLng> zoneList) {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(zoneList);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(0x5000ff00);
        polygonOptions.strokeWidth(7);
        gMap.addPolygon(polygonOptions);
    }
}
