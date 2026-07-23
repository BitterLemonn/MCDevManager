package com.lemon.mcdevmanagermp.ui.iconpack

import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.collections.List as ____KtList

public object IconPack

private var __AllIcons: ____KtList<ImageVector>? = null

public val IconPack.AllIcons: ____KtList<ImageVector>
    get() {
        if (__AllIcons != null) {
            return __AllIcons!!
        }
        __AllIcons = listOf(
            IconPack.Analyze, IconPack.BarChart, IconPack.Calendar, IconPack.Comment,
            IconPack.Dashboard, IconPack.Feedback, IconPack.Filter, IconPack.License,
            IconPack.LineChart, IconPack.Menu, IconPack.Mod, IconPack.Modified,
            IconPack.NoReply, IconPack.NoShow, IconPack.Profit, IconPack.Replied,
            IconPack.Sale, IconPack.Setting, IconPack.Show, IconPack.Star,
            IconPack.Total, IconPack.User, IconPack.Work
        )
        return __AllIcons!!
    }
