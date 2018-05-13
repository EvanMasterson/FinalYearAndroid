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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
    This class is responsible for retrieving all the information associated with a user from Firebase DB
    Using a custom event listener called UserInfoListener, activities that wish to retrieve data from this class
    must implement the event listener
 */
public class UserInfo {
    // Declaration of variables
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private String phone, zoneColour, patientName, patientAge, patientAbout;
    private Double latitude, longitude, zoneLatitude, zoneLongitude;
    private ArrayList<ArrayList<LatLng>> completeZoneList = new ArrayList<>();
    private ArrayList<String> completeZoneColourList = new ArrayList<>();
    private JSONArray heartRateInfo;
    private UserInfoListener listener;

    public UserInfo(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference().child(auth.getCurrentUser().getUid());
    }

    public void setEventListener(UserInfoListener listener){
        this.listener = listener;
    }

    /*
        Responsible for retrieving all of the information on the associate user and storing it in
        their relevant variables which can be access through getters below
        Makes use of JSONArrays and JSONObjects to access formatted data in the DB
     */
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
                    if(data.getKey().equals("patient_name")){
                        patientName = data.getValue().toString();
                    }
                    if(data.getKey().equals("patient_age")){
                        patientAge = data.getValue().toString();
                    }
                    if(data.getKey().equals("patient_about")){
                        patientAbout = data.getValue().toString();
                    }
                    if(data.getKey().equals("heart_rate")){
                        heartRateInfo = new JSONArray();
                        try {
                            JSONArray jsonArray = new JSONArray(data.getValue().toString());
                            for(int i=0; i<jsonArray.length(); i++){
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("heart_rate", jsonArray.getJSONObject(i).getString("heart_rate"));
                                jsonObject.put("date_time", jsonArray.getJSONObject(i).getString("date_time"));
                                heartRateInfo.put(i, jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    /*
        Getters and setters below allow other activities to retrieve information on the user
        or push new data to the DB such as adding a new geofence zone
     */
    // Push zone list to db, and add zone colour to end of list in db
    public void addZone(final List<LatLng> zone, final String zoneColour){
        if(completeZoneList.contains(zone)){
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getKey().equals("zones")) {
                            for(DataSnapshot zones : data.getChildren()){
                                ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
                                try {
                                    JSONArray jsonArray = new JSONArray(zones.getValue().toString());
                                    for(int i=0; i<jsonArray.length()-1; i++){
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        zoneLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                                        zoneLongitude = Double.parseDouble(jsonObject.getString("longitude"));
                                        LatLng point = new LatLng(zoneLatitude, zoneLongitude);
                                        listGeofencePoints.add(point);
                                    }
                                    if(listGeofencePoints.equals(zone)){
                                        dbRef.child("zones").child(zones.getKey()).setValue(zone);
                                        dbRef.child("zones").child(zones.getKey()).child(String.valueOf(zone.size())).child("colour").setValue(zoneColour);
                                    }
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            String size = String.valueOf(zone.size());
            String key = dbRef.child("zones").push().getKey();
            dbRef.child("zones").child(key).setValue(zone);
            dbRef.child("zones").child(key).child(size).child("colour").setValue(zoneColour);
        }
    }

    public void deleteZone(final List<LatLng> zone){
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getKey().equals("zones")) {
                        for(DataSnapshot zones : data.getChildren()){
                            ArrayList<LatLng> listGeofencePoints = new ArrayList<>();
                            try {
                                JSONArray jsonArray = new JSONArray(zones.getValue().toString());
                                for(int i=0; i<jsonArray.length()-1; i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    zoneLatitude = Double.parseDouble(jsonObject.getString("latitude"));
                                    zoneLongitude = Double.parseDouble(jsonObject.getString("longitude"));
                                    LatLng point = new LatLng(zoneLatitude, zoneLongitude);
                                    listGeofencePoints.add(point);
                                }
                                if(listGeofencePoints.equals(zone)){
                                    dbRef.child("zones").child(zones.getKey()).removeValue();
                                }
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setPhone(String phone){
        dbRef.child("phone").setValue(phone);
    }

    public void setAlertStatus(Boolean status){
        dbRef.child("notifications").setValue(status);
    }

    public void setPatientName(String name){
        dbRef.child("patient_name").setValue(name);
    }

    public void setPatientAge(String age){
        dbRef.child("patient_age").setValue(age);
    }

    public void setPatientAbout(String about){
        dbRef.child("patient_about").setValue(about);
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

    public JSONArray getHeartRateInfo(){
        return heartRateInfo;
    }

    public String getPatientName(){
        return patientName;
    }

    public String getPatientAge(){
        return patientAge;
    }

    public String getPatientAbout(){
        return patientAbout;
    }
}
