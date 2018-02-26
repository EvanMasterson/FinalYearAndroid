package emasterson.finalyearandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeofenceActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener{
    private Double latitude, longitude;
    private GoogleMap gMap;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private LatLng point, previousPoint, watch;
    private PolygonOptions polygonOptions;
    private PolylineOptions polylineOptions;
    private Polyline polyline;
    private Polygon polygon;
    private ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
    private ArrayList<Polyline> polylineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference().child(auth.getCurrentUser().getUid());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setOnMapLongClickListener(this);
        gMap.setOnMarkerClickListener(this);
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

                    watch = new LatLng(latitude, longitude);
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

        listGeofencePoints.add(point);
        gMap.addMarker(new MarkerOptions().position(point).title(point.toString()));

        if(listGeofencePoints.size() > 1){
            polylineOptions = new PolylineOptions().color(Color.BLUE);
            polylineOptions.add(point);
            previousPoint = listGeofencePoints.get(listGeofencePoints.size() - 2);
            polylineOptions.add(previousPoint);
            polyline = gMap.addPolyline(polylineOptions);
            polylineList.add(polyline);

            if(listGeofencePoints.size() > 2){
                polylineOptions.add(listGeofencePoints.get(0));
                polyline = gMap.addPolyline(polylineOptions);
                polylineList.add(polyline);
                createPolygon();
            }
        }
//        }
        Toast.makeText(getApplicationContext(), "Click marker's to remove", Toast.LENGTH_LONG).show();
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        final TextView radiusTV = new TextView(this);
//        radiusTV.setText("Radius");
//        final EditText radiusET = new EditText(this);
//        radiusET.setHint("Radius");
//        layout.addView(radiusTV);
//        layout.addView(radiusET);

//        LayoutInflater inflater = this.getLayoutInflater();
//        final View view = inflater.inflate(R.layout.geofence_settings_dialog, null);
//
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//        alert.setTitle("Geofence");
//        alert.setMessage("Edit Geofence Settings:");
//        alert.setView(view);
//        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                gMap.addMarker(new MarkerOptions().position(point).title("You clicked here"));
//                dialog.dismiss();
//            }
//        });
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        alert.show();

    }

    @Override
    public boolean onMarkerClick(Marker marker){
        for(int i=0; i<listGeofencePoints.size(); i++){
            if(listGeofencePoints.get(i).equals(marker.getPosition())){
                if(polygon != null){
                    polygon.remove();
                }
                listGeofencePoints.remove(i);
                marker.remove();
                reDrawPolylines();
            }
        }
        return true;
    }

    public void reDrawPolylines(){
        polylineOptions = new PolylineOptions().color(Color.BLUE);

        for(Polyline line : polylineList){
            line.remove();
        }
        polylineList.clear();
        for(int i = 0; i<listGeofencePoints.size(); i++) {
            polylineOptions.add(listGeofencePoints.get(i));
            polyline = gMap.addPolyline(polylineOptions);
            polylineList.add(polyline);
        }
    }

    public void createPolygon() {
        polygonOptions = new PolygonOptions();
        polygonOptions.addAll(listGeofencePoints);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(Color.GREEN);
        polygonOptions.strokeWidth(7);
        polygon = gMap.addPolygon(polygonOptions);
    }

    public void savePolygon(){

    }
}
