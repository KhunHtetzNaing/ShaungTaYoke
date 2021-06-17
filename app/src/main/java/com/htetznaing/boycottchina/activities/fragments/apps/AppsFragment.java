package com.htetznaing.boycottchina.activities.fragments.apps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.adapters.AppRecyclerAdapter;
import com.htetznaing.boycottchina.dialogs.MyMaterialDialog;
import com.htetznaing.boycottchina.items.AppItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AppsFragment extends Fragment {
    private PackageManager packageManager;
    private final List<AppItem> data = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private AppRecyclerAdapter appRecyclerAdapter;
    private final int UNINSTALL_CODE = 1;
    private AppItem currentItem;
    private int colorPrimary;
    private TextView app_notice;
    private SharedPreferences sharedPreferences;
    private final String DO_NOT_ASK_AGAIN = "dont_ask";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        packageManager = requireActivity().getPackageManager();
        colorPrimary = Constants.getAttr(requireContext(),R.attr.colorPrimary);
        appRecyclerAdapter = new AppRecyclerAdapter(requireContext(),data);
        appRecyclerAdapter.setOnItemClickListener(new AppRecyclerAdapter.OnItemClick() {
            @Override
            public void clicked(AppItem item) {
                currentItem = item;
                askToUninstall();
            }
        });

        app_notice = view.findViewById(R.id.app_notice);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_app);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(appRecyclerAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadApps();
            }
        });
        updateFoundedList();
        swipeRefreshLayout.setRefreshing(true);
        loadApps();
    }

    private void askToUninstall() {
        if (sharedPreferences.getBoolean(DO_NOT_ASK_AGAIN,false)){
            uninstall();
        }else {
            View customView = getLayoutInflater().inflate(R.layout.uninstall_dialog, null);
            ((TextView) customView.findViewById(R.id.msg)).setText(getString(R.string.uninstall_msg, currentItem.getName()));
            ((CheckBox)customView.findViewById(R.id.do_not_ask)).setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferences.edit().putBoolean(DO_NOT_ASK_AGAIN,isChecked).apply());

            new MyMaterialDialog(requireActivity())
                    .setCancelable(false)
                    .setIcon(currentItem.getIcon())
                    .setView(customView)
                    .setPositiveButton(R.string.yes, new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {
                            uninstall();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {
                            Toast.makeText(requireContext(), R.string.uninstall_app_notice, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }

    private void uninstall(){
        startActivityForResult(Constants.uninstall(currentItem.getPackageName()),UNINSTALL_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==UNINSTALL_CODE && currentItem!=null && !Constants.appInstalled(getContext(),currentItem.getPackageName())){
            Constants.foundedList.remove(currentItem.getPackageName());
            updateFoundedList();
        }
    }

    private void updateFoundedList(){
        data.clear();
        data.addAll(Constants.foundedList.values());
        appRecyclerAdapter.notifyDataSetChanged();
        app_notice.setText(data.isEmpty() ? R.string.no_apps : R.string.uninstall_app_notice);
        app_notice.setTextColor(data.isEmpty() ? colorPrimary : ContextCompat.getColor(requireContext(),R.color.main));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadApps();
    }

    public AppItem fetchDetail(String packageName) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String name = info.loadLabel(packageManager).toString();
            String size = Constants.calculateFileSize(new File(info.publicSourceDir).length());
            Drawable drawable = info.loadIcon(packageManager);

            AppItem appItem = new AppItem();
            appItem.setIcon(drawable);
            appItem.setName(name);
            appItem.setSize(size);
            appItem.setPackageName(info.packageName);
            appItem.setSystem(isSystemPackage(info.flags));
            return appItem;
        } catch (PackageManager.NameNotFoundException e) {
            System.err.println("Not Found => "+packageName);
        }
        return null;
    }

    private void loadApps(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (String k:Constants.chinaAppList.keySet()){
                    AppItem item = fetchDetail(k);
                    if (item!=null) {
                        Constants.foundedList.put(k,item);
                    }
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        if (getContext()!=null)
                            updateFoundedList();
                    }
                });
            }
        });
    }

    // Custom method to determine an app is system app
    public boolean isSystemPackage(int flags){
        return (flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}