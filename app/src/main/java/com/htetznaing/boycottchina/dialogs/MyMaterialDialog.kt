package com.htetznaing.boycottchina.dialogs

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.custom_interface.FuckDismissListener
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.htetznaing.boycottchina.R

class MyMaterialDialog(mContext: Activity) {
    private var contexnt = mContext
    private val builder = MaterialStyledDialog.Builder(mContext)


    private lateinit var dialog:MaterialStyledDialog

    fun setScrollable(scroll: Boolean): MyMaterialDialog {
        builder.isScrollable = scroll
        return this;
    }

    fun setTitle(title: CharSequence): MyMaterialDialog {
        builder.title = title
        return this;
    }

    fun setTitle(title: Int): MyMaterialDialog {
        builder.title = contexnt.getString(title)
        return this;
    }

    fun setMessage(message: Int): MyMaterialDialog {
        builder.description = contexnt.getString(message)
        return this;
    }

    fun setMessage(message: CharSequence): MyMaterialDialog {
        builder.description = message
        return this;
    }

    fun setView(customView: View): MyMaterialDialog {
        if (customView?.parent != null) {
            (customView.parent as ViewGroup).removeView(customView) // <- fix
        }
        builder.setCustomView(customView)
        return this;
    }

    fun setView(customView: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int): MyMaterialDialog {
        if (customView?.parent != null) {
            (customView.parent as ViewGroup).removeView(customView) // <- fix
        }
        builder.setCustomView(customView, left, top, right, bottom);
        return this;
    }

    fun withDialogAnimation(animate: Boolean): MyMaterialDialog {
        builder.withDialogAnimation(animate)
        return this;
    }


    fun setCancelable(animate: Boolean): MyMaterialDialog {
        builder.setCancelable(animate)
        return this;
    }

    fun withDarkerOverlay(animate: Boolean): MyMaterialDialog {
        builder.withDarkerOverlay(animate)
        return this;
    }

    fun withIconAnimation(animate: Boolean): MyMaterialDialog {
        builder.withIconAnimation(animate)
        return this;
    }

    fun setStyle(style: Style): MyMaterialDialog {
        builder.setStyle(style)
        return this
    }

    fun setIcon(icon: Drawable): MyMaterialDialog {
        builder.setIcon(icon)
        return this
    }

    fun setIcon(@DrawableRes iconRes: Int?): MyMaterialDialog {
        builder.setIcon(iconRes)
        return this
    }

    fun setTextIsSelectable(selectable:Boolean):MyMaterialDialog{
        builder.selectable = selectable
        return this
    }

    fun setHeaderDrawable(drawable: Drawable): MyMaterialDialog {
        builder.setHeaderDrawable(drawable)
        return this
    }

    fun setHeaderDrawable(@DrawableRes drawable: Int?): MyMaterialDialog {
        builder.setIcon(drawable)
        return this
    }

    fun setHeaderColor(@ColorRes color: Int): MyMaterialDialog {
        builder.setHeaderColor(color)
        return this
    }

    fun setHeaderColorInt(@ColorInt color: Int): MyMaterialDialog {
        builder.setHeaderColorInt(color)
        return this
    }

    fun setPositiveButton(text: Int, clicked: OnClickedListener): MyMaterialDialog {
        builder.setPositiveText(text)
                .onPositive {
                    clicked.clicked(dialog)
                }
        return this
    }

    fun setPositiveButton(text: CharSequence, clicked: OnClickedListener): MyMaterialDialog {
        builder.setPositiveText(text)
                .onPositive {
                    clicked.clicked(dialog)
                }
        return this
    }


    fun setNeutralButton(text: Int, clicked: OnClickedListener): MyMaterialDialog {
        builder.neutral = contexnt.getString(text)
        builder.onNeutral {
            clicked.clicked(dialog)
        }
        return this
    }

    fun setNeutralButton(text: CharSequence, clicked: OnClickedListener): MyMaterialDialog {
        builder.neutral = text
        builder.onNeutral {
            clicked.clicked(dialog)
        }
        return this
    }

    fun setNegativeButton(text: Int, clicked: OnClickedListener): MyMaterialDialog {
        builder.setNegativeText(text)
                .onNegative {
                    clicked.clicked(dialog)
                }
        return this
    }

    fun setNegativeButton(text: CharSequence, clicked: OnClickedListener): MyMaterialDialog {
        builder.setNegativeText(text)
                .onNegative {
                    clicked.clicked(dialog)
                }

        return this
    }

    fun show():MaterialStyledDialog{
        return if (!contexnt.isFinishing) {
            builder.isScrollable = contexnt.resources.getBoolean(R.bool.scroll_dialog)
            builder.selectable = contexnt.resources.getBoolean(R.bool.scroll_dialog)
            val typeface = ResourcesCompat.getFont(contexnt, R.font.mm)
            builder.typeface = typeface
            dialog = builder.show()
            dialog.getNeutralButton().typeface = typeface
            dialog.getPositiveButton().typeface = typeface
            dialog.getNegativeButton().typeface = typeface
            dialog
        }else build()
    }

    fun dismiss(){
        dialog?.dismiss()
    }

    fun build():MaterialStyledDialog{
        dialog = builder.build()
        return dialog
    }

    fun isDarkerOverlay(isDark: Boolean):MyMaterialDialog{
        builder.isDarkerOverlay = isDark
        return this
    }

    fun onDismissed(onDismiss: FuckDismissListener):MyMaterialDialog{
        builder.dismiss = onDismiss

        return this
    }



    interface OnClickedListener {
        fun clicked(dialog: MaterialStyledDialog)
    }
}