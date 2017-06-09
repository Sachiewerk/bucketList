package ua.aengussong.www.bucketlist.utilities;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by coolsmileman on 08.06.2017.
 */

public class Utils {
    public static boolean isInternetWorking(Context context){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if(activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED){
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;

        return connected;
    }
}
