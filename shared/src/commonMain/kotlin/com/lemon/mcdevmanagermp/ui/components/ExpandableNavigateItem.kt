package com.lemon.mcdevmanagermp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.fallback
import com.github.panpf.sketch.request.placeholder
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import mcdevmanagermpr.shared.generated.resources.Res
import mcdevmanagermpr.shared.generated.resources.img_avatar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ExpandableNavigateItem(
    title: String,
    titleColor: Color = LocalAppColors.current.onSurface,
    titleWeight: FontWeight = FontWeight.Normal,
    icon: Any?,
    iconModifier: Modifier = Modifier,
    isTinted: Boolean = true,
    expanded: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected && expanded) colors.primary.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected indicator bar
        if (selected) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .width(3.dp)
                    .fillMaxHeight(0.5f)
                    .clip(CircleShape)
                    .background(colors.primary)
            )
        } else {
            Box(modifier = Modifier.width(7.dp))
        }

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (icon is DrawableResource) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = if (isTinted) {
                        if (selected) colors.primary else colors.onSurfaceVariant
                    } else Color.Transparent,
                    modifier = iconModifier.then(Modifier.size(24.dp))
                )
            } else if (icon is String) {
                AsyncImage(
                    uri = icon,
                    state = rememberAsyncImageState(ComposableImageOptions {
                        placeholder(Res.drawable.img_avatar)
                        fallback(Res.drawable.img_avatar)
                        crossfade()
                        error(Res.drawable.img_avatar)
                        sizeMultiplier(2.0f)
                    }),
                    contentDescription = title,
                    modifier = iconModifier
                        .then(Modifier.size(32.dp))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.img_avatar),
                    contentDescription = "avatar"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    color = if (selected) colors.primary else titleColor,
                    maxLines = 1,
                    softWrap = false,
                    fontSize = 14.sp,
                    fontWeight = titleWeight
                )
            }
        }
    }
}
