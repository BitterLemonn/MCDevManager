package com.lemon.mcdevmanagermp.ui.pages.work.workdetail.layout.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.FormSection
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

/**
 * 尚未实现的占位模块。标题 / 说明 / 图标集中枚举，调用处只传 [PlaceholderModule]，
 * 避免在各个 layout 重复 import 图标。功能开发完成后将 [PlaceholderSection] 调用替换为真实表单。
 */
internal enum class PlaceholderModule(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    PE_RESOURCE(
        title = "上传 PE 资源管理",
        description = "管理 PE 端模组资源文件",
        icon = Icons.Filled.FolderZip
    ),
    PE_CAROUSEL(
        title = "PE 资源中心首页轮播推广图",
        description = "配置资源中心首页轮播推广图",
        icon = Icons.Filled.ViewCarousel
    )
}

/**
 * 占位区块：淡边框居中卡片（图标 + 说明 + 「即将推出」徽章），保持整体布局的视觉节奏，
 * 待对应功能开发后替换为真实表单。
 */
@Composable
internal fun PlaceholderSection(
    module: PlaceholderModule,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    FormSection(title = module.title, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colors.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.surfaceContainerHigh
                ) {
                    Text(
                        text = "即将推出",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
