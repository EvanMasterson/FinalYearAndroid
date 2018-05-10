package emasterson.finalyearandroid.notifications;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/*
    This class is responsible for storing the tokenId of the users device in shared preferences
    This tokenId is accessed by LoginRegistrationActivity when confirming user
    This tokenId allows push notifications to be sent to the users device
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println("Token Refresh:"+refreshedToken);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString("tokenId", refreshedToken).apply();
    }
}
