package com.htetznaing.boycottchina.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.htetznaing.boycottchina.R;

public class MyProgressDialog {
    private final AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private CharSequence mTitle;
    private final int padding;
    private OnClicked onClicked;
    private final View containerView;
    private TextView msg;
    private Activity context;
    public MyProgressDialog(Activity context) {
        this.context = context;
        dialogBuilder = new AlertDialog.Builder(context);
        containerView = LayoutInflater.from(context).inflate(R.layout.progress_dialog_content,null);
        msg = containerView.findViewById(R.id.msg);
        msg.setTypeface(ResourcesCompat.getFont(context,R.font.mm));
        padding = containerView.getPaddingRight();
    }

    public MyProgressDialog setIcon(int icon){
        dialogBuilder.setIcon(icon);
        return this;
    }

    public MyProgressDialog setCancelable(boolean how){
        dialogBuilder.setCancelable(how);
        return this;
    }

    public MyProgressDialog setIcon(Drawable icon){
        dialogBuilder.setIcon(icon);
        return this;
    }

    public void show(){
        if (context.isFinishing())return;
        msg.setPadding(padding,0,0,0);
        if(containerView.getParent() != null) {
            ((ViewGroup)containerView.getParent()).removeView(containerView); // <- fix
        }
        dialog = dialogBuilder
                .setView(containerView)
                .setTitle(mTitle)
                .show();
    }

    public MyProgressDialog setOnClickListener(OnClicked clicked){
        onClicked = clicked;
        return this;
    }

    public MyProgressDialog addButton(int btnType, String text){
        if (btnType== AlertDialog.BUTTON_POSITIVE) {
            containerView.setPadding(padding,padding/2,padding,0);
            dialogBuilder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onClicked != null) onClicked.onButton(dialog, AlertDialog.BUTTON_POSITIVE);
                }
            });
        }else if (btnType== AlertDialog.BUTTON_NEGATIVE) {
            containerView.setPadding(padding,padding/2,padding,0);
            dialogBuilder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onClicked != null) onClicked.onButton(dialog, AlertDialog.BUTTON_NEGATIVE);
                }
            });
        }else if (btnType== AlertDialog.BUTTON_NEUTRAL) {
            containerView.setPadding(padding,padding/2,padding,0);
            dialogBuilder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (onClicked != null) onClicked.onButton(dialog, AlertDialog.BUTTON_NEUTRAL);
                }
            });
        }
        return this;
    }

    public MyProgressDialog setTitle(CharSequence title){
        mTitle = title;
        if (mTitle!=null) {
            containerView.setPadding(padding,padding/2,padding,padding);
        }
        return this;
    }

    public void dismiss(){
        if (!context.isFinishing() && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public MyProgressDialog setMessage(CharSequence message){
        msg.setText(message);
        return this;
    }

    public interface OnClicked{
        void onButton(AlertDialog dialog, int position);
        void onItem(AlertDialog dialog, int position);
    }
}
