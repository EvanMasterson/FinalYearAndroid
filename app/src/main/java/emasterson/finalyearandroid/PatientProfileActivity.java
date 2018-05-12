package emasterson.finalyearandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/*
    This activity is responsible for setting information about a patient
 */
public class PatientProfileActivity extends BaseActivity {
    // Declaration of variables
    private Boolean input;
    private EditText nameET, ageET, aboutET;
    private ImageButton imageButton;
    private UserInfo userInfo;
    private FirebaseUser user;

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        userInfo = new UserInfo();
        user = userInfo.getAuth().getCurrentUser();
        imageButton = findViewById(R.id.imageButton);
        nameET = findViewById(R.id.nameET);
        ageET = findViewById(R.id.ageET);
        aboutET = findViewById(R.id.aboutET);
        input = false;

        inputState(false);
        retrieveInfo();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input) {
                    updateInfo(nameET.getText().toString(), ageET.getText().toString(), aboutET.getText().toString());
                    inputState(false);
                    return;
                }
                inputState(true);
            }
        });

        Uri uri = user.getPhotoUrl();
        if(checkPermissions()) {
            if (uri != null) {
                imageButton.setImageURI(uri);
            }
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                gallery.setType("image/*");
                startActivityForResult(gallery, 0);
            }
        });
    }

    /*
        Responsible for checking the state of the edittext fields
        Allows them to be toggled via FloatingActionButton
     */
    public void inputState(Boolean state){
        if(state){
            nameET.setEnabled(true);
            ageET.setEnabled(true);
            aboutET.setEnabled(true);
            input = true;
            return;
        }
        nameET.setEnabled(false);
        ageET.setEnabled(false);
        aboutET.setEnabled(false);
        input = false;
    }

    /*
        Responsible for retrieving existing information about the patient
        Called on creation of activity
     */
    public void retrieveInfo(){
        userInfo.getUserData();
        userInfo.setEventListener(new UserInfoListener() {
            @Override
            public void onEvent() {
                nameET.setText(userInfo.getPatientName());
                ageET.setText(userInfo.getPatientAge());
                aboutET.setText(userInfo.getPatientAbout());
            }
        });
    }

    /*
        Responsible for updating any information about the patient
        Called on FloatingActionButton click when toggling input fields
     */
    public void updateInfo(String name, String age, String about){
        userInfo.setPatientName(name);
        userInfo.setPatientAge(age);
        userInfo.setPatientAbout(about);
    }

    /*
        Responsible for receiving the result of the intent passed in the ImageButton onClick
        Sets the image on the ImageButton to uri of image chosen from gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            imageButton.setImageURI(uri);
            updatePicture(uri);
        }
    }

    /*
        Responsible for updating and storing picture associated with the patient
        Creates a UserProfileChangeRequest as part of Firebase API to set the photoUri
     */
    public void updatePicture(Uri uri){
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Updated patient image", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to update patient image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
        Responsible for checking permission for READ_EXTERNAL_STORAGE
        If not permitted, user is prompted to allow permissions so they can choose/set patient photo
     */
    public boolean checkPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }

    /*
        Responsible for result of checkPermissions method
        If the user is prompted to enter permissions and accepts, then activity is reloaded and they can choose photo
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            finish();
            startActivity(getIntent());
        }
    }
}
