package com.github.javiersantos.materialstyleddialogs

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.github.javiersantos.materialstyleddialogs.UtilsAnimation.startAnimation
import com.github.javiersantos.materialstyleddialogs.UtilsLibrary.dpToPixels
import com.github.javiersantos.materialstyleddialogs.UtilsLibrary.getColor
import com.github.javiersantos.materialstyleddialogs.UtilsLibrary.getPrimaryColor
import com.github.javiersantos.materialstyleddialogs.custom_interface.FuckDismissListener
import com.github.javiersantos.materialstyleddialogs.enums.Duration
import com.github.javiersantos.materialstyleddialogs.enums.Style

class MaterialStyledDialog(private val builder: Builder) : DialogBase(builder.context, R.style.MD_Dark) {

    @UiThread
    override fun show() {
        builder.dialog?.show()
    }

    @UiThread
    override fun dismiss() {
        builder.dialog?.dismiss()
    }

    @UiThread
    private fun initMaterialStyledDialog(builder: Builder): MaterialDialog {
        val dialog = MaterialDialog(builder.context)

        dialog.setOnDismissListener {
            builder.dismiss?.dismissed();
        }

        // Set cancelable
        dialog.cancelable(builder.isCancelable)

        // Set style
        dialog.customView(view = initStyle(builder), scrollable = false, noVerticalPadding = true)


        // Set positive button
        if (builder.positive != null && builder.positive!!.isNotEmpty())
            dialog.positiveButton(text = builder.positive, click = builder.positiveCallback)
        // set negative button
        if (builder.negative != null && builder.negative!!.isNotEmpty())
            dialog.negativeButton(text = builder.negative, click = builder.negativeCallback)

        // Set neutral button
        if (builder.neutral != null && builder.neutral!!.isNotEmpty())
            dialog.neutralButton(text = builder.neutral, click = builder.neutralCallback)

        // Set auto dismiss when touching the buttons
        if (!builder.isAutoDismiss) dialog.noAutoDismiss()

        // Set dialog animation and animation duration
        if (builder.isDialogAnimation) {
            dialog.window!!.attributes.windowAnimations = when(builder.duration) {
                Duration.FAST -> R.style.MaterialStyledDialogs_DialogAnimationFast
                Duration.SLOW -> R.style.MaterialStyledDialogs_DialogAnimationSlow
                else -> R.style.MaterialStyledDialogs_DialogAnimationNormal
            }
        }
        return dialog
    }

    @UiThread
    @TargetApi(16)
    private fun initStyle(builder: Builder): View {
        val contentView = when (builder.style) {
            Style.HEADER_WITH_ICON -> layoutInflater.inflate(R.layout.style_dialog_header_icon, null)
            else -> layoutInflater.inflate(R.layout.style_dialog_header_title, null)
        }

        // Init Views
        val dialogHeaderColor =
            contentView.findViewById(R.id.mdStyledHeaderColor) as RelativeLayout
        val dialogHeader =
            contentView.findViewById(R.id.mdStyledHeader) as AppCompatImageView
        val dialogPic =
            contentView.findViewById(R.id.mdStyledHeaderPic) as AppCompatImageView?
        val dialogTitle =
                contentView.findViewById(R.id.mdStyledDialogTitle) as TextView
        val dialogDescription =
            contentView.findViewById(R.id.mdStyledDialogDescription) as TextView
        val dialogCustomViewGroup =
            contentView.findViewById(R.id.mdStyledDialogCustomView) as FrameLayout
        val dialogDivider =
            contentView.findViewById(R.id.mdStyledDialogDivider) as View

        // Set header color or drawable
        if (builder.headerDrawable != null) {
            dialogHeader.setImageDrawable(builder.headerDrawable)
            // Apply gray/darker overlay to the header if enabled
            if (builder.isDarkerOverlay) {
                dialogHeader.setColorFilter(
                    Color.rgb(123, 123, 123),
                    PorterDuff.Mode.MULTIPLY
                )
            }
        }
        dialogHeaderColor.setBackgroundColor(builder.primaryColor)
        dialogHeader.scaleType = builder.headerScaleType

        //Set the custom view
        if (builder.customView != null) {
            dialogCustomViewGroup.addView(builder.customView)
            dialogCustomViewGroup.setPadding(
                builder.customViewPaddingLeft,
                builder.customViewPaddingTop,
                builder.customViewPaddingRight,
                builder.customViewPaddingBottom
            )
        }

        // Set header icon
        if (builder.iconDrawable != null) {
            if (builder.style === Style.HEADER_WITH_TITLE) {
                Log.e("MaterialStyledDialog", "You can't set an icon to the HEADER_WITH_TITLE style.")
            } else {
                dialogPic?.setImageDrawable(builder.iconDrawable)
            }
        }

        // Set dialog title
        if (builder.title != null && builder.title!!.isNotEmpty()) {
            dialogTitle.text = builder.title
            if (builder.typeface!=null)
                dialogTitle.typeface = builder.typeface
        } else {
            dialogTitle.visibility = View.GONE
        }

        // Set dialog description
        if (builder.description != null && builder.description!!.isNotEmpty()) {
            dialogDescription.text = builder.description
            // Set scrollable
            dialogDescription.isVerticalScrollBarEnabled = builder.isScrollable
            if (builder.isScrollable) {
                dialogDescription.maxLines = builder.maxLines!!
                dialogDescription.movementMethod = ScrollingMovementMethod()
            } else {
                dialogDescription.maxLines = Int.MAX_VALUE
            }
            if (builder.typeface!=null)
                dialogDescription.typeface = builder.typeface

            dialogDescription.setTextIsSelectable(builder.selectable)
        } else {
            dialogDescription.visibility = View.GONE
        }

        // Set icon animation
        if (builder.isIconAnimation) {
            if (builder.style !== Style.HEADER_WITH_TITLE)
                startAnimation(builder.context, dialogPic!!, builder.iconAnimation)
        }

        // Show dialog divider if enabled
        if (builder.isDialogDivider) dialogDivider.visibility = View.VISIBLE
        return contentView
    }

    fun getPositiveButton(): Button {
        return positiveButton() as Button
    }

    fun getNegativeButton(): Button{
        return negativeButton() as Button
    }

    fun getNeutralButton(): Button{
        return neutralButton() as Button
    }

    fun positiveButton() = builder.dialog?.getActionButton(WhichButton.POSITIVE)
    fun negativeButton() = builder.dialog?.getActionButton(WhichButton.NEGATIVE)
    @Deprecated("Neutral button is deprecated in the parent library: https://github.com/afollestad/material-dialogs")
    fun neutralButton() = builder.dialog?.getActionButton(WhichButton.NEUTRAL)

    class Builder(var context: Context) : IBuilder {

        var dialog: MaterialDialog? = null
        var style: Style?
        var duration: Duration?

        @AnimRes
        var iconAnimation: Int
        var isIconAnimation: Boolean
        var isDialogAnimation: Boolean
        var isDialogDivider: Boolean
        var isCancelable: Boolean
        var isScrollable: Boolean
        var isDarkerOverlay: Boolean
        var isAutoDismiss: Boolean
        var headerDrawable: Drawable? = null
        var iconDrawable: Drawable? = null
        var primaryColor: Int
        var maxLines: Int?
        var title: CharSequence? = null
        var typeface: Typeface? = null
        var selectable: Boolean = false
        var description: CharSequence? = null
        var customView: View? = null
        var customViewPaddingLeft = 0
        var customViewPaddingTop = 0
        var customViewPaddingRight = 0
        var customViewPaddingBottom = 0
        var headerScaleType: ScaleType?
        var positive: CharSequence? = null
        var negative: CharSequence? = null
        var neutral: CharSequence? = null
        var positiveCallback: DialogCallback? = null
        var dismiss: FuckDismissListener? = null
        var negativeCallback: DialogCallback? = null
        var neutralCallback: DialogCallback? = null

        override fun setCustomView(customView: View?): Builder {
            this.customView = customView
            customViewPaddingLeft = 0
            customViewPaddingRight = 0
            customViewPaddingTop = 0
            customViewPaddingBottom = 0
            return this
        }

        override fun setCustomView(
            customView: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ): Builder {
            this.customView = customView
            customViewPaddingLeft = dpToPixels(context, left)
            customViewPaddingRight = dpToPixels(context, right)
            customViewPaddingTop = dpToPixels(context, top)
            customViewPaddingBottom = dpToPixels(context, bottom)
            return this
        }

        override fun setStyle(style: Style?): Builder {
            this.style = style
            return this
        }

        override fun withIconAnimation(withAnimation: Boolean?): Builder {
            isIconAnimation = withAnimation!!
            return this
        }

        override fun withDialogAnimation(withAnimation: Boolean?): Builder {
            isDialogAnimation = withAnimation!!
            return this
        }

        override fun withDialogAnimation(
            withAnimation: Boolean?,
            duration: Duration?
        ): Builder {
            isDialogAnimation = withAnimation!!
            this.duration = duration
            return this
        }

        override fun withDivider(withDivider: Boolean?): Builder {
            isDialogDivider = withDivider!!
            return this
        }

        override fun withDarkerOverlay(withDarkerOverlay: Boolean?): Builder {
            isDarkerOverlay = withDarkerOverlay!!
            return this
        }

        override fun setIcon(icon: Drawable): Builder {
            iconDrawable = icon
            return this
        }

        override fun setIcon(@DrawableRes iconRes: Int?): Builder {
            iconDrawable = ResourcesCompat.getDrawable(context.resources, iconRes!!, null)
            return this
        }

        override fun setIconAnimation(@AnimRes animResource: Int): Builder {
            iconAnimation = animResource
            return this
        }

        override fun setTitle(@StringRes titleRes: Int): Builder {
            setTitle(context.getString(titleRes))
            return this
        }

        override fun setTitle(title: CharSequence): Builder {
            this.title = title
            return this
        }

        override fun setDescription(@StringRes descriptionRes: Int): Builder {
            setDescription(context.getString(descriptionRes))
            return this
        }

        override fun setDescription(description: CharSequence): Builder {
            this.description = description
            return this
        }

        override fun setHeaderColor(@ColorRes color: Int): Builder {
            primaryColor = getColor(context, color)
            return this
        }

        override fun setHeaderColorInt(@ColorInt color: Int): Builder {
            primaryColor = color
            return this
        }

        override fun setHeaderDrawable(drawable: Drawable): Builder {
            headerDrawable = drawable
            return this
        }

        override fun setHeaderDrawable(@DrawableRes drawableRes: Int?): Builder {
            headerDrawable =
                ResourcesCompat.getDrawable(context.resources, drawableRes!!, null)
            return this
        }

        @Deprecated("")
        override fun setPositive(
            text: String?,
            callback: DialogCallback?
        ): Builder {
            positive = text
            positiveCallback = callback
            return this
        }

        override fun setPositiveText(@StringRes buttonTextRes: Int): Builder {
            setPositiveText(context.getString(buttonTextRes))
            return this
        }

        override fun setPositiveText(buttonText: CharSequence): Builder {
            positive = buttonText
            return this
        }

        override fun onPositive(callback: DialogCallback): Builder {
            positiveCallback = callback
            return this
        }

        @Deprecated("")
        override fun setNegative(
            text: String?,
            callback: DialogCallback?
        ): Builder {
            negative = text
            negativeCallback = callback
            return this
        }

        override fun setNegativeText(@StringRes buttonTextRes: Int): Builder {
            setNegativeText(context.getString(buttonTextRes))
            return this
        }

        override fun setNegativeText(buttonText: CharSequence): Builder {
            negative = buttonText
            return this
        }

        override fun onNegative(callback: DialogCallback): Builder {
            negativeCallback = callback
            return this
        }

        @Deprecated("")
        override fun setNeutral(
            text: String?,
            callback: DialogCallback?
        ): Builder {
            neutral = text
            neutralCallback = callback
            return this
        }

        @Deprecated("")
        override fun setNeutralText(@StringRes buttonTextRes: Int): Builder {
            setNeutralText(context.getString(buttonTextRes))
            return this
        }

        @Deprecated("")
        override fun setNeutralText(buttonText: CharSequence): Builder {
            neutral = buttonText
            return this
        }

        @Deprecated("")
        override fun onNeutral(callback: DialogCallback): Builder {
            neutralCallback = callback
            return this
        }

        override fun setHeaderScaleType(scaleType: ScaleType?): Builder {
            headerScaleType = scaleType
            return this
        }

        override fun setCancelable(cancelable: Boolean?): Builder {
            isCancelable = cancelable!!
            return this
        }

        override fun setScrollable(scrollable: Boolean?): Builder {
            isScrollable = scrollable!!
            return this
        }

        override fun setScrollable(
            scrollable: Boolean?,
            maxLines: Int?
        ): Builder {
            isScrollable = scrollable!!
            this.maxLines = maxLines
            return this
        }

        override fun autoDismiss(dismiss: Boolean?): Builder {
            isAutoDismiss = dismiss!!
            return this
        }

        @UiThread
        fun build() = MaterialStyledDialog(this)

        @UiThread
        fun show(): MaterialStyledDialog {
            val materialStyledDialog = build()
            materialStyledDialog.show()
            return materialStyledDialog
        }

        init {
            style = Style.HEADER_WITH_ICON
            isIconAnimation = true
            iconAnimation = R.anim.md_styled_zoom_in_out
            isDialogAnimation = false
            isDialogDivider = false
            isDarkerOverlay = false
            duration = Duration.NORMAL
            isCancelable = true
            primaryColor = getPrimaryColor(context)
            isScrollable = false
            maxLines = 5
            isAutoDismiss = true
            headerScaleType = ScaleType.CENTER_CROP
        }
    }

    init {
        builder.dialog = initMaterialStyledDialog(builder)
    }
}