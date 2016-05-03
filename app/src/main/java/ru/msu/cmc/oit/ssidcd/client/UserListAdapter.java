package ru.msu.cmc.oit.ssidcd.client;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.msu.cmc.oit.ssidcd.client.common.FacebookUserID;
import ru.msu.cmc.oit.ssidcd.client.common.UserID;

/**
 * Adapter for user list
 */

public class UserListAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater lInflater;

    private UserID userId;
    private List<UserID> userIDList = Collections.emptyList();
    private List<DisplayData> displayDataList;
    private FBProfileSource FBProfileSource;

    private class DisplayData {
        String name ;
        Bitmap bitmap;
    }




    UserListAdapter(Context context, UserID id) {
        FBProfileSource =  FBProfileSource.getInstance();
        ctx = context;
        userId=id;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return userIDList.size();
    }


    @Override
    public Object getItem(int position) {
        return userIDList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item, parent, false);
        }
        if(position<displayDataList.size()) {
            final TextView textView = (TextView) view.findViewById(R.id.uName);
            textView.setText(displayDataList.get(position).name);
            final ImageView imageView = (ImageView) view.findViewById(R.id.uImage);
            imageView.setImageBitmap(displayDataList.get(position).bitmap);


            final View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserID _userID = userIDList.get(position);
                    if(_userID instanceof FacebookUserID) {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW, FBProfileSource.getUserPageUrl((FacebookUserID) _userID)));
                    }

                }
            };
            textView.setOnClickListener(listener);
        }

        return view;
    }


    UserID getUser(int position) {
        return ((UserID) getItem(position));
    }

    public List<UserID> getUserIDList() {
        return userIDList;
    }

    public void setUserIDList(List<UserID> userIDList) {
        this.userIDList = userIDList;
        displayDataList = new ArrayList<>(userIDList.size());

        for (int i = 0, objectsSize = userIDList.size(); i < objectsSize; i++) {
            //trying to fill array with cached user name and picture
            UserID userID = userIDList.get(i);
            final DisplayData displayData = new DisplayData();
            displayDataList.add(displayData);

            final boolean you = userID.equals(userId);
            if(userID instanceof FacebookUserID) {
                FacebookUserID facebookUserID = (FacebookUserID) userID;

                if (you)
                    displayData.name = ctx.getString(R.string.you);
                else
                    displayData.name = FBProfileSource.getCashedName(facebookUserID);

                displayData.bitmap = FBProfileSource.getCachedPicture(facebookUserID);
                notifyDataSetChanged();

                //start downloading name name and data if needed
                if (displayData.bitmap == null) {
                    FBProfileSource.downloadPicture(facebookUserID, new FBProfileSource.Callback2() {
                        @Override
                        public void onResult(Bitmap bitmap) {
                            displayData.bitmap = bitmap;
                            notifyDataSetChanged();
                        }
                    });
                }
                if (displayData.name == null) {
                    FBProfileSource.downloadName(facebookUserID, new FBProfileSource.Callback1() {
                        @Override
                        public void onResult(String name) {
                            displayData.name = name;
                            notifyDataSetChanged();
                        }
                    });
                }
            }

        }
    }
}

