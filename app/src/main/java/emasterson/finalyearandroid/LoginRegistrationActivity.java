package emasterson.finalyearandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    @Reference
    https://github.com/firebase/quickstart-android/tree/master/auth
    https://firebase.google.com/docs/auth/android/password-auth?authuser=0
 */
public class LoginRegistrationActivity extends AppCompatActivity {

    private Button loginBtn, verifyBtn;
    private TextView forgotPasswordTV;
    private TextView signupTV;
    private EditText emailET;
    private EditText passwordET;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,40})";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);
        getSupportActionBar().setTitle(R.string.title_login);

        loginBtn = findViewById(R.id.loginBtn);
        verifyBtn = findViewById(R.id.verifyBtn);
        forgotPasswordTV = findViewById(R.id.forgotPasswordTV);
        signupTV = findViewById(R.id.signupTV);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);

        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(emailET.getText().toString(), passwordET.getText().toString());
            }
        });

        signupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(emailET.getText().toString(), passwordET.getText().toString());
            }
        });

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword(emailET.getText().toString());
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail(emailET.getText().toString());
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        user = auth.getCurrentUser();
        confirmUser(user);
    }

    private void signIn(String email, String password) {
        if (!validateEmail(email) && !validatePassword(password)) {
            return;
        }
        showProgressDialog();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkIfEmailVerified();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            confirmUser(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void createAccount(final String email, String password) {
        if (!validateEmail(email) && !validatePassword(password)) {
            return;
        }
        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendVerificationEmail(email);
                            user = auth.getCurrentUser();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference dbRef = database.getReference().child(user.getUid());
                            dbRef.child("email").setValue(email);
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            confirmUser(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void sendVerificationEmail(String email){
        user = auth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    auth.signOut();
                    overridePendingTransition(0, 0);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    Toast.makeText(getApplicationContext(), "Email sent...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to send email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkIfEmailVerified(){
        user = auth.getCurrentUser();
        if(user.isEmailVerified()){
            confirmUser(user);
        } else {
            Toast.makeText(getApplicationContext(), "Email not verified.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword(final String email){
        showProgressDialog();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Email sent...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "User:" + email + " does not exist.", Toast.LENGTH_LONG).show();
                }
                hideProgressDialog();
            }
        });
    }

    private boolean validateEmail(String email) {
        boolean valid = false;
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if(matcher.matches()){
            valid = true;
        } else {
            emailET.setError("Invalid Email");
            Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
        }
        return valid;
    }

    private boolean validatePassword(String password) {
        boolean valid = false;
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);

        if(matcher.matches()){
            valid = true;
        } else {
            passwordET.setError("Invalid Password");
            Toast.makeText(getApplicationContext(), "Password must be 8-40 characters long and contain AT LEAST\nONE upper case, ONE lower case and ONE number", Toast.LENGTH_LONG).show();
        }
        return valid;
    }

    private void confirmUser(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            sendTokenId();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }

    private void sendTokenId(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tokenId = preferences.getString("tokenId", "");
        user = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference().child(user.getUid());
        dbRef.child("tokenId").setValue(tokenId);
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
