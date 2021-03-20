package com.htetznaing.boycottchina;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Base64;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.esafirm.imagepicker.model.Image;
import com.htetznaing.boycottchina.items.AppItem;
import com.htetznaing.boycottchina.utils.CustomTypefaceSpan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Constants {
    public static Image selectedImage = null;
    public static final HashMap<String,String> chinaAppList = new HashMap<>();
    public static final HashMap<String,AppItem> foundedList = new HashMap<>();
    public static final String DARK_THEME_KEY = "dark_theme",APP_THEME_KEY = "cur_app_theme";
    private static boolean autoCheckUpdate = true;
    private static final String autoCheckUpdateKey = "auto_check_update";
    public static void changeTheme(int which){
        if (which==1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (which==2)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static String getSHA1(Context context,String packageName) {
        PackageManager pm=context.getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signatures=info.signatures;
            MessageDigest md=MessageDigest.getInstance("SHA");
            Signature signature=signatures[0];
            md.update(signature.toByteArray());
            return Base64.encodeToString(md.digest(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String calculateFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String readRaw(Context context, int id) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static Intent uninstall(String packageName){
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+packageName));
        intent.setFlags(0);
        return intent;
    }

    public static void uninstall(Activity activity, String packageName){
        uninstall(activity,packageName,Integer.MIN_VALUE);
    }

    public static void uninstall(Activity activity, String packageName,int code){
        Intent intent = uninstall(packageName);
        if (code==Integer.MIN_VALUE)
            activity.startActivity(intent);
        else activity.startActivityForResult(intent,code);
    }

    public static boolean appInstalled(Context context, String uri) {
        if (uri!=null) {
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                System.out.println(uri+" not installed");
            }
        }
        return false;
    }

    public static int getAttr(Context context,int input){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(input, typedValue, true);
        return typedValue.data;
    }

    public static boolean isChinaProducts(String barcodeNumber){
        // China products barcode prefix = 690-699
        // Ref => https://en.wikipedia.org/wiki/List_of_GS1_country_codes
        String regex = "^(69[0-9]\\d).*";
        return barcodeNumber.matches(regex);
    }

    public static void vibrateDevice(Context context){
        vibrateDevice(context,100);
    }

    public static void vibrateDevice(Context context,int milliseconds){
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(milliseconds);
        }
    }

    //    Direct open play Store
    public static void openInPlayStore(Context context,String packageName){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            startOpenInStore(context,packageName);
        }
    }

    //    Direct open AppGallery
    public static void openInAppGallery(Context context,String packageName){
        String playStoreScheme = "market://details?id=", huaweiScheme = "appmarket://details?id=",appGalleryPackageName = "com.huawei.appmarket";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(appGalleryPackageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse(playStoreScheme+packageName));
        if (intent.resolveActivityInfo(context.getPackageManager(),0)==null){
            Uri.parse(huaweiScheme+packageName);
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            startOpenInStore(context,packageName);
        }
    }

    //    Open to any App Store
    public static void startOpenInStore(Context context,String pk) {
        String playStoreScheme = "market://details?id=", huaweiScheme = "appmarket://details?id=";
        if (!openInStore(context,playStoreScheme+pk)) {
            if (!openInStore(context,huaweiScheme + pk)) {
                openInStore(context,"https://play.google.com/store/apps/details?id=" + pk);
            }
        }
    }

    private static boolean openInStore(Context context,String uri){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException anfe) {
            return false;
        }
    }

    public static SpannableStringBuilder mmString(Context context, String input){
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(input);
        stringBuilder.setSpan(new CustomTypefaceSpan("", ResourcesCompat.getFont(context, R.font.mm)), 0, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return stringBuilder;
    }

    public static String decodeBase64(String input){
        return new String(Base64.decode(input,Base64.DEFAULT));
    }

    public static boolean getAutoCheckUpdate(){
        autoCheckUpdate = MyApplication.sharedPreferences.getBoolean(autoCheckUpdateKey,false);
        return autoCheckUpdate;
    }

    public static void setAutoCheckUpdate(boolean auto){
        autoCheckUpdate = auto;
        MyApplication.sharedPreferences.edit().putBoolean(autoCheckUpdateKey,auto).apply();
    }
}
