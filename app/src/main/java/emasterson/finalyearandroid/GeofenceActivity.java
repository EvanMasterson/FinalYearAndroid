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

import java.util.ArrayList;

/*
    This activity is resonsible for allowing the user to create, update and delete geofence zones
    Contains google maps with various on click listeners
 */
public class GeofenceActivity extends BaseActivity implements OnMapReadyCallback,
                                                            GoogleMap.OnMapLongClickListener,
                                                            GoogleMap.OnMarkerClickListener,
                                                            GoogleMap.OnPolygonClickListener,
                                                            AdapterView.OnItemSelectedListener{
    // Declaration of variables
    private Double latitude, longitude;
    private GoogleMap gMap;
    private LatLng point;
    private Polygon polygon;
    private ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private ArrayList<String> completeZoneColourList = new ArrayList<>();
    private ArrayList<Polyline> polylineList = new ArrayList<>();
    private ArrayList<Polygon> polygonList = new ArrayList<>();
    private View view;
    private String zoneColour;
    private Spinner zoneSpinner;
    private UserInfo userInfo;

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
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

    /*
        Responsible for intialising the google map
        Contains event listener for UserInfo, retrieves relevant data such as
        current latitude/longitude and list of already defined zones
        Updates map with already existing information
     */
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
                completeZoneList = userInfo.getZones();
                completeZoneColourList = userInfo.getZoneColours();

                if(latitude != null && longitude != null) {
                    gMap.clear();
                    LatLng watch = new LatLng(latitude, longitude);
                    gMap.addMarker(new MarkerOptions().position(watch).title("Current Watch Location"));
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(watch, 16.0f));
                }

                if(!completeZoneList.isEmpty() && !completeZoneColourList.isEmpty() &&(completeZoneList.size()==completeZoneColourList.size())){
                    for(int i=0; i<completeZoneList.size(); i++){
                        createPolygon(completeZoneList.get(i), completeZoneColourList.get(i));
                    }
                }
            }
        });
    }

    /*
        Responsible for allowing the user to define geofence zones
        Stores points in an ArrayList and creates Polylines between them
        Once enough points are plotted, makes a call to createPolygon to show on map
     */
    @Override
    public void onMapLongClick(LatLng location){
        point = location;
        listGeofencePoints.add(point);
        gMap.addMarker(new MarkerOptions().position(point).title(point.toString()));

        reDrawPolylines();
        Toast.makeText(getApplicationContext(), "Click marker's to remove", Toast.LENGTH_SHORT).show();
    }

    /*
        Responsible for allowing the user to remove markers
        If marker is accidentally placed, user can click and remove geofence to start over
     */
    @Override
    public boolean onMarkerClick(Marker marker){
        for(int i=0; i<listGeofencePoints.size(); i++){
            if(listGeofencePoints.get(i).equals(marker.getPosition())){
                for(Polygon polygon : polygonList){
                    if(polygon.getPoints().contains(marker.getPosition())) {
                        polygon.remove();
                    }
                }
                listGeofencePoints.remove(i);
                marker.remove();
                reDrawPolylines();
            }
        }
        return true;
    }

    /*
        Responsible for enabling the user to pick the zone colour of the clicked polygon
        If polygon exists it means it is a geofence zone, displays new view containing
        spinner with zone colour options green/yellow/red
        On selection and save, makes a call to savePolygon which stores information in DB
        On cancellation, returns user back to view
     */
    @Override
    public void onPolygonClick(final Polygon polygon) {
        view.setVisibility(View.VISIBLE);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Geofence");
        alert.setMessage("Edit Geofence Settings:");
        alert.setView(view);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ViewGroup) view.getParent()).removeView(view);
                dialog.dismiss();
                savePolygon(zoneColour);
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

    /*
        Responsible for drawing the polylines of a geofence and making a call to createPolygon
        Clears any polylines that exist first before replotting them
     */
    public void reDrawPolylines(){
        PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE);
        Polyline polyline;

        for(Polyline line : polylineList){
            line.remove();
        }
        polylineList.clear();
        for(int i = 0; i<listGeofencePoints.size(); i++) {
            polylineOptions.add(listGeofencePoints.get(i));
            polyline = gMap.addPolyline(polylineOptions);
            polylineList.add(polyline);
        }
        if(listGeofencePoints.size() > 2){
            createPolygon(listGeofencePoints, "Green");
        }
    }

    /*
        Responsible for creating polygon on map
        Takes in list of points and colour of zone
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
        polygon = gMap.addPolygon(polygonOptions);
        polygon.setClickable(true);
        polygonList.add(polygon);
    }

    /*
        Responsible for saving desired polygon and pushing its information to Firebase DB
     */
    public void savePolygon(String zoneColour){
        userInfo.addZone(listGeofencePoints, zoneColour);
        finish();
        startActivity(getIntent());
    }
}
