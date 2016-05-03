package ru.msu.cmc.oit.ssidcd.client;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.msu.cmc.oit.ssidcd.client.common.FacebookUserID;
import ru.msu.cmc.oit.ssidcd.client.common.UserID;

public class MainActivity extends AppCompatActivity {
    public static volatile long lastWifiScanTime = 0;
    private com.facebook.Profile fbProfile;
    private UserID userID;
    Button checkInBtn;
    Button refreshBtn;
    List<UserID> usersNear;
    UserListAdapter adapter;
    boolean progress;
    private List<TimeDialogOptionObject> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Время последнего сканирования точек доступа будет записываться в статическое поле lastWifiScanTime
        registerWifiScanResultReceiver();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkInBtn = (Button) findViewById(R.id.checkin_btn);
        refreshBtn = (Button) findViewById(R.id.refresh_btn);

        Bundle extras = getIntent().getExtras();
        if(extras==null)
            goToLogin();
        fbProfile = getIntent().getParcelableExtra("fbProfile");
        userID = new FacebookUserID(fbProfile.getId());


        ListView peopleList = (ListView) findViewById(R.id.people_near);
        adapter = new UserListAdapter(this, userID);
        peopleList.setAdapter(adapter);

        checkInBtn = (Button) findViewById(R.id.checkin_btn);

        list = createTimeDialogOptionList();

        final String [] listName = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            list.get(i);
            listName[i]= list.get(i).getLabel();
        }

        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.select_time_dialog_title)
                        .setItems(listName, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TimeDialogOptionObject t = list.get(which);
                                checkin(t.getTime());
                            }
                        });

                builder.show();
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsersNear();
            }
        });

        getUsersNear();

    }

    private void registerWifiScanResultReceiver() {
        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
            @Override
          public void onReceive(Context c, Intent intent) {
              // This condition is not necessary if you listen to only one action
              if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                 lastWifiScanTime=System.currentTimeMillis();
              }
          }
      };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiScanReceiver, intentFilter);
    }

    private List<TimeDialogOptionObject> createTimeDialogOptionList() {
       List<TimeDialogOptionObject> list = new ArrayList<>();
//        list.add(new TimeDialogOptionObject(120,  "2 min"));
//        list.add(new TimeDialogOptionObject(300,  "5 min"));
        list.add(new TimeDialogOptionObject(60*10,  getString(R.string.time_10_min)));
        list.add(new TimeDialogOptionObject(60*20,  getString(R.string.time_20_min)));
        list.add(new TimeDialogOptionObject(60*30,  getString(R.string.time_30_min)));
        list.add(new TimeDialogOptionObject(60*40,  getString(R.string.time_40_min)));
        list.add(new TimeDialogOptionObject(3600,   getString(R.string.time_1_h)));
        list.add(new TimeDialogOptionObject(3600*3/2,   getString(R.string.time_1_5_h)));
        list.add(new TimeDialogOptionObject(3600*2,  getString(R.string.time_2_h)));
        list.add(new TimeDialogOptionObject(3600*3,   getString(R.string.time_3_h)));
        list.add(new TimeDialogOptionObject(3600*4,  getString(R.string.time_4_h)));
        list.add(new TimeDialogOptionObject(3600*5,   getString(R.string.time_5_h)));
        list.add(new TimeDialogOptionObject(3600*6,   getString(R.string.time_6_h)));
        list.add(new TimeDialogOptionObject(3600*7,   getString(R.string.time_7_h)));
        list.add(new TimeDialogOptionObject(3600*8,  getString(R.string.time_8_h)));
        list.add(new TimeDialogOptionObject(3600*10,  getString(R.string.time_10_h)));
        return list;
    }


    private void getUsersNear() {
      checkin(-1);
    }

    private void checkin(int time) {
        progress = true;
        update();//disables buttons

        new CheckinTask(time, userID,this){
            @Override
            protected void onPostExecute(CheckinTask result) {
                adapter.setUserIDList(Collections.<UserID>emptyList());
                adapter.notifyDataSetChanged();
                if(isError()){
                    Toast.makeText(MainActivity.this, this.getError(), Toast.LENGTH_SHORT).show();
                    usersNear = Collections.EMPTY_LIST;
                }
                else{
                    usersNear = this.getResult();
                }

                adapter.setUserIDList(usersNear);
                progress = false;
                update();//enables buttons
            }

        }.execute();
    }

    /**
     * refresh UI
     */
    private void update() {
        checkInBtn.setEnabled(!progress);
        refreshBtn.setEnabled(!progress);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            goToLogin();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void addTestUsers() {
        for (String uid : "100000277862602,100000504936751,100002071872568".split(",")) {
            new CheckinTask(120, new FacebookUserID(uid), this){
            }.execute();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

}
