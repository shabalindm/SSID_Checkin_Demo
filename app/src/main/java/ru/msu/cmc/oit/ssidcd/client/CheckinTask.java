package ru.msu.cmc.oit.ssidcd.client;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import ru.msu.cmc.oit.ssidcd.client.common.UserID;


/**
 * Asynchronously scans wireless spots, sends result to server and obtains list of userIDs near
 */
public class CheckinTask extends AsyncTask<Void,Void,CheckinTask> {

    private final int checkingTTL;
    private final UserID userId;

    private final Context ctx;
    volatile String error;
    volatile List<UserID> result;

    /**
     *
     * @param checkingTTL time for checkin to live. If -1, no checkin created.
     * @param userId - current user identifier.
     * @param ctx
     */
    public CheckinTask(int checkingTTL, UserID userId, Context ctx) {
        super();
        this.checkingTTL = checkingTTL;
        this.userId = userId;
        this.ctx = ctx;
    }



    @Override
    protected CheckinTask doInBackground(Void... params) {
        try {
            List<ScanResult> scanResults = scanWiFi();

            String bssidList = serialize(scanResults);

            requestToServer(bssidList);

        }catch (CheckinException e){
            error=e.getMessage();
        }

        return this;
    }

    private void requestToServer(String bssidList) throws CheckinException {
        try {
            URL url = new URL(ctx.getString(R.string.server_dns));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("userID", userId.serialize())
                    .appendQueryParameter("ssidList", bssidList)
                    .appendQueryParameter("ttl", String.valueOf(checkingTTL));
            String query = builder.build().getEncodedQuery();


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.flush();
            writer.write(query);
            writer.close();
            os.close();


            conn.connect();

            StringBuilder buffer = new StringBuilder();

            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            reader.close();
            inputStream.close();


            String[] split = buffer.toString().split(",");
            result = new ArrayList<>(split.length);

            for (String userID : split) {
                if (userID.length() > 0)
                    result.add(UserID.deserialize(userID));
            }


        } catch (Exception e) {
            Log.e(CheckinTask.class.getName(), "", e);
            throw new CheckinException(ctx.getString(R.string.error_on_connection_to_server));

        }
    }


    @NonNull
    private List<ScanResult> scanWiFi() throws CheckinException {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        long lastWifiScan = MainActivity.lastWifiScanTime;

        if (System.currentTimeMillis() - lastWifiScan > 10000) {//Требуется новое сканирование
            wifiManager.startScan();
            long timeOut = System.currentTimeMillis() + 60000;

            while (MainActivity.lastWifiScanTime == lastWifiScan && System.currentTimeMillis() < timeOut) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (MainActivity.lastWifiScanTime == lastWifiScan) {
                throw new CheckinException(ctx.getString(R.string.Error_on_scanning_WIFIs));
            }
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();

        if (scanResults.isEmpty()) {
            throw new CheckinException(ctx.getString(R.string.No_WIFI_spot_visible));
        }
        return scanResults;
    }


    @NonNull
    private String serialize(List<ScanResult> scanResults) {
        List<String> bssids = new ArrayList<>(scanResults.size());
        for (ScanResult scanResult : scanResults) {
            bssids.add(scanResult.BSSID);
        }
        return getCommaSeparatedList(bssids);
    }

    private String getCommaSeparatedList(List<String> ssids) {
        StringBuilder sb = new StringBuilder();
        for (String ssid : ssids) {
            sb.append(ssid);
            sb.append(",");
        }
        return sb.toString();
    }

    public String getError() {
        return error;
    }

    public List<UserID> getResult() {
        return result;
    }
    public boolean isError(){
        return error!=null;
    }

    private static class CheckinException extends Exception{

        public CheckinException(String detailMessage) {
            super(detailMessage);
        }
    }
}
