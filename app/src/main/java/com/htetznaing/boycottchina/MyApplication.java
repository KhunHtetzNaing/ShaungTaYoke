package com.htetznaing.boycottchina;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.htetznaing.boycottchina.storage.AppDataStorage;
import com.onesignal.OneSignal;

import java.util.Map;
import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;

public class MyApplication extends Application {
    private static MyApplication context;
    public static SharedPreferences sharedPreferences;
    public static Markwon markwon;

    public static Context getAppContext() {
        return context;
    }

    public static ConnectivityManager connectivityManager;
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID,MODE_PRIVATE);
        context = this;
        markwon = Markwon.create(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId("a7f0524d-4eb5-4bb1-8e6f-446d1125e794");

        AppDataStorage.init(this);
        load();
    }

    private void load(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (AppDataStorage.get().isEmpty()) {
                    String json = Constants.readRaw(context, R.raw.app);
                    AppDataStorage.put(json);
                }else Constants.chinaAppList.putAll((Map<? extends String, ? extends String>) AppDataStorage.get());
            }
        });
    }
}
