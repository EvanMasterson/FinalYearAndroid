package emasterson.finalyearandroid;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserInfo {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private String firstName, lastName, email, phone;
    private Double latitude, longitude, zoneLatitude, zoneLongitude;
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private UserInfoListener listener;

    public UserInfo(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference().child(auth.getCurrentUser().getUid());
    }

    public void setEventListener(UserInfoListener listener){
        this.listener = listener;
    }

    public void getUserData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getKey().equals("latitude")){
                        latitude = Double.parseDouble(data.getValue().toString());
                    }
                    if(data.getKey().equals("longitude")){
                        longitude = Double.parseDouble(data.getValue().toString());
                    }
                    if(data.getKey().equals("firstName")){
                        firstName = data.getValue().toString();
                    }
                    if(data.getKey().equals("lastName")){
                        lastName = data.getValue().toString();
                    }
                    if(data.getKey().equals("email")){
                        email = data.getValue().toString();
                    }
                    if(data.getKey().equals("phone")){
                        phone = data.getValue().toString();
                    }
                    if(data.getKey().equals("zones")){
                        for(DataSnapshot zones : data.getChildren()){
                            ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
                            try {
                                JSONArray jsonArray = new JSONArray(zones.getValue().toString());
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    zoneLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                                    zoneLongitude = Double.parseDouble(jsonObject.getString("longitude"));
                                    LatLng point = new LatLng(zoneLatitude, zoneLongitude);
                                    listGeofencePoints.add(point);
                                }
                                completeZoneList.add(listGeofencePoints);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                listener.onEvent();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w("Failed to read value.", databaseError.toException());
            }
        });
    }

    public void addZone(ArrayList<LatLng> zone){
        dbRef.child("zones").push().setValue(zone);
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public ArrayList<ArrayList<LatLng>> getUserZones(){
        return completeZoneList;
    }
}
