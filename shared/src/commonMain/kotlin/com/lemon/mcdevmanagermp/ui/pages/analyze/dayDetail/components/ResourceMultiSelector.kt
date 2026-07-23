package com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.data.vo.netease.resource.ResourceData
import com.lemon.mcdevmanagermp.ui.pages.analyze.dayDetail.CHART_COLORS
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 多资源选择器
 * 支持复选最多5个资源，已选资源显示为标签
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResourceMultiSelector(
    resList: List<ResourceData>,
    selectedIIDs: List<String>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onToggleResource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val selectedResources = resList.filter { it.itemId in selectedIIDs }

    Column(modifier = modifier.fillMaxWidth()) {
        // 已选资源标签 + 添加按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceContainerHigh)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggle
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedResources.isEmpty()) {
                Text(
                    text = "点击选择资源进行对比",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
            } else {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    selectedResources.forEachIndexed { index, res ->
                        SelectedTag(
                            name = res.itemName,
                            colorIndex = index,
                            onRemove = { onToggleResource(res.itemId) }
                        )
                    }
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (isExpanded) "关闭" else "添加",
                modifier = Modifier.size(18.dp),
                tint = colors.onSurfaceVariant
            )
        }

        // 展开的资源列表
        if (isExpanded) {
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                ResourceListPanel(
                    resList = resList,
                    selectedIids = selectedIIDs,
                    onToggleResource = onToggleResource
                )
            }
        }
    }
}

/**
 * 已选资源标签
 */
@Composable
private fun SelectedTag(
    name: String,
    colorIndex: Int,
    onRemove: () -> Unit,
) {
    val colors = LocalAppColors.current
    val tagColor = CHART_COLORS[colorIndex.coerceAtMost(CHART_COLORS.size - 1)]
    val bgColor = tagColor.copy(alpha = 0.12f)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tagColor)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = name.take(8),
            style = MaterialTheme.typography.labelSmall,
            color = tagColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Spacer(Modifier.width(2.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "移除",
                modifier = Modifier.size(10.dp),
                tint = tagColor
            )
        }
    }
}

/**
 * 资源列表面板（带搜索）
 */
@Composable
private fun ResourceListPanel(
    resList: List<ResourceData>,
    selectedIids: List<String>,
    onToggleResource: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(resList, searchQuery) {
        if (searchQuery.isBlank()) resList
        else resList.filter {
            it.itemName.contains(searchQuery, ignoreCase = true) ||
                    it.itemId.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 搜索框
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text("搜索资源名称或 IID", color = colors.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceContainerLow,
                unfocusedContainerColor = colors.surfaceContainerLow,
                cursorColor = colors.primary,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = "已选 ${selectedIids.size}/5",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // 资源列表
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            items(filteredList) { res ->
                val isSelected = res.itemId in selectedIids
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onToggleResource(res.itemId) }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleResource(res.itemId) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colors.primary,
                            uncheckedColor = colors.onSurfaceVariant,
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = res.itemName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.textColor,
                            maxLines = 1
                        )
                        Text(
                            text = res.itemId,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(
                    color = colors.outlineVariant.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}
