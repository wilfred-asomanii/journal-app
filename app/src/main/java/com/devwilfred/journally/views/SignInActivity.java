package com.devwilfred.journally.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.devwilfred.journally.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 *  activity for user authentication.
 *  launched only when no user is logged in
 */
public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SignInActivity";
    private static final int REQ_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.sign_in_btn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, REQ_SIGN_IN);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_SIGN_IN) {
            // process selected account
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {

                GoogleSignInAccount account = result.getSignInAccount();

                if (account != null) {
                    authenticate(account);
                }
            } else {
                // sign in failed
                Log.e(TAG, getString(R.string.sign_in_fail));
            }
        }
    }

    /**
     *  authenticate a user ob firebase with google account
     * @param acct the google account of the user
     */
    private void authenticate(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.auth_fail,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // put user details in shared preference, so other activities can access
                            SharedPreferences.Editor editor = mPreferences.edit();
                            editor.putString(getString(R.string.username_preference), acct.getDisplayName());
                            editor.putString(getString(R.string.user_uid_preference), mFirebaseAuth.getUid());
                            editor.apply();

                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        // unable to connect to google services api
        Toast.makeText(this, R.string.google_service_err, Toast.LENGTH_SHORT).show();
    }
}
