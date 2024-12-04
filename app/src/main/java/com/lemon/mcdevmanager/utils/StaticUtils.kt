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

// 获取平均分布的元素 必须包含第一个和最后一个
fun <T> getAvgItems(list: List<T>, count: Int): List<T> {
    val result = mutableListOf<T>()
    val size = list.size
    if (size <= count) {
        return list
    }
    val step = size / (count - 1)
    for (i in 0 until count) {
        val index = i * step
        if (index < size) {
            result.add(list[index])
        }
    }
    if (list.last() != result.last()) {
        result.add(list.last())
    }
    return result
}

fun getFontScale(context: Context): Float {
    return context.resources.configuration.fontScale
}