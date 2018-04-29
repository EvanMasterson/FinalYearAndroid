package emasterson.finalyearandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GeofenceActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolygonClickListener, AdapterView.OnItemSelectedListener{
    private Double latitude, longitude;
    private GoogleMap gMap;
    private LatLng point, previousPoint;
    private Polyline polyline;
    private Polygon polygon;
    private ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private ArrayList<Polyline> polylineList = new ArrayList<>();
    private ArrayList<Polygon> polygonList = new ArrayList<>();
    private View view;
    private String zoneColour;
    private Spinner zoneSpinner;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);
        view = findViewById(R.id.geofence_settings_layout);
        view.setVisibility(View.INVISIBLE);
        userInfo = new UserInfo();

        LayoutInflater inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.geofence_settings_dialog, null);
        zoneSpinner = view.findViewById(R.id.zoneSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.zone, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(adapter);
        zoneSpinner.setOnItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        zoneColour = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setOnMapLongClickListener(this);
        gMap.setOnMarkerClickListener(this);
        gMap.setOnPolygonClickListener(this);
        gMap.setIndoorEnabled(true);
        userInfo.getUserData();
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

    @Override
    public void onMapLongClick(LatLng location){
        point = location;
        listGeofencePoints.add(point);
        gMap.addMarker(new MarkerOptions().position(point).title(point.toString()));

        if(listGeofencePoints.size() > 1){
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE);
            polylineOptions.add(point);
            previousPoint = listGeofencePoints.get(listGeofencePoints.size() - 2);
            polylineOptions.add(previousPoint);
            polyline = gMap.addPolyline(polylineOptions);
            polylineList.add(polyline);

            if(listGeofencePoints.size() > 2){
                polylineOptions.add(listGeofencePoints.get(0));
                polyline = gMap.addPolyline(polylineOptions);
                polylineList.add(polyline);
                createPolygon(listGeofencePoints);
            }
        }
        Toast.makeText(getApplicationContext(), "Click marker's to remove", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        for(int i=0; i<listGeofencePoints.size(); i++){
            if(listGeofencePoints.get(i).equals(marker.getPosition())){
                for(Polygon polygon : polygonList){
                    polygon.remove();
                }
                listGeofencePoints.remove(i);
                marker.remove();
                reDrawPolylines();
            }
        }
        return true;
    }

    @Override
    public void onPolygonClick(final Polygon polygon) {
        System.out.println("Clicked");
        view.setVisibility(View.VISIBLE);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Geofence");
        alert.setMessage("Edit Geofence Settings:");
        alert.setView(view);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                view.setVisibility(View.INVISIBLE);
                ((ViewGroup) view.getParent()).removeView(view);
                reDrawPolygon(polygon, zoneColour);
                dialog.dismiss();
                savePolygon();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ViewGroup) view.getParent()).removeView(view);
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public void reDrawPolygon(Polygon clickedPolygon, String zoneColour){
        clickedPolygon.remove();
        int fillColor = 0;
        if(zoneColour.equals("Green")){
            fillColor = 0x5000ff00;
        } else if(zoneColour.equals("Yellow")) {
            fillColor = 0x50ffff00;
        } else if(zoneColour.equals("Red")){
            fillColor = 0x50ff0000;
        }
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(polygon.getPoints());
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(fillColor);
        polygonOptions.strokeWidth(7);
        Polygon polygon = gMap.addPolygon(polygonOptions);
        polygon.setClickable(true);
        polygonList.add(polygon);
    }

    public void reDrawPolylines(){
        PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE);

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

    public void createPolygon(ArrayList<LatLng> zoneList) {
        System.out.println(zoneList);
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(zoneList);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.fillColor(0x5000ff00);
        polygonOptions.strokeWidth(7);
        polygon = gMap.addPolygon(polygonOptions);
        polygon.setClickable(true);
        polygonList.add(polygon);
    }

    public void savePolygon(){
        userInfo.addZone(listGeofencePoints);
        listGeofencePoints.clear();
    }
}
