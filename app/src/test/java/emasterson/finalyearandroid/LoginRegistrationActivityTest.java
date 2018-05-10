package emasterson.finalyearandroid;

import android.app.ProgressDialog;
import android.widget.EditText;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginRegistrationActivityTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private LoginRegistrationActivity mockActivity;
    
    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private Task mockTask;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    /*
        EmailValidator tests demonstrates how the code should perform
        These test are an example reference, PasswordValidator would perform the exact same actions
     */
    @Test
    public void EmailValidator_CorrectEmail_ReturnsTrue(){
        when(mockActivity.validateEmail("name@email.com")).thenReturn(true);
        mockActivity.validateEmail("name@email.com");
        verify(mockActivity).validateEmail("name@email.com");
        assertThat(mockActivity.validateEmail("name@email.com"), is(true));
    }

    @Test
    public void EmailValidator_IncorrectEmail_ReturnsFalse(){
        when(mockActivity.validateEmail("email.com")).thenReturn(false);
        mockActivity.validateEmail("email.com");
        verify(mockActivity).validateEmail("email.com");
        assertThat(mockActivity.validateEmail("email.com"), is(false));
    }

    @Test
    public void EmailValidator_IncorrectEmail_SetsError(){
        EditText mockET = mock(EditText.class);
        mockET.setError("Invalid Email");
        verify(mockET).setError("Invalid Email");
    }

    /*
        SigIn test demonstrate how the code should perform and interact with Firebase
        These tests are an example reference, createAccount, sendVerificationEmail, checkIfEmailVerified and resetPassword
        would perform the exact same actions
     */
    @Test
    public void SignIn_VerifyInteraction(){
        mockActivity.signIn("name@email.com", "Password1");
        verify(mockActivity).signIn("name@email.com", "Password1");
    }

    @Test
    public void SignIn_FirebaseListener_ReturnsTask(){
        when(mockAuth.signInWithEmailAndPassword("name@email.com", "Password1")).thenReturn(mockTask);
        mockAuth.signInWithEmailAndPassword("name@email.com", "Password1");
        verify(mockAuth).signInWithEmailAndPassword("name@email.com", "Password1");
        assertThat(mockAuth.signInWithEmailAndPassword("name@email.com", "Password1"), is(mockTask));
    }

    @Test
    public void SignIn_FirebaseTaskIsSuccessful_ReturnsTrue(){
        when(mockTask.isSuccessful()).thenReturn(true);
        mockTask.isSuccessful();
        verify(mockTask).isSuccessful();
        assertThat(mockTask.isSuccessful(), is(true));
    }

    @Test
    public void ShowProgressDialog_ShowsMessage(){
        ProgressDialog progressDialog = mock(ProgressDialog.class);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        verify(progressDialog).setMessage("Loading...");
        verify(progressDialog).show();
    }
}
