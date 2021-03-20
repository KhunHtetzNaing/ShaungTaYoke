package com.htetznaing.boycottchina.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.items.AppItem;

import java.util.List;

public class AppRecyclerAdapter extends RecyclerView.Adapter<AppRecyclerAdapter.MyViewHolder> {
    private final Context context;
    private final List<AppItem> data;
    private OnItemClick click;

    public AppRecyclerAdapter(Context context,List<AppItem> data) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.apps_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppItem item = data.get(position);
        holder.icon.setImageDrawable(item.getIcon());
        holder.size.setText(item.getSize());
        holder.name.setText(item.getName());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,size;
        ImageView icon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            size = itemView.findViewById(R.id.size);
            icon = itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int index = (int) itemView.getTag();
                        if (click!=null)
                            click.clicked(data.get(index));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClick itemClick){
        click = itemClick;
    }

    public interface OnItemClick{
        void clicked(AppItem item);
    }
}
