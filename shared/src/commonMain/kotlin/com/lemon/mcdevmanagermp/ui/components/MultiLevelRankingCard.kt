package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.lemon.mcdevmanagermp.data.page.RankCategoryContent
import com.lemon.mcdevmanagermp.data.page.RankCategoryData
import com.lemon.mcdevmanagermp.data.page.RankCategoryTypeEnum
import com.lemon.mcdevmanagermp.data.page.RankSubCategoryTypeEnum
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.ic_arrow_down
import mcdevmanagermpr.shared.generated.resources.ic_arrow_up
import mcdevmanagermpr.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

@Composable
fun MultiLevelRankingCard(
    data: List<RankCategoryData>,
    onChange: (RankCategoryTypeEnum, RankSubCategoryTypeEnum?) -> Unit = { _, _ -> }
) {
    val colors = LocalAppColors.current
    if (data.isEmpty()) return

    var selectedCategoryTitle by remember { mutableStateOf(data.first().categoryTitle) }
    val selectedCategory = remember(data, selectedCategoryTitle) {
        data.find { it.categoryTitle == selectedCategoryTitle } ?: data.first()
    }
    var selectedSubCategoryName by remember { mutableStateOf("") }
    var isShowAll by remember { mutableStateOf(false) }

    val currentContent = selectedCategory.content
    val subTabTitles = remember(selectedCategory) {
        when (currentContent) {
            is RankCategoryContent.Multi -> currentContent.groups.map { it.categoryName }
            is RankCategoryContent.Single -> emptyList()
        }
    }
    val currentList = remember(selectedCategory, selectedSubCategoryName) {
        when (currentContent) {
            is RankCategoryContent.Single -> currentContent.list
            is RankCategoryContent.Multi -> {
                val group = currentContent.groups.find { it.categoryName == selectedSubCategoryName }
                    ?: currentContent.groups.firstOrNull()
                group?.data ?: emptyList()
            }
        }
    }

    LaunchedEffect(selectedCategory.categoryTitle) {
        isShowAll = false
        selectedSubCategoryName = if (currentContent is RankCategoryContent.Multi)
            currentContent.groups.firstOrNull()?.categoryName ?: "" else ""
    }

    val triggerFetch = {
        onChange(
            when (selectedCategory.categoryTitle) {
                RankCategoryTypeEnum.PE_HOT.typeName -> RankCategoryTypeEnum.PE_HOT
                RankCategoryTypeEnum.HOT_SEARCH.typeName -> RankCategoryTypeEnum.HOT_SEARCH
                RankCategoryTypeEnum.PE_DOWNLOAD.typeName -> RankCategoryTypeEnum.PE_DOWNLOAD
                RankCategoryTypeEnum.PE_SELL.typeName -> RankCategoryTypeEnum.PE_SELL
                RankCategoryTypeEnum.PC_DOWNLOAD.typeName -> RankCategoryTypeEnum.PC_DOWNLOAD
                RankCategoryTypeEnum.PC_LIKE.typeName -> RankCategoryTypeEnum.PC_LIKE
                else -> RankCategoryTypeEnum.PE_HOT
            },
            if (selectedSubCategoryName.isNotEmpty()) {
                when (selectedSubCategoryName) {
                    RankSubCategoryTypeEnum.MOD.typeName -> RankSubCategoryTypeEnum.MOD
                    RankSubCategoryTypeEnum.MAP.typeName -> RankSubCategoryTypeEnum.MAP
                    RankSubCategoryTypeEnum.RESOURCE_PACK.typeName -> RankSubCategoryTypeEnum.RESOURCE_PACK
                    RankSubCategoryTypeEnum.SERVER.typeName -> RankSubCategoryTypeEnum.SERVER
                    else -> null
                }
            } else null
        )
    }

    LaunchedEffect(selectedCategory.categoryTitle, selectedSubCategoryName) {
        if (currentList.isEmpty()) triggerFetch()
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val selectedIndex = data.indexOf(selectedCategory).coerceAtLeast(0)
                PrimaryScrollableTabRow(
                    modifier = Modifier.weight(1f),
                    edgePadding = 0.dp,
                    selectedTabIndex = selectedIndex,
                    containerColor = colors.surfaceContainerHigh,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedIndex)
                        )
                    }
                ) {
                    data.forEach { item ->
                        Tab(
                            selected = selectedCategory == item,
                            onClick = { selectedCategoryTitle = item.categoryTitle },
                            text = {
                                Text(
                                    text = item.categoryTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = subTabTitles.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subTabTitles) { title ->
                            FilterChip(
                                selected = selectedSubCategoryName == title,
                                onClick = { selectedSubCategoryName = title },
                                label = { Text(text = title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = colors.surfaceContainerHigh,
                                    selectedContainerColor = colors.secondaryContainer
                                )
                            )
                        }
                        item {
                            IconButton(onClick = { triggerFetch() }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_refresh),
                                    contentDescription = "Refresh",
                                    modifier = Modifier.size(24.dp),
                                    tint = colors.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            val displayList = if (!isShowAll && currentList.size > 3) currentList.take(3) else currentList
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .heightIn(min = 100.dp, max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无数据", color = colors.onSurfaceVariant)
                    }
                } else {
                    displayList.forEach { item ->
                        RankingListItem(item)
                        if (item != displayList.last()) {
                            HorizontalDivider(color = colors.divider, thickness = 1.dp)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = !isShowAll && currentList.size > 3) {
                TextButton(
                    onClick = { isShowAll = true },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("查看全部榜单")
                }
            }
        }
    }
}

@Composable
private fun RankingListItem(item: com.lemon.mcdevmanagermp.data.page.RankListItemData) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.rank}",
            style = MaterialTheme.typography.titleLarge,
            color = when (item.rank) {
                1 -> MaterialTheme.colorScheme.primary
                2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (!item.imgUrl.isNullOrBlank()) {
            AsyncImage(
                uri = item.imgUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = colors.textColor,
            modifier = Modifier.weight(1f)
        )

        if (!item.isNew) {
            if (item.rankChange != 0) {
                Text(
                    text = "${abs(item.rankChange)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.rankChange > 0) colors.primary else colors.secondary,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                painter = painterResource(
                    if (item.rankChange > 0) Res.drawable.ic_arrow_up
                    else Res.drawable.ic_arrow_down
                ),
                contentDescription = null,
                tint = null,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = "NEW",
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = colors.primary,
            )
        }
    }
}
