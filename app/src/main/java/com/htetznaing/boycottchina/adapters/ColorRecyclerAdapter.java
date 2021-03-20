package com.htetznaing.boycottchina.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.items.ColorItem;
import com.htetznaing.boycottchina.MyApplication;
import com.htetznaing.boycottchina.R;

import java.util.List;

public class ColorRecyclerAdapter extends RecyclerView.Adapter<ColorRecyclerAdapter.MyViewHolder> {
    private final Context context;
    private final List<ColorItem> themes;
    private int cur_item,last_checked = Integer.MIN_VALUE;

    public ColorRecyclerAdapter(Context context, List<ColorItem> themes) {
        this.context = context;
        this.themes = themes;
        cur_item = MyApplication.sharedPreferences.getInt(Constants.APP_THEME_KEY,0);
    }

    public int getChecked(){
        return cur_item;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.color_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.checked.setBackgroundColor(themes.get(position).getPreview());
        holder.itemView.setTag(position);
        if (cur_item==position || cur_item==themes.get(position).getTheme()){
            last_checked = position;
            ImageViewCompat.setImageTintList( holder.checked, ColorStateList.valueOf(Color.WHITE));
        }else {
            ImageViewCompat.setImageTintList(holder.checked,ColorStateList.valueOf(themes.get(position).getPreview()));
        }
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView checked;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checked = itemView.findViewById(R.id.iv_checked);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (itemView.getTag()==null)return;

                    int index = (int) itemView.getTag();

                    cur_item = themes.get(index).getTheme();

                    notifyItemChanged(index);

                    if (last_checked!=Integer.MIN_VALUE)
                        notifyItemChanged(last_checked);

                    last_checked = index;
                }
            });
        }
    }
}
