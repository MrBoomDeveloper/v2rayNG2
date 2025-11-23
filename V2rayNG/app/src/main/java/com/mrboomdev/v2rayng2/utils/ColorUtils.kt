package com.mrboomdev.v2rayng2.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun colorStateListOf(context: Context, @ColorRes color: Int): ColorStateList {
    return ColorStateList.valueOf(ContextCompat.getColor(context, color))
}

fun Drawable.setColor(context: Context, @ColorRes color: Int) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this),
        ContextCompat.getColor(context, color)
    );
}