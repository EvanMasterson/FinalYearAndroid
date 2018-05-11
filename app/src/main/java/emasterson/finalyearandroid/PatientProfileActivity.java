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

public class PatientProfileActivity extends BaseActivity {
    private Boolean input;
    private EditText nameET, ageET, aboutET;
    private ImageButton imageButton;
    private UserInfo userInfo;
    private FirebaseUser user;

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

    public void updateInfo(String name, String age, String about){
        userInfo.setPatientName(name);
        userInfo.setPatientAge(age);
        userInfo.setPatientAbout(about);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            Uri uri = data.getData();
            imageButton.setImageURI(uri);
            updatePicture(uri);
        }
    }

    public void updatePicture(Uri uri){
        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        System.out.println(uri);
        user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Updated patient image", Toast.LENGTH_LONG).show();
            }
        });
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            finish();
            startActivity(getIntent());
        }
    }
}
