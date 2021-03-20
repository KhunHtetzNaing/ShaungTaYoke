package com.htetznaing.boycottchina.AppUpdater;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresPermission;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.dialogs.MyMaterialDialog;
import com.htetznaing.boycottchina.dialogs.MyProgressDialog;
import com.htetznaing.boycottchina.networking.GetJSON;
import com.htetznaing.boycottchina.storage.AppDataStorage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class AppUpdater {
    private final Activity activity;
    private boolean uninstall = false,force=true;
    private final String json_url;
    private MyMaterialDialog dialog;
    private boolean showMessage;
    private final MyProgressDialog progressDialog;
    @RequiresPermission(Manifest.permission.INTERNET)
    public AppUpdater(Activity activity, String json_url) {
        this.json_url = json_url;
        this.activity = activity;

        progressDialog = new MyProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(activity.getString(R.string.checking));
    }

    public void check(boolean showMessage){
        this.showMessage=showMessage;
        checkUpdate();
    }

    private void checkUpdate() {
        if (showMessage){
            progressDialog.show();
        }
        new GetJSON(activity)
                .listen(new GetJSON.OnDone() {
                    @Override
                    public void onTaskCompleted(String result) {
                        String s;
                        Document document = Jsoup.parse(result);

                        //AutoUpdate
                        Elements auto_update = document.getElementsByTag("auto_update");
                        if (!auto_update.isEmpty()){
                            Constants.setAutoCheckUpdate(Boolean.parseBoolean(auto_update.first().text()));
                        }

                        AppDataStorage.setAppList(document);

                        if (!showMessage && !Constants.getAutoCheckUpdate()) {
                            return;
                        }

                        if (json_url.contains("blogspot.com") || document.hasClass("post-body entry-content")) {
                            //Blogspot Link
                            try{
                                s = document.getElementsByClass("post-body entry-content").get(0).text();
                            }catch (Exception e) {
                                s = document.getElementsByTag("body").get(0).text();
                            }
                        }else {
                            s = document.text();
                        }
                        if (showMessage){
                            progressDialog.dismiss();
                        }
                        if (s!=null){
                            int start = s.indexOf("{");
                            int end = s.lastIndexOf("}")+1;
                            s = s.substring(start,end);
                            letCheck(s);
                        }
                    }

                    @Override
                    public void onTaskFailure(String error) {

                    }
                })
                .request(json_url);
    }

    private String query(String key,JSONObject object){
        if (object.has(key)){
            try {
                return object.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void letCheck(String response){
        if (dialog!=null)dialog.dismiss();
        UpdateModel data = new UpdateModel();
        if (response!=null) {
            try {
                JSONObject object = new JSONObject(response);
                data.setVersionName(query("versionName",object));
                data.setTitle(query("title",object));
                data.setMessage(query("message",object));
                data.setDownload(query("download",object));
                data.setPlaystore(query("playstore",object));
                data.setUninstall(object.getBoolean("uninstall"));
                data.setVersionCode(object.getInt("versionCode"));

                data.setForce(object.getBoolean("force"));
                if (object.has("app_gallery"))
                    data.setApp_gallery(object.getBoolean("app_gallery"));

                if (object.has("what")) {
                    JSONObject what = object.getJSONObject("what");
                    data.setAll(what.getBoolean("all"));
                    JSONArray versions = what.getJSONArray("version");

                    int[] v_int = new int[versions.length()];
                    for (int i=0;i<versions.length();i++){
                        v_int[i] = versions.getInt(i);
                    }
                    data.setVersions(v_int);

                    JSONArray models = what.getJSONArray("model");
                    String[] models_string = new String[models.length()];
                    for (int i=0;i<models.length();i++){
                        models_string[i] = models.getString(i);
                    }
                    data.setModels(models_string);
                }

                if (object.has("hash")){
                    data.setHash(object.getString("hash"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        uninstall = data.isUninstall();
        int versionCode = data.getVersionCode();

        force = data.isForce();

        PackageManager manager = activity.getPackageManager();
        PackageInfo info;
        int currentVersion = 0;
        try {
            info = manager.getPackageInfo(activity.getPackageName(), 0);
            currentVersion = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (data.getHash()!=null && !data.getHash().isEmpty()){
            if (!Constants.getSHA1(activity,activity.getPackageName()).trim().equals(data.getHash())){
                letUpdate(data);
                return;
            }
        }

        if (versionCode ==currentVersion || versionCode < currentVersion){
            if (showMessage) {
                dialog = new MyMaterialDialog(activity);
                dialog.setMessage(activity.getString(R.string.latest_version))
                        .setStyle(Style.HEADER_WITH_ICON)
                        .setIcon(R.drawable.ic_emoji)
                        .withDialogAnimation(true)
                        .setPositiveButton(R.string.yes, new MyMaterialDialog.OnClickedListener() {
                            @Override
                            public void clicked(@NotNull MaterialStyledDialog dialog) {

                            }
                        });
                if (!activity.isFinishing()) dialog.show();
            }
        }else{
            if (data.isAll()){
                letUpdate(data);
            }else {
                String my_model = Build.MANUFACTURER.toLowerCase();
                int my_version = Build.VERSION.SDK_INT;
                boolean match_model = false,match_version=false;

                for (String string:data.getModels()){
                    if (my_model.equalsIgnoreCase(string)){
                        match_model = true;
                        break;
                    }
                }


                for (int i:data.getVersions()){
                    if (my_version==i){
                        match_version = true;
                        break;
                    }
                }

                if (match_model && match_version){
                    letUpdate(data);
                }
            }
        }
    }

    private void letUpdate(UpdateModel model){
        activity.runOnUiThread(() -> {
            dialog = new MyMaterialDialog(activity);
            dialog.setTitle(model.getTitle())
                    .setMessage(model.getMessage())
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setCancelable(!force)
                    .setIcon(R.drawable.ic_update)
                    .withDialogAnimation(true);
            if (model.getPlaystore()!=null && !model.getPlaystore().isEmpty()){
                dialog.setPositiveButton(R.string.play_store, dialog -> {
                    if (uninstall){
                        Constants.uninstall(activity,activity.getPackageName());
                    }
                    Constants.openInPlayStore(activity,model.getPlaystore());
                });
            }

            if (model.getDownload()!=null && !model.getDownload().isEmpty()){
                dialog.setNeutralButton(activity.getString(R.string.other), dialog -> {
                    if (uninstall){
                        Constants.uninstall(activity,activity.getPackageName());
                    }
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getDownload())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Toast.makeText(activity, Constants.mmString(activity,activity.getString(R.string.unknown_error)), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (model.isApp_gallery()){
                dialog.setNegativeButton(activity.getString(R.string.app_gallery), dialog -> {
                    if (uninstall){
                        Constants.uninstall(activity,activity.getPackageName());
                    }
                    try {
                        Constants.openInAppGallery(activity,model.getPlaystore());
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Toast.makeText(activity, Constants.mmString(activity,activity.getString(R.string.unknown_error)), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (!activity.isFinishing())dialog.show();
        });
    }
}