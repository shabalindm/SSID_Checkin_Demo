package ru.msu.cmc.oit.ssidcd.client;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helps to generate hashcode for Facebook
 * to generate hashcode add android:name=".FacebookHashGenerator" to Application section of Manifest.xml
 * run App and search for tag  "KeyHash:" in logcat
 */
public class FacebookHashGenerator extends Application {

    public void onCreate() {
        super.onCreate();

        try {
            String packageName = getPackageName();
            PackageInfo info = getPackageManager().getPackageInfo(
                     packageName,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.i("KeyHash:", hash);
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
