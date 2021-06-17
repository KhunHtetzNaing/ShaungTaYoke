package com.htetznaing.boycottchina.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.MyApplication;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.adapters.ColorRecyclerAdapter;
import com.htetznaing.boycottchina.dialogs.MyMaterialDialog;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity {
    private final int REQ_CAMERA = 1;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setBackground(null);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_products, R.id.navigation_apps)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission())
                    openScanner();
                else
                    reqCameraPermission();
            }
        });
    }

    void openScanner(){
        startActivity(new Intent(getApplicationContext(), FullScannerActivity.class));
    }

    private void reqCameraPermission() {
        new MyMaterialDialog(this)
                .withDialogAnimation(true)
                .setHeaderDrawable(R.drawable.ic_info)
                .setTitle(R.string.notice)
                .setMessage(getString(R.string.req_camera_permission))
                .setPositiveButton(R.string.ok, new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {
                        String permission = Manifest.permission.CAMERA;
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permission},REQ_CAMERA);
                        } else {
                            if (!MyApplication.sharedPreferences.getBoolean(permission,false)) {
                                MyApplication.sharedPreferences.edit().putBoolean(permission,true).apply();
                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{permission},REQ_CAMERA);
                            } else {
                                openAppSettings();
                            }
                        }
                    }
                })
                .show();
    }

    private void openAppSettings() {
        Intent intent = new  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQ_CAMERA && hasCameraPermission())
            openScanner();
    }

    private boolean hasCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        new MyMaterialDialog(this)
                .setHeaderDrawable(R.drawable.ic_info)
                .setTextIsSelectable(true)
                .setMessage(MyApplication.markwon.toMarkdown(getString(R.string.about_text)))
                .withDialogAnimation(true)
                .setPositiveButton(R.string.check_update, new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {
                        appUpdater.check(true);
                    }
                })
                .setNegativeButton(R.string.change_color, new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {
                        showThemeDialog();
                    }
                })
                .show();
    }

    private void showThemeDialog() {
        int selected = MyApplication.sharedPreferences.getInt(Constants.DARK_THEME_KEY,0);
        View v =getLayoutInflater().inflate(R.layout.change_theme_dialog,null);
        RadioGroup action_dark_mode = v.findViewById(R.id.action_dark_mode);

        if (selected == 1)
            selected = R.id.dark;
        else if (selected == 2)
            selected = R.id.light;
        else selected = R.id.auto;

        action_dark_mode.check(selected);
        action_dark_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });

        ColorRecyclerAdapter adapter = new ColorRecyclerAdapter(this,themes);

        new MyMaterialDialog(this)
                .withDialogAnimation(true)
                .setIcon(R.drawable.ic_color)
                .setView(v)
                .setPositiveButton(getString(R.string.let_apply), new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {
                        int which = Integer.MIN_VALUE;
                        switch (action_dark_mode.getCheckedRadioButtonId()){
                            case R.id.auto:
                                which = 0;
                                break;
                            case R.id.light:
                                which = 2;
                                break;
                            case R.id.dark:
                                which = 1;
                                break;
                        }
                        MyApplication.sharedPreferences.edit().putInt(Constants.APP_THEME_KEY,adapter.getChecked()).apply();
                        if (which!=Integer.MIN_VALUE)
                            MyApplication.sharedPreferences.edit().putInt(Constants.DARK_THEME_KEY,which).apply();
                        recreate();
                    }
                })
                .show();

        RecyclerView recyclerView = v.findViewById(R.id.color_recycler);

        recyclerView.setLayoutManager(new GridLayoutManager(this,4));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if(!navController.navigateUp()) {
            new MyMaterialDialog(this)
                    .withDialogAnimation(true)
                    .setHeaderDrawable(R.drawable.ic_info)
                    .setTitle(getString(R.string.notice))
                    .setMessage(MyApplication.markwon.toMarkdown(getString(R.string.exit_msg)))
                    .setPositiveButton(getString(R.string.close), new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.contact), new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {
                            composeEmail(new String[]{getString(R.string.dev_email)},getString(R.string.app_name));
                        }
                    })
                    .show();
        }
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if(subject!=null) intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}