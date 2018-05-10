package emasterson.finalyearandroid;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/*
    This activity is responsible for implementing the options menu
    It is extended by every other activity and allows the user to freely navigate through the application
 */
public class BaseActivity extends AppCompatActivity {
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_patient_profile:
                Intent patientProfileIntent = new Intent(getApplicationContext(), PatientProfileActivity.class);
                startActivity(patientProfileIntent);
                return true;
            case R.id.menu_user_profile:
                Intent profileIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(profileIntent);
                return true;
            case R.id.menu_logout:
                auth.signOut();
                Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                Intent logoutIntent = new Intent(getApplicationContext(), LoginRegistrationActivity.class);
                startActivity(logoutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
