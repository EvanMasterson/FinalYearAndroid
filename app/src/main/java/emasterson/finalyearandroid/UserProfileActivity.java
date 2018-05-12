package emasterson.finalyearandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    This activity is responsible for allowing the user to configure details about their account
    including toggling alerts on/off, opting into texts, changing email and changing password
 */
public class UserProfileActivity extends BaseActivity {
    // Declaration of variables
    private EditText phoneET, verifyCodeET, emailET, passwordET, currentPasswordET, newPasswordET, repeatPasswordET;
    private Button phoneBtn, verifyCodeBtn, emailBtn, passwordBtn;
    private ToggleButton alertTglBtn;
    private UserInfo userInfo;
    private FirebaseUser user;
    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,40})";
    private String phoneNumber, verifyCode, codeVerification;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallback;

    /*
        Responsible for instantiating all objects required in the class
        Responsible for setting onClickListener for Buttons
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userInfo = new UserInfo();
        user = userInfo.getAuth().getCurrentUser();

        phoneET = findViewById(R.id.phoneET);
        verifyCodeET = findViewById(R.id.verifyCodeET);
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        currentPasswordET = findViewById(R.id.currentPasswordET);
        newPasswordET = findViewById(R.id.newPasswordET);
        repeatPasswordET = findViewById(R.id.repeatPasswordET);
        phoneBtn = findViewById(R.id.phoneBtn);
        verifyCodeBtn = findViewById(R.id.verifyCodeBtn);
        emailBtn = findViewById(R.id.emailBtn);
        passwordBtn = findViewById(R.id.passwordBtn);
        alertTglBtn = findViewById(R.id.alertTglBtn);

        verifyCodeET.setVisibility(View.GONE);
        verifyCodeBtn.setVisibility(View.GONE);

        /*
            Responsible for allowing the user to opt into text alerts
            Must provide a valid phone number, if it doesn't already exist in the DB then a call
            to the verifyNumber method is made
         */
        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneET.getText().toString();
                if(!TextUtils.isEmpty(phoneNumber)) {
                    userInfo.getUserData();
                    userInfo.setEventListener(new UserInfoListener() {
                        @Override
                        public void onEvent() {
                            if(phoneNumber.equals(userInfo.getPhone())){
                                Toast.makeText(getApplicationContext(), phoneNumber + ", has already been setup for text alerts.", Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(getIntent());
                            } else {
                                verifyNumber();
                            }
                        }
                    });
                } else {
                    phoneET.setError("Required.");
                    Toast.makeText(getApplicationContext(), "Phone number required.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
            Responsible for accepting the verification code sent by Firebase
            Only shown once a user has successfully triggered the opt in option
            If they've entered the verification code, makes a call to verifyPhoneNumberWithCode
            If not, displays error message
         */
        verifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode = verifyCodeET.getText().toString();
                if(!TextUtils.isEmpty(verifyCode)) {
                    verifyPhoneNumberWithCode(codeVerification, verifyCode);
                } else {
                    verifyCodeET.setError("Required.");
                    Toast.makeText(getApplicationContext(), "Verification code required.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
            Responsible for triggering email change which is nested inside sendVerificationEmail
            Signs user out as they've opted to changed email
         */
        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail(emailET.getText().toString(), passwordET.getText().toString());
            }
        });

        /*
            Responsible for triggering password change
            Makes a call to changePassword which contains the implementation
         */
        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        /*
            Responsible for allowing the user to toggle alerts on/off
            If checked, makes a call to userInfo to setAlertStatus to true which updates in the DB
            if not checked, makes a call to userInfo to setAlertStatus to false which updates in the DB
         */
        alertTglBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    userInfo.setAlertStatus(true);
                    Toast.makeText(getApplicationContext(), "Notification alerts turned on.", Toast.LENGTH_LONG).show();
                } else {
                    userInfo.setAlertStatus(false);
                    Toast.makeText(getApplicationContext(), "Notification alerts turned off.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*
            Responsible for acting as callback to phone verification if requested by the user
            On Success, makes a call to userInfo to setPhone to update phone number in DB
            On Failure, displays message informing the user
            On CodeSent, disables opt in views and enables verification views
         */
        phoneCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(), "Successfully Verified Number: " + phoneNumber, Toast.LENGTH_SHORT).show();
                userInfo.setPhone(phoneNumber);
                finish();
                startActivity(getIntent());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(), "Error encountered verifying code", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token){
                Toast.makeText(getApplicationContext(), "Verification code sent to: " + phoneNumber, Toast.LENGTH_SHORT).show();
                codeVerification = verificationId;
                phoneET.setVisibility(View.GONE);
                phoneBtn.setVisibility(View.GONE);
                verifyCodeET.setVisibility(View.VISIBLE);
                verifyCodeBtn.setVisibility(View.VISIBLE);
            }
        };
    }

    /*
        Responsible for triggering password change
        Makes a call to validatePasswordFields to ensure user has entered valid password
        Makes a call to Firebase API, requesting to reauthenticate the user
        On Success, makes another call to Firebase API, requesting to updatePassword
        On Failure, displays message informing the user
     */
    public void changePassword(){
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
                                    finish();
                                    startActivity(getIntent());
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

    /*
        Responsible for validating the passwords entered by the user
        Called from inside the password button onClickListener
        If passwords match requirements, returns true and displays message
        If password do not match requirements, return false and displays message
     */
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

    /*
        Responsible for sending verification email is the user wishes to change their email
        Called from inside the email button onClickListener
        Makes a call to Firebase API, requesting to reauthenticate current user
        On Success, makes another call to Firebase API to updateEmail
        On Success of updateEmail, makes another call to Firebase API to sendVerificationEmail
        On Success of this, signs the user out as they need to now verify their new email
        On Failure of any, displays message informing the user
     */
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

    /*
        Responsible for sending verification sms from Firebase
     */
    public void verifyNumber(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, phoneCallback);
        auth.setLanguageCode("IE");
    }

    /*
        Responsible for checking if the verification code entered by the user is valid
        Makes a call to onVerificationCompleted which saves the user phone number
     */
    public void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        phoneCallback.onVerificationCompleted(credential);
    }
}
