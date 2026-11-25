package com.lemon.mcdevmanagermp.ui.pages.work.workmanage.layout.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lemon.mcdevmanagermp.data.consts.enums.priceTypeLabel
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.components.tabularNumbers
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.WorkStatusFilter
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.matchesSearch
import com.lemon.mcdevmanagermp.ui.pages.work.workmanage.matchesStatusFilter
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

private enum class ArchivePickerFilter(val label: String, val targetFilter: WorkStatusFilter) {
    ALL("全部未归档", WorkStatusFilter.ALL),
    ATTENTION("异常/已下架", WorkStatusFilter.ATTENTION),
    DRAFT("待提审", WorkStatusFilter.DRAFT),
    ONLINE("已上架", WorkStatusFilter.ONLINE)
}

/**
 * 批量添加归档选择弹窗：支持搜索、状态快捷过滤与多选归入归档列表。
 */
@Composable
internal fun AddArchiveDialog(
    candidates: List<ResourceData>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ArchivePickerFilter.ALL) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val visibleCandidates = remember(candidates, searchQuery, filter) {
        candidates.filter { item ->
            item.matchesStatusFilter(filter.targetFilter) && item.matchesSearch(searchQuery)
        }
    }
    val visibleIds = remember(visibleCandidates) { visibleCandidates.map { it.itemId }.toSet() }
    val allVisibleSelected = visibleIds.isNotEmpty() && visibleIds.all { it in selectedIds }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Archive,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "添加归档",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textColor
                        )
                        Text(
                            text = "选择要隐藏出主列表的作品（仅本地生效）",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = colors.onSurfaceVariant
                        )
                    }
                }

                // 搜索框与快捷筛选
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkManageSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "搜索待归档作品名称或 ID"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ArchivePickerFilter.entries.forEach { option ->
                                val selected = filter == option
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selected) colors.secondaryContainer
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) Color.Transparent else colors.outlineVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { filter = option }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        if (visibleIds.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    selectedIds = if (allVisibleSelected) {
                                        selectedIds - visibleIds
                                    } else {
                                        selectedIds + visibleIds
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.primary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (allVisibleSelected) "取消全选" else "全选当前",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

                // 候选列表
                if (visibleCandidates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (candidates.isEmpty()) "暂无可归档的作品" else "当前筛选下暂无可归档作品",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 340.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleCandidates, key = { it.itemId }) { item ->
                            val checked = item.itemId in selectedIds
                            ArchiveCandidateRow(
                                item = item,
                                checked = checked,
                                onToggle = {
                                    selectedIds = if (checked) {
                                        selectedIds - item.itemId
                                    } else {
                                        selectedIds + item.itemId
                                    }
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

                // 底部确认栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已选择 ${selectedIds.size} 个作品",
                        style = MaterialTheme.typography.labelLarge.tabularNumbers(),
                        color = colors.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurfaceVariant)
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = { onConfirm(selectedIds) },
                            enabled = selectedIds.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary,
                                disabledContainerColor = colors.surfaceContainerHighest,
                                disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.55f)
                            )
                        ) {
                            Text("确认归档")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveCandidateRow(
    item: ResourceData,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    val titleColor = if (checked) colors.onSecondaryContainer else colors.onSurface
    val subColor = if (checked) colors.onSecondaryContainer.copy(alpha = 0.78f) else colors.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) colors.secondaryContainer
                else colors.surfaceContainerHighest
            )
            .border(
                width = 1.dp,
                color = if (checked) colors.primary.copy(alpha = 0.4f) else colors.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggle)
            .padding(start = 6.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                checkmarkColor = colors.onPrimary,
                uncheckedColor = colors.onSurfaceVariant
            )
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = item.itemName.ifBlank { "未命名作品" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val priceLabel = if (item.price <= 0) {
                "免费"
            } else {
                val unit = priceTypeLabel(item.priceType)
                if (unit.isEmpty()) "${item.price}" else "${item.price} $unit"
            }
            Text(
                text = "$priceLabel  ·  ID ${item.itemId}",
                style = MaterialTheme.typography.labelSmall.tabularNumbers(),
                color = subColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        WorkStatusBadge(status = item.getStatus())
    }
}
