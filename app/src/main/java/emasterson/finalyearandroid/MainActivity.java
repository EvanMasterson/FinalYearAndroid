package emasterson.finalyearandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements OnMapReadyCallback{
    private Button geofenceBtn, extraInfoBtn;
    private Double latitude, longitude, zoneLatitude, zoneLongitude;
    private GoogleMap gMap;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private ArrayList<LatLng> listGeofencePoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference().child(auth.getCurrentUser().getUid());

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
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getKey().equals("latitude")){
                        latitude = Double.parseDouble(data.getValue().toString());
                    }
                    if(data.getKey().equals("longitude")){
                        longitude = Double.parseDouble(data.getValue().toString());
                    }
                    if(latitude != null && longitude != null) {
                        gMap.clear();
                        LatLng watch = new LatLng(latitude, longitude);
                        gMap.addMarker(new MarkerOptions().position(watch).title("Current Watch Location"));
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 16.0f));
                    }
                    if(data.getKey().equals("zones")){
                        for(DataSnapshot zones : data.getChildren()){
                            try {
                                JSONArray jsonArray = new JSONArray(zones.getValue().toString());
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    zoneLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                                    zoneLongitude = Double.parseDouble(jsonObject.getString("longitude"));
                                    LatLng point = new LatLng(zoneLatitude, zoneLongitude);
                                    listGeofencePoints.add(point);
                                }
                                createPolygon();
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Failed to read value.", error.toException());
            }
        });
    }

    public void createPolygon() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(listGeofencePoints);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(0x5000ff00);
        polygonOptions.strokeWidth(7);
        gMap.addPolygon(polygonOptions);
        listGeofencePoints.clear();
    }
}
