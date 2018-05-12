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
/*
    This activity is responsible for allowing users to sign up, login, reset password and resend verification emails
 */
public class LoginRegistrationActivity extends AppCompatActivity {
    // Declaration of variables

    private Button loginBtn, verifyBtn;
    private TextView forgotPasswordTV;
    private TextView signupTV;
    private EditText emailET;
    private EditText passwordET;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,40})";

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
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
                signIn(emailET.getText().toString(), passwordET.getText().toString());
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        user = auth.getCurrentUser();
        confirmUser(user);
    }

    /*
        Responsible for allowing the user to sign in via the loginBtn onClickListener
        Makes a call to validateEmail and validatePassword functions before making a call to Firebase
        Makes a call to Firebase API, requesting to signInWithEmailAndPassword
        On Success, makes a call to checkIfEmailVerified
        On Failure, displays message, confirms user as being null
     */
    public void signIn(String email, String password) {
        if (!validateEmail(email) || !validatePassword(password)) {
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

    /*
        Responsible for allowing the user to sign up via the signUpBtn onClickListener
        Makes a call to validateEmail and validatePassword functions before making a call to Firebase
        Makes a call to Firebase API, requesting to createUserWithEmailAndPassword
        On Success, makes a call to sendVerificationEmail, gets user object and sets default info in DB
        On Failure, displays message, confirms user as being null
     */
    private void createAccount(final String email, String password) {
        if (!validateEmail(email) || !validatePassword(password)) {
            return;
        }
        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendVerificationEmail();
                    user = auth.getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference dbRef = database.getReference().child(user.getUid());
                    dbRef.child("email").setValue(email);
                    dbRef.child("notifications").setValue(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    confirmUser(null);
                }
                hideProgressDialog();
            }
        });
    }

    /*
        Responsible for sending verification email to user on creating an account
        Activated by createAccount and checkIfEmailVerified which creates a user object
        The user object needs to exist to be able to send an email to that user
        Makes a call to Firebase API, requesting to sendEmailVerification
        On Success, signs the user out immediately as they are not yet verfied
        On Failure, displays message
     */
    private void sendVerificationEmail(){
        user = auth.getCurrentUser();
        if(user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        auth.signOut();
                        Toast.makeText(getApplicationContext(), "Verification Email sent...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to send email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /*
        Responsible for checking if the user has verified their email
        Activated by signIn, checks if user signing in is verified
        On Success, calls confirmUser
        On Failure, calls sendVerificationEmail which will send another email to the user
     */
    private void checkIfEmailVerified(){
        user = auth.getCurrentUser();
        if(user != null) {
            if (user.isEmailVerified()) {
                confirmUser(user);
            } else {
                sendVerificationEmail();
                Toast.makeText(getApplicationContext(), "Email not verified.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Responsible for resetting the users password
        Makes a call to Firebase API, requesting to sendPasswordResetEmail
        On Success, email is sent by Firebase and displays message informing user
        On Failure, displays message email does not exist
     */
    private void resetPassword(final String email){
        if(email == null){
            emailET.setError("Required.");
            return;
        }
        showProgressDialog();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Password Reset Email sent...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "User:" + email + " does not exist.", Toast.LENGTH_LONG).show();
                }
                hideProgressDialog();
            }
        });
    }

    /*
        Responsible for validating the email entered by the user
        Returns true if email is valid
        Return false if email is not valid, also displays error message
     */
    public boolean validateEmail(String email) {
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

    /*
        Responsible for validating the password entered by the user
        Returns true if password is valid
        Return false if password is not valid, also displays error message
     */
    public boolean validatePassword(String password) {
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

    /*
        Responsible for redirecting user to MainActivity
        Called is valid user exists
        If user is not null, makes a call to sendTokenId and starts intent
     */
    private void confirmUser(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            sendTokenId();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
    }

    /*
        Responsible for saving device tokenId
        TokenId is saved in shared preferences by MyFirebaseInstanceIDService class
        Pushes users tokenId to Firebase
        TokenId is used for sending push notifications to the device
     */
    private void sendTokenId(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tokenId = preferences.getString("tokenId", "");
        user = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference().child(user.getUid());
        dbRef.child("tokenId").setValue(tokenId);
    }

    public ProgressDialog mProgressDialog;

    /*
        Responsible for displaying loading message on screen when called
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /*
        Responsible for hiding loading message when called
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
