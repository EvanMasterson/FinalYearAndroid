package emasterson.finalyearandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileActivity extends BaseActivity {
    private EditText firstNameET, lastNameET, phoneET, emailET, passwordET, currentPasswordET, newPasswordET, repeatPasswordET;
    private Button saveBtn, emailBtn, passwordBtn;
    private UserInfo userInfo;
    private FirebaseUser user;
    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,40})";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userInfo = new UserInfo();
        user = userInfo.getAuth().getCurrentUser();

        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        phoneET = findViewById(R.id.phoneET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        currentPasswordET = findViewById(R.id.currentPasswordET);
        newPasswordET = findViewById(R.id.newPasswordET);
        repeatPasswordET = findViewById(R.id.repeatPasswordET);
        saveBtn = findViewById(R.id.saveBtn);
        emailBtn = findViewById(R.id.emailBtn);
        passwordBtn = findViewById(R.id.passwordBtn);

        getUserDetails();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInfo.setFirstName(firstNameET.getText().toString());
                userInfo.setLastName(lastNameET.getText().toString());
                userInfo.setPhone(phoneET.getText().toString());
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail(emailET.getText().toString(), passwordET.getText().toString());
                auth.signOut();
            }
        });

        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validatePasswordFields(newPasswordET.getText().toString(), repeatPasswordET.getText().toString())) {
                    user = userInfo.getAuth().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPasswordET.getText().toString());
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                user.updatePassword(newPasswordET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            currentPasswordET.setText(null);
                                            newPasswordET.setText(null);
                                            repeatPasswordET.setText(null);
                                            Toast.makeText(getApplicationContext(), "Password has been changed.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error changing password.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Error authenticating user", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void getUserDetails(){
        firstNameET.setText(userInfo.getFirstName());
        lastNameET.setText(userInfo.getLastName());
        phoneET.setText(userInfo.getPhone());
        emailET.setText(user.getEmail());
    }

    public Boolean validatePasswordFields(String newPass, String repeatPass){
        Boolean valid = false;
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcherNew = pattern.matcher(newPass);
        Matcher matcherRepeat = pattern.matcher(repeatPass);
        if(matcherNew.matches() && matcherRepeat.matches()){
            if(newPass.equals(repeatPass)) {
                valid = true;
            } else {
                newPasswordET.setError("Passwords MUST match");
                repeatPasswordET.setError("Passwords MUST match");
                Toast.makeText(getApplicationContext(), "Passwords must match!", Toast.LENGTH_LONG).show();
            }
        } else {
            newPasswordET.setError("Requirements not met");
            repeatPasswordET.setError("Requirements not met");
            Toast.makeText(getApplicationContext(), "Password must be 8-40 characters long and contain AT LEAST\nONE upper case, ONE lower case and ONE number", Toast.LENGTH_LONG).show();
        }
        return valid;
    }

    public void sendVerificationEmail(final String email, String password){
        user = auth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            auth.signOut();
                                            Intent logoutIntent = new Intent(getApplicationContext(), LoginRegistrationActivity.class);
                                            startActivity(logoutIntent);
                                            Toast.makeText(getApplicationContext(), "Verification Email sent...", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Unable to send email", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Error updating email", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Error authenticating user", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
