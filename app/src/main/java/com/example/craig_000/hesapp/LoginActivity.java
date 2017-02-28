package com.example.craig_000.hesapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("PLS", "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                } else {
                    // User is signed out
                    Log.d("PLS", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        mLoginFormView = findViewById(R.id.login_form);
    }

    public void signIn(View view){
        final String email = mEmailView.getText().toString();
        final String pass = mPasswordView.getText().toString();
        if(email.isEmpty() || pass.isEmpty() ){
            Toast.makeText(LoginActivity.this, "One or more text boxes is empty",Toast.LENGTH_SHORT).show();
            return;
        }
        //Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();
        final ProgressDialog signInDialog = ProgressDialog.show(this,"", "Signing in...",true, false);
        mFirebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("LOGIN", "signInWithEmail:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener
                if (!task.isSuccessful()) {
                    signInDialog.dismiss();
                    Log.w("LOGIN", "signInWithEmail:failed", task.getException());
                    Task<ProviderQueryResult> result = mFirebaseAuth.fetchProvidersForEmail(email);

                    result.addOnCompleteListener(LoginActivity.this, new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if(task.getResult().getProviders().isEmpty()){
                                //No account with this email is created, please throw toast
                                final ProgressDialog signUpDialog = ProgressDialog.show(LoginActivity.this,"", "Signing up...",true, false);
                                //lines 138-153 to go to sign up activity. No longer needed here
                                mFirebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(!task.isSuccessful()){
                                            Log.w("SIGNING UP","IT MESSED UP BOYS", task.getException());
                                            signUpDialog.dismiss();
                                        }else{
                                            //Toast.makeText(LoginActivity.this, "SIGN UP WORKED", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                            startActivity(intent);
                                            signUpDialog.dismiss();
                                        }
                                    }
                                });
                            } else{
                                //Wrong Password
                                Toast.makeText(LoginActivity.this, "YOU ENTERED THE WRONG PASSWORD HOMIE", Toast.LENGTH_SHORT).show();
                            }
                            Log.w("TASK COMPLETE", task.getResult().getProviders().toString());

                        }
                    });

                    Log.w("FETCH PROVIDERS","Task should be complete by now");
//                    Toast.makeText(LoginActivity.this, "AUTH FAILED",
//                            Toast.LENGTH_SHORT).show();
                } else{
                    //Toast.makeText(LoginActivity.this, "AUTH WORKED", Toast.LENGTH_SHORT).show();
                    signInDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }

                // ...
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }
}




