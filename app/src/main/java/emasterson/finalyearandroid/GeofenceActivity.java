package emasterson.finalyearandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class GeofenceActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{
    private Double latitude, longitude;
    private GoogleMap gMap;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private LatLng point;

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
        gMap.setOnMapLongClickListener(this);
        gMap.setIndoorEnabled(true);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
                Map<String, Object> objectHashMap = dataSnapshot.getValue(objectsGTypeInd);
                if(objectHashMap != null) {
                    ArrayList<Object> objectArrayList = new ArrayList(objectHashMap.values());
                    latitude = Double.parseDouble(objectArrayList.get(0).toString());
                    longitude = Double.parseDouble(objectArrayList.get(1).toString());

                    LatLng watch = new LatLng(latitude, longitude);
                    gMap.clear();
                    gMap.addMarker(new MarkerOptions().position(watch).title("Current Watch Location"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 16.0f));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w( "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onMapLongClick(LatLng location){
        point = location;
//        gMap.addMarker(new MarkerOptions().position(point).title("You clicked here"));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final TextView radiusTV = new TextView(this);
        radiusTV.setText("Radius");
        final EditText radiusET = new EditText(this);
        radiusET.setHint("Radius");
        layout.addView(radiusTV);
        layout.addView(radiusET);
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.geofence_settings_dialog, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Geofence");
        alert.setMessage("Edit Geofence Settings:");
        alert.setView(view);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gMap.addMarker(new MarkerOptions().position(point).title("You clicked here"));
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();

    }
}
