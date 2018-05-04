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
    private String phone, zoneColour;
    private Double latitude, longitude, zoneLatitude, zoneLongitude;
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private ArrayList<String> completeZoneColourList = new ArrayList<>();
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
                    if(data.getKey().equals("phone")){
                        phone = data.getValue().toString();
                    }
                    if(data.getKey().equals("zones")){
                        completeZoneList.clear();
                        for(DataSnapshot zones : data.getChildren()){
                            ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
                            try {
                                JSONArray jsonArray = new JSONArray(zones.getValue().toString());
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if(i==jsonArray.length()-1){
                                        zoneColour = jsonObject.getString("colour");
                                        completeZoneColourList.add(zoneColour);
                                    } else {
                                        zoneLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                                        zoneLongitude = Double.parseDouble(jsonObject.getString("longitude"));
                                        LatLng point = new LatLng(zoneLatitude, zoneLongitude);
                                        listGeofencePoints.add(point);
                                    }
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

    // Push zone list to db, and add zone colour to end of list in db
    public void addZone(ArrayList<LatLng> zone, String zoneColour){
        String size = String.valueOf(zone.size());
        String key = dbRef.child("zones").push().getKey();
        dbRef.child("zones").child(key).setValue(zone);
        dbRef.child("zones").child(key).child(size).child("colour").setValue(zoneColour);
    }

    public void setPhone(String phone){
        dbRef.child("phone").setValue(phone);
    }

    public FirebaseAuth getAuth(){
        return auth;
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

    public ArrayList<ArrayList<LatLng>> getZones(){
        return completeZoneList;
    }

    public ArrayList<String> getZoneColours(){
        return completeZoneColourList;
    }
}
