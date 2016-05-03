package ru.msu.cmc.oit.ssidcd.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ru.msu.cmc.oit.ssidcd.client.common.FacebookUserID;
import ru.msu.cmc.oit.ssidcd.client.common.UserID;


/**
 * Downloads and cashes users names and pictures from Facebook
 */

public class FBProfileSource {
    private static final String LOG_TAG = FBProfileSource.class.getName();
    private static FBProfileSource instance = new FBProfileSource();

    public static FBProfileSource getInstance() {
        return instance;
    }

    private Map<FacebookUserID, String> names = new HashMap<>();
    private Map<FacebookUserID, Bitmap> pictures = new HashMap<>();

    public void downloadName(final FacebookUserID userId, final Callback1 callBack){
        final String cached = names.get(userId);
        if (cached != null) {
            callBack.onResult(cached);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("fields", "name");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + userId.getId(),
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null)
                            try {
                                JSONObject jsonObject = response.getJSONObject();
                                String name = jsonObject.getString("name");
                                names.put(userId, name);
                                callBack.onResult(name);
                            } catch (JSONException e) {
                                callBack.onResult(null);
                            }
                        else
                            callBack.onResult(null);

                    }
                }
        ).executeAsync();

    }


    public void downloadPicture(final FacebookUserID userId, final Callback2 callBack){
        final Bitmap cached = pictures.get(userId);
        if(cached!=null) {
            callBack.onResult(cached);
            return;
        }

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return getFacebookProfilePicture(userId.getId());
            }


            @Override
            protected void onPostExecute(Bitmap image) {
                pictures.put(userId, image);
                callBack.onResult(image);
            }
        }.execute();


    }


    public static Bitmap getFacebookProfilePicture(String userID){
        try {
            URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=square");
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            return bitmap;
        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
        }
        return null;


    }

   public Bitmap getCachedPicture(FacebookUserID facebookUserID){
       return pictures.get(facebookUserID);
   }
    public String getCashedName(FacebookUserID facebookUserID){
        return names.get(facebookUserID);
    }

    public Uri getUserPageUrl(FacebookUserID userID) {
        return Uri.parse("https://www.facebook.com/" + userID.getId());
    }



    public static interface Callback1 {
        public void onResult(String name);
    }
    public static interface Callback2 {
        public void onResult(Bitmap image);
    }

}
