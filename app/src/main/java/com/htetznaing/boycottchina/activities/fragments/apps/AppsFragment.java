package com.htetznaing.boycottchina.activities.fragments.apps;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.adapters.AppRecyclerAdapter;
import com.htetznaing.boycottchina.items.AppItem;
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                startActivityForResult(Constants.uninstall(currentItem.getPackageName()),UNINSTALL_CODE);
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