package emasterson.finalyearandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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


public class UserProfileActivity extends BaseActivity {
    private EditText phoneET, verifyCodeET, emailET, passwordET, currentPasswordET, newPasswordET, repeatPasswordET;
    private Button phoneBtn, verifyCodeBtn, emailBtn, passwordBtn;
    private UserInfo userInfo;
    private FirebaseUser user;
    private static final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{8,40})";
    private String phoneNumber, verifyCode, codeVerification;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallback;

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

        verifyCodeET.setVisibility(View.GONE);
        verifyCodeBtn.setVisibility(View.GONE);

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

    public void verifyNumber(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, phoneCallback);
        auth.setLanguageCode("IE");
    }

    public void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        phoneCallback.onVerificationCompleted(credential);
    }
}
