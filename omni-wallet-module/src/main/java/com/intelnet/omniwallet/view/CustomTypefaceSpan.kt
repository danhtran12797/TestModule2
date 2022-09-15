package com.intelnet.omniwallet.view

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(
    fm: String,
    private val tf: Typeface
) : TypefaceSpan(fm) {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, this.tf)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, this.tf)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old: Typeface? = paint.typeface
        oldStyle = old?.style ?: 0

        val fake = oldStyle and tf.style.inv()
        if ((fake and Typeface.BOLD) != 0) {
            paint.isFakeBoldText = true
        }

        if ((fake and Typeface.ITALIC) != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = tf
    }
}