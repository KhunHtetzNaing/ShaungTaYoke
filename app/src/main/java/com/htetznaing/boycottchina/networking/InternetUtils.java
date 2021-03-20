package com.htetznaing.boycottchina.networking;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.htetznaing.boycottchina.MyApplication;

public class InternetUtils {
    private static boolean isInternetOn() {
        ConnectivityManager cm = MyApplication.connectivityManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                }  else return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }
        }else {
            NetworkInfo netInfo = null;
            if (cm != null) {
                netInfo = cm.getActiveNetworkInfo();
            }
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return false;
    }

    public static boolean isOnline() {
        return isInternetOn();
    }
}
