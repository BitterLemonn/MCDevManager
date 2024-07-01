package com.lemon.mcdevmanager.utils

import android.content.Context
import android.content.res.Resources

fun getNavigationBarHeight(context: Context): Int {
    val resources: Resources = context.resources
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        pxToDp(context, resources.getDimensionPixelSize(resourceId).toFloat())
    } else 0
}

fun pxToDp(context: Context, px: Float): Int {
    return Math.round(px / context.resources.displayMetrics.density)
}