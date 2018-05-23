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

/*
    This activity is responsible for showing the user the main view of the application
    Contains a google map with real-time update of current location of watch
    Displays already defined geofence zones
 */
public class MainActivity extends BaseActivity implements OnMapReadyCallback{
    // Declaration of variables
    private Button geofenceBtn, extraInfoBtn;
    private Double latitude, longitude;
    private GoogleMap gMap;
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private ArrayList<String> completeZoneColourList = new ArrayList<>();
    private UserInfo userInfo;

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userInfo = new UserInfo();

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

    /*
        Responsible for intialising the google map
        Contains event listener for UserInfo, retrieves relevant data such as
        current latitude/longitude and list of already defined zones
        Updates map with information
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setIndoorEnabled(true);
        userInfo.getUserData();
        userInfo.setEventListener(new UserInfoListener() {
            @Override
            public void onEvent() {
                latitude = userInfo.getLatitude();
                longitude = userInfo.getLongitude();
                completeZoneList = userInfo.getZones();
                completeZoneColourList = userInfo.getZoneColours();

                if(latitude != null && longitude != null) {
                    gMap.clear();
                    LatLng watch = new LatLng(latitude, longitude);
                    gMap.addMarker(new MarkerOptions().position(watch).title(watch.toString()));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 16.0f));
                }

                if(completeZoneList != null && completeZoneColourList != null &&(completeZoneList.size()==completeZoneColourList.size())){
                    for(int i=0; i<completeZoneList.size(); i++){
                        createPolygon(completeZoneList.get(i), completeZoneColourList.get(i));
                    }
                }
            }
        });
    }

    /*
        Responsible for creating the polygons on the google map
        Called from within the event listener if zones exist
     */
    public void createPolygon(ArrayList<LatLng> zoneList, String zoneColour) {
        int fillColor = 0;
        if(zoneColour.equals("Green")){
            fillColor = 0x5000ff00;
        } else if(zoneColour.equals("Yellow")) {
            fillColor = 0x50ffff00;
        } else if(zoneColour.equals("Red")){
            fillColor = 0x50ff0000;
        }
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(zoneList);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(fillColor);
        polygonOptions.strokeWidth(7);
        gMap.addPolygon(polygonOptions);
    }
}
