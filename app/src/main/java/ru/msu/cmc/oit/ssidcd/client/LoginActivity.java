package ru.msu.cmc.oit.ssidcd.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginActivity extends AppCompatActivity {

    private LoginButton loginButton;
    CallbackManager manager;
    private ProfileTracker profileTracker;
    AccessTokenTracker accessTokenTracker;

    FacebookCallback<LoginResult> callback =  new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            goToMainActivity(Profile.getCurrentProfile());

        }

        @Override
        public void onCancel() {
            Log.e("", "onCancel");

        }

        @Override
        public void onError(FacebookException error) {
            Log.e("","onError");
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        goToMainActivity(Profile.getCurrentProfile());
    }

    @Override
    public void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        accessTokenTracker.stopTracking();
    }

    private void goToMainActivity(Profile profile) {
        if(profile!=null) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("fbProfile", profile);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        manager = CallbackManager.Factory.create();
        Profile currentProfile = Profile.getCurrentProfile();
        if(currentProfile!=null){
            goToMainActivity(currentProfile);
            return;
        }

        profileTracker = new ProfileTracker() {

            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                goToMainActivity(currentProfile);
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        profileTracker.startTracking();
        accessTokenTracker.startTracking();

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends", "public_profile");
        loginButton.registerCallback(manager, callback);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        manager.onActivityResult(requestCode, resultCode, data);
    }
}
