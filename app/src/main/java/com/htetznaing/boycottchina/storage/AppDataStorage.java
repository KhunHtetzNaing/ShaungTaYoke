package com.htetznaing.boycottchina.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.htetznaing.boycottchina.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Map;

public abstract class AppDataStorage implements SharedPreferences {
    private static final String DB="china_apps";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context){
        sharedPreferences = context.getSharedPreferences(DB,Context.MODE_PRIVATE);
    }

    public static void setAppList(Document document) {
        String TAG = "app_list";
        Elements elements = document.getElementsByTag(TAG);
        if (!elements.isEmpty()){
            String text = elements.first().text().trim();
            if (!text.isEmpty() && !text.startsWith("[")){
                try {
                    text = Constants.decodeBase64(text);
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
            }
            put(text);
        }
    }

    public static void put(String json){
        if (json==null || json.isEmpty())
            return;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("name"), packageName = object.getString("package");
                sharedPreferences.edit().putString(packageName,name).apply();
            }
            Constants.chinaAppList.clear();
            Constants.chinaAppList.putAll((Map<? extends String, ? extends String>) get());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,?> get(){
        return sharedPreferences.getAll();
    }
}
