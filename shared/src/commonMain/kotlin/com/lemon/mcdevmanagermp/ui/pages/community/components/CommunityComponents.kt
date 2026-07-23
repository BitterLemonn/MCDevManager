package com.lemon.mcdevmanagermp.ui.pages.community.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.iconpack.Filter
import com.lemon.mcdevmanagermp.ui.iconpack.IconPack
import com.lemon.mcdevmanagermp.ui.iconpack.Star
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

// ============================================================
// Data classes
// ============================================================

data class FilterGroupDef(
    val key: String,
    val label: String,
    val options: List<String>,
    val selectedValues: Set<String>,
    val exclusive: Boolean = true,
)

// ============================================================
// ModernFilterBar — collapsible filter panel
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModernFilterBar(
    groups: List<FilterGroupDef>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String = "搜索...",
    onToggleFilter: (groupKey: String, value: String) -> Unit,
    onClearAll: () -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    expandedExtras: (@Composable () -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    val hasActiveFilters = groups.any { it.selectedValues.isNotEmpty() } || searchQuery.isNotBlank()
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),

    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Search row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        searchPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "清除",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline.copy(alpha = 0.5f),
                    focusedContainerColor = colors.surfaceContainerLowest,
                    unfocusedContainerColor = colors.surfaceContainerLowest,
                    focusedTextColor = colors.textColor,
                    unfocusedTextColor = colors.textColor,
                    cursorColor = colors.primary,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Header: filter icon + active badges + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconPack.Filter,
                    contentDescription = null,
                    tint = if (hasActiveFilters) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))

                // Active badges (horizontally scrollable)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasActiveFilters) {
                        groups.forEach { group ->
                            group.selectedValues.forEach { value ->
                                ActiveFilterBadge(
                                    label = "${group.label}: $value",
                                    onRemove = { onToggleFilter(group.key, value) }
                                )
                            }
                        }
                        if (searchQuery.isNotBlank()) {
                            ActiveFilterBadge(
                                label = "\"$searchQuery\"",
                                onRemove = { onSearchChange("") }
                            )
                        }
                    } else {
                        Text(
                            text = "筛选条件",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                // Clear & expand buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasActiveFilters) {
                        Surface(
                            onClick = onClearAll,
                            shape = RoundedCornerShape(8.dp),
                            color = colors.error.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = "清除",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.error,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onToggleExpanded,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "收起" else "展开更多筛选",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp).rotate(
                                if (isExpanded) 0f else 180f
                            )
                        )
                    }
                }
            }

            // Expandable filter chip groups — scrollable to avoid squeezing the list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = colors.outlineVariant, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))

                    groups.forEach { group ->
                        if (group.options.isNotEmpty()) {
                            FilterChipGroup(
                                label = group.label,
                                options = group.options,
                                selectedValues = group.selectedValues,
                                onToggle = { value -> onToggleFilter(group.key, value) },
                                exclusive = group.exclusive
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    if (expandedExtras != null) {
                        expandedExtras()
                    }
                }
            }
        }
    }
}

// ============================================================
// FilterChipGroup — labeled section with FlowRow of chips
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipGroup(
    label: String,
    options: List<String>,
    selectedValues: Set<String>,
    onToggle: (String) -> Unit,
    exclusive: Boolean = true,
) {
    val colors = LocalAppColors.current

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedValues.contains(option)
                FilterChipItem(
                    text = option,
                    isSelected = isSelected,
                    onClick = { onToggle(option) }
                )
            }
        }
    }
}

// ============================================================
// FilterChipItem — a single selectable chip
// ============================================================

@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current

    val bg = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainerHighest
    val fg = if (isSelected) colors.primary else colors.onSurfaceVariant
    val border =
        if (isSelected) colors.primary.copy(alpha = 0.4f) else colors.outline.copy(alpha = 0.3f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

// ============================================================
// StarChipGroup — 1-5 star rating filter chips
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StarChipGroup(
    selectedStars: Set<String>,
    onToggle: (String) -> Unit,
) {
    val colors = LocalAppColors.current
    val starColor = Color(0xFFFFA000)

    Column {
        Text(
            text = "星级",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (1..5).forEach { star ->
                val starStr = star.toString()
                val isSelected = selectedStars.contains(starStr)
                val bg =
                    if (isSelected) starColor.copy(alpha = 0.15f) else colors.surfaceContainerHighest
                val border =
                    if (isSelected) starColor.copy(alpha = 0.5f) else colors.outline.copy(alpha = 0.3f)

                Surface(
                    onClick = { onToggle(starStr) },
                    shape = RoundedCornerShape(20.dp),
                    color = bg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, border),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(star) {
                            Icon(
                                imageVector = IconPack.Star,
                                contentDescription = null,
                                tint = if (isSelected) starColor else starColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// DateRangeChipGroup — quick-select date range
// ============================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateRangeChipGroup(
    selectedRange: String?,
    onSelect: (String?) -> Unit,
    getLabel: (String) -> String = { it },
) {
    val colors = LocalAppColors.current
    val ranges = listOf("today", "week", "month", "quarter", "all")
    val labels = listOf("今天", "近7天", "近30天", "近90天", "全部")

    Column {
        Text(
            text = "日期范围",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ranges.forEachIndexed { index, range ->
                val label = labels[index]
                val isSelected = selectedRange == range

                val bg =
                    if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceContainerHighest
                val fg = if (isSelected) colors.primary else colors.onSurfaceVariant
                val border =
                    if (isSelected) colors.primary.copy(alpha = 0.4f) else colors.outline.copy(alpha = 0.3f)

                Surface(
                    onClick = { onSelect(if (isSelected) null else range) },
                    shape = RoundedCornerShape(20.dp),
                    color = bg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, border),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = fg,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
// ActiveFilterBadge — removable filter indicator
// ============================================================

@Composable
fun ActiveFilterBadge(
    label: String,
    onRemove: () -> Unit,
) {
    val colors = LocalAppColors.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.primary.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "移除",
                    tint = colors.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

// ============================================================
// EmptyDetailPlaceholder (kept from original)
// ============================================================

@Composable
fun EmptyDetailPlaceholder(
    icon: ImageVector,
    hint: String
) {
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// Date utility
// ============================================================

fun computeDateRange(range: String?): Pair<String?, String?> {
    if (range == null || range == "all") return null to null

    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(tz).date

    val endDate = today.toString()

    val startDate = when (range) {
        "today" -> today.toString()
        "week" -> today.minus(7, DateTimeUnit.DAY).toString()
        "month" -> today.minus(30, DateTimeUnit.DAY).toString()
        "quarter" -> today.minus(90, DateTimeUnit.DAY).toString()
        else -> null
    }

    return startDate to endDate
}
