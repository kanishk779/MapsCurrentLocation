package com.example.amankumarkashyap.mapscurrentlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = NetworkUtil.getNetworkState(context);
        if(status == NetworkUtil.TYPE_MOBILE)
        {
            Toast.makeText(context, "connected to mobile network", Toast.LENGTH_SHORT).show();
        }
        else if(status == NetworkUtil.TYPE_WIFI)
        {
            Toast.makeText(context, "connected to wifi ", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context,"Not connected",Toast.LENGTH_SHORT).show();
        }
    }
}
