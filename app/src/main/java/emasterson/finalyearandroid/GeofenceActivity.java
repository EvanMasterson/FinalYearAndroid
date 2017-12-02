package emasterson.finalyearandroid;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeofenceActivity extends BaseActivity implements OnMapReadyCallback{
    private Double latitude, longitude;
    private GoogleMap gMap;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference().child("location");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
                Map<String, Object> objectHashMap = dataSnapshot.getValue(objectsGTypeInd);
                ArrayList<Object> objectArrayList = new ArrayList(objectHashMap.values());

                latitude = Double.parseDouble(objectArrayList.get(0).toString());
                longitude = Double.parseDouble(objectArrayList.get(1).toString());

                LatLng watch = new LatLng(latitude, longitude);
                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(watch).title("Current Watch Location"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 15.0f));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w( "Failed to read value.", error.toException());
            }
        });
    }
}
