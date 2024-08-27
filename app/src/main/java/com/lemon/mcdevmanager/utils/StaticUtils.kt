package com.lemon.mcdevmanager.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

fun getNavigationBarHeight(context: Context): Int {
    val resources: Resources = context.resources
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        pxToDp(context, resources.getDimensionPixelSize(resourceId).toFloat())
    } else 0
}

fun getScreenWidth(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return windowManager.currentWindowMetrics.bounds.width()
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }
}

fun getScreenHeight(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return windowManager.currentWindowMetrics.bounds.height()
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}

fun pxToDp(context: Context, px: Float): Int {
    return Math.round(px / context.resources.displayMetrics.density)
}

fun dpToPx(context: Context, dp: Int): Int {
    return Math.round(dp * context.resources.displayMetrics.density)
}

fun getNoScaleTextSize(context: Context, textSize: Float): Float {
    val fontScale = getFontScale(context)
    if (fontScale > 1.0f) {
        return textSize / fontScale
    }
    return textSize
}

fun <T> getAvgItems(list: List<T>, count: Int): List<T> {
    if (count <= 0 || list.isEmpty()) {
        return emptyList()
    }
    val step = list.size / count
    val selectedItems = mutableListOf<T>()
    var index = 0
    while (index < list.size && selectedItems.size < count) {
        selectedItems.add(list[index])
        index += step
    }
    return selectedItems
}

fun getFontScale(context: Context): Float {
    return context.resources.configuration.fontScale
}