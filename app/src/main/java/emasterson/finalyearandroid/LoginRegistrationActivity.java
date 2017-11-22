package emasterson.finalyearandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
/*
    @Reference
    https://github.com/firebase/quickstart-android/tree/master/auth
    https://firebase.google.com/docs/auth/android/password-auth?authuser=0
 */
public class LoginRegistrationActivity extends AppCompatActivity {

    private Button loginBtn;
    private TextView forgotPasswordTV;
    private TextView signupTV;
    private EditText emailET;
    private EditText passwordET;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        loginBtn = findViewById(R.id.loginBtn);
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
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        confirmUser(currentUser);
    }

    private void signIn(String email, String password) {
        if (!validateEmail() && !validatePassword()) {
            return;
        }
        showProgressDialog();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = auth.getCurrentUser();
                            confirmUser(currentUser);
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            confirmUser(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void createAccount(String email, String password) {
        if (!validateEmail() && !validatePassword()) {
            return;
        }
        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = auth.getCurrentUser();
                            confirmUser(currentUser);
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            confirmUser(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void resetPassword(final String email){
        if (!validateEmail()) {
            return;
        }
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

    //TODO Proper email validation
    private boolean validateEmail() {
        boolean valid = true;

        String email = emailET.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Required.");
            valid = false;
        } else {
            emailET.setError(null);
        }
        return valid;
    }

    //TODO Proper password validation
    private boolean validatePassword() {
        boolean valid = true;

        String password = passwordET.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Required.");
            valid = false;
        } else {
            passwordET.setError(null);
        }
        return valid;
    }

    private void confirmUser(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
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
