package com.htetznaing.boycottchina.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.htetznaing.boycottchina.AppUpdater.AppUpdater;
import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.items.ColorItem;
import com.htetznaing.boycottchina.MyApplication;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.networking.InternetUtils;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    protected List<ColorItem> themes = new ArrayList<>();
    protected AppUpdater appUpdater;

    @Override
    protected void onResume() {
        super.onResume();
        if (InternetUtils.isOnline())
            appUpdater.check(false);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addThemes();
        int theme = MyApplication.sharedPreferences.getInt(Constants.APP_THEME_KEY,0);
        if (theme!=0)
            getTheme().applyStyle(theme,true);
        Constants.changeTheme(MyApplication.sharedPreferences.getInt(Constants.DARK_THEME_KEY,0));
        appUpdater = new AppUpdater(this,getString(R.string.check_update_url));
    }

    protected void addThemes() {
        put2Theme(ContextCompat.getColor(this, R.color.main),R.style.Theme_ငါလိုးမတရုတ်);
        put2Theme(ContextCompat.getColor(this,R.color.old_theme),R.style.OLD_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.pink_theme),R.style.PINK_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.purple_theme),R.style.PURPLE_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.deep_purple_theme),R.style.DEEP_PURPLE_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.indigo_theme),R.style.INDIGO_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.blue_theme),R.style.BLUE_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.light_blue_theme),R.style.LIGHT_BLUE_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.cyan_theme),R.style.CYAN_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.teal_theme),R.style.TEAL_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.green_theme),R.style.GREEN_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.lime_theme),R.style.LIME_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.yellow_theme),R.style.YELLOW_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.amber_theme),R.style.AMBER_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.orange_theme),R.style.ORANGE_THEME);
        put2Theme(ContextCompat.getColor(this,R.color.deep_orange_theme),R.style.DEEP_ORANGE_THEME);
    }

    private void put2Theme(Integer preview,Integer theme){
        ColorItem item = new ColorItem();
        item.setPreview(preview);
        item.setTheme(theme);
        themes.add(item);
    }
}
