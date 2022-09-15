package com.intelnet.omniwallet.util

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.intelnet.omniwallet.view.CustomTypefaceSpan

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.disable() {
    isEnabled = false
}

fun View.enabled() {
    isEnabled = true
}

fun Context.createDialog(layout: Int, cancelable: Boolean): Dialog {
    val dialog = Dialog(this, android.R.style.Theme_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(layout)
    dialog.window?.setGravity(Gravity.CENTER)
    dialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(cancelable)
    return dialog
}

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun TextView.setSpan(
    context: Context,
    content: String,
    text: String,
    font: Int? = null,
    color: Int? = null,
    isUnderline: Boolean = false,
    clickable: (() -> Unit)? = null
) {
    val stringBuilder = SpannableStringBuilder(content)

    val start = content.indexOf(text)
    if (start == -1) {
        this.text = stringBuilder
        return
    }
    val end = content.indexOf(text) + text.length

    clickable?.let {

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                it.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = isUnderline
            }
        }

        stringBuilder.setSpan(
            clickableSpan,
            start,
            end,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        this.movementMethod = LinkMovementMethod.getInstance()
    }

    font?.let {
        ResourcesCompat.getFont(context, font)?.let { typeface ->
            stringBuilder.setSpan(
                CustomTypefaceSpan("", typeface),
                start,
                end,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    color?.let {
        stringBuilder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, it)),
            start,
            end,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
    }

    this.text = stringBuilder
}

fun CompoundButton.setCustomChecked(value: Boolean, listener: CompoundButton.OnCheckedChangeListener) {
    setOnCheckedChangeListener(null)
    isChecked = value
    setOnCheckedChangeListener(listener)
}
