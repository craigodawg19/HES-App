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
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import static android.Manifest.permission.READ_CONTACTS;

    /**
     * A login screen that offers login via email/password.
     */
    public class SignUp extends AppCompatActivity {

        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private static final int REQUEST_READ_CONTACTS = 0;

        // Firebase instance variables
        private FirebaseAuth mFirebaseAuth;
        private FirebaseUser mFirebaseUser;
        private FirebaseAuth.AuthStateListener mAuthListener;

        // UI references.
        private EditText mFirstNameView;
        private EditText mLastNameView;
        private EditText mMajorView;
        private EditText mBirthdayView;
        private EditText mEmailView;
        private EditText mPasswordView;
        private EditText mConfirmView;

        private View mProgressView;
        private View mSignUpFormView;
        private ProgressBar mProgressBar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.sign_up);


            mFirstNameView = (EditText) findViewById(R.id.first_name);
            mLastNameView = (EditText) findViewById(R.id.last_name);
            mMajorView = (EditText)findViewById(R.id.major_view);
            mBirthdayView = (EditText) findViewById(R.id.birthday);
            mEmailView = (EditText) findViewById(R.id.email);
            mPasswordView= (EditText) findViewById(R.id.password);
            mConfirmView = (EditText) findViewById(R.id.confirm_password);

            mFirebaseAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Log.d("PLS", "onAuthStateChanged:signed_in:" + user.getUid());
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);

                    } else {
                        // User is signed out
                        Log.d("PLS", "onAuthStateChanged:signed_out");
                    }
                    // ...
                }
            };
            Button mEmailSignInButton = (Button) findViewById(R.id.sign_up_button);

            mSignUpFormView = findViewById(R.id.sign_up_form);
        }

        public void signUp(View view){
            final String email = mEmailView.getText().toString();
            final String pass = mPasswordView.getText().toString();
            final String first = mFirstNameView.getText().toString();
            final String last = mLastNameView.getText().toString();
            final String major = mMajorView.getText().toString();
            final String confirm = mConfirmView.getText().toString();
            final String birthday = mBirthdayView.getText().toString();
            if(email.isEmpty() || pass.isEmpty() || first.isEmpty() || last.isEmpty() || major.isEmpty()||confirm.isEmpty() ){
                Toast.makeText(com.example.craig_000.hesapp.SignUp.this, "One or more text boxes is empty",Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog signUpDialog = ProgressDialog.show(com.example.craig_000.hesapp.SignUp.this,"", "Signing up...",true, false);
            mFirebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(com.example.craig_000.hesapp.SignUp.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Log.w("SIGNING UP","IT MESSED UP BOYS", task.getException());
                        signUpDialog.dismiss();
                    }else{
                        //Toast.makeText(LoginActivity.this, "SIGN UP WORKED", Toast.LENGTH_SHORT).show();

                        //Add information to firebase for the user

                        Log.d("ADDIN' TAH FIREBASE", "ADDING STUFF TO FIREBASE");


                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference userRef = database.getReference("users");
                        DatabaseReference newChildRef = userRef.child(mFirebaseAuth.getCurrentUser().getUid());

                        HashMap<String, String> newUserData = new HashMap<String, String>();
                        newUserData.put("birthday", birthday);
                        newUserData.put("name", first + " " + last);
                        newUserData.put("email", email);
                        newUserData.put("major", major);

                        newChildRef.setValue(newUserData);

                        Intent intent = new Intent(com.example.craig_000.hesapp.SignUp.this, MainActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        signUpDialog.dismiss();
                    }
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






