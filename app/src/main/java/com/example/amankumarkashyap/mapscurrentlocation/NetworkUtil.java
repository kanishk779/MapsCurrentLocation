package com.example.amankumarkashyap.mapscurrentlocation;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkUtil {
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_NOTCONNECTED = 2;

    private static ConnectivityManager cm;
    public static int getNetworkState(Context context)
    {
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null)
        {
            if(NetworkCapabilities.TRANSPORT_CELLULAR == networkInfo.getSubtype())
            {
                Toast.makeText(context, "networkInfo.subtype : " +networkInfo.getSubtype(), Toast.LENGTH_SHORT).show();
                return TYPE_MOBILE;
            }
            else if(networkInfo.getSubtype() == NetworkCapabilities.TRANSPORT_WIFI)
            {
                Toast.makeText(context, "networkInfo.subtype : " +networkInfo.getSubtype(), Toast.LENGTH_SHORT).show();
                return TYPE_WIFI;
            }
        }

        return TYPE_NOTCONNECTED;
    }
}
