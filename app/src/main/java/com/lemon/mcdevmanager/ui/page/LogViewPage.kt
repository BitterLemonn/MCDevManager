package com.lemon.mcdevmanager.ui.page

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.TextWhite
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.widget.AppLoadingWidget
import com.lemon.mcdevmanager.ui.widget.HintDoubleSelectDialog
import com.lemon.mcdevmanager.ui.widget.SNACK_WARN
import com.lemon.mcdevmanager.viewModel.LogViewAction
import com.lemon.mcdevmanager.viewModel.LogViewEvent
import com.lemon.mcdevmanager.viewModel.LogViewModel
import com.lt.compose_views.other.VerticalSpace
import com.orhanobut.logger.Logger
import com.zj.mvi.core.observeEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun LogViewPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: LogViewModel = viewModel()
) {
    val states by viewModel.viewState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var isShowDetail by remember { mutableStateOf(false) }
    var isShowDeleteDialog by remember { mutableStateOf(false) }
    var isShowExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.dispatch(LogViewAction.UpdateLogDirPath(AppContext.logDirPath))
        viewModel.viewEvent.observeEvent(lifecycleOwner) { event ->
            when (event) {
                is LogViewEvent.ShowToast -> showToast(event.message, event.type)
                is LogViewEvent.ExportLog -> {
                    val targetPath =
                        Environment.DIRECTORY_DOWNLOADS + File.pathSeparator + "MCDevManager" + File.pathSeparator + "Log"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val resolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.Downloads.DISPLAY_NAME,
                                "MCDevManager" + File.pathSeparator + "Log" + File.pathSeparator + event.filename
                            )
                            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = resolver.insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        uri?.let {
                            try {
                                resolver.openOutputStream(it)?.use { outputStream ->
                                    val file = File(event.filePath)
                                    FileInputStream(file).use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                                viewModel.dispatch(LogViewAction.DownloadOverSingle)
                            } catch (e: IOException) {
                                Logger.e(e, "日志文件导出失败: ${e.message}")
                                viewModel.dispatch(LogViewAction.DownloadFailSingle)
                            }
                        } ?: run {
                            viewModel.dispatch(LogViewAction.DownloadFailSingle)
                            Logger.e("日志文件导出失败: uri is null")
                        }
                    } else {
                        val sourceFile = File(event.filePath)
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(targetPath)
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }
                        val destinationFile = File(downloadsDir, event.filename)

                        try {
                            FileInputStream(sourceFile).use { input ->
                                FileOutputStream(destinationFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            viewModel.dispatch(LogViewAction.DownloadOverSingle)
                        } catch (e: IOException) {
                            Logger.e(e, "日志文件导出失败: ${e.message}")
                            viewModel.dispatch(LogViewAction.DownloadFailSingle)
                        }
                    }
                }
            }
        }
        viewModel.dispatch(LogViewAction.LoadLogList)
    }

    Column(Modifier.fillMaxSize()) {
        HeaderWidget(title = "日志", leftAction = {
            Box(modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    navController.navigateUp()
                }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .then(it),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(TextWhite)
                )
            }
        })
        LazyColumn(
            Modifier.fillMaxWidth()
        ) {
            items(states.logList) {
                LogFileItem(fileName = it,
                    isSelected = states.selectedLogList.contains(it),
                    onSelected = { isSelected ->
                        viewModel.dispatch(LogViewAction.SelectLog(it, isSelected))
                    }) {
                    showToast("日志内容可能包含隐私信息，请谨慎分享", SNACK_WARN)
                    viewModel.dispatch(LogViewAction.LoadLogContent(it))
                    isShowDetail = true
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = AppTheme.colors.dividerColor
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(
            visible = states.selectedLogList.isNotEmpty(),
            enter = slideInVertically(
                animationSpec = tween(200),
                initialOffsetY = { it }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { it }
            ) + fadeOut()
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = AppTheme.colors.dividerColor
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.card)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.error)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple()
                            ) {
                                isShowDeleteDialog = true
                            }
                    ) {
                        Text(
                            text = "删除",
                            fontSize = 14.sp,
                            color = TextWhite,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.primaryColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple()
                            ) { isShowExportDialog = true }
                    ) {
                        Text(
                            text = "导出",
                            fontSize = 14.sp,
                            color = TextWhite,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isShowDeleteDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        HintDoubleSelectDialog(
            hint = "确定删除选中的日志文件吗？",
            onCanceled = { isShowDeleteDialog = false }
        ) {
            viewModel.dispatch(LogViewAction.DeleteLog)
            isShowDeleteDialog = false
        }
    }

    AnimatedVisibility(visible = isShowExportDialog, enter = fadeIn(), exit = fadeOut()) {
        HintDoubleSelectDialog(
            hint = "确定导出选中的${states.selectedLogList.size}个日志文件吗？",
            onCanceled = { isShowExportDialog = false }
        ) {
            viewModel.dispatch(LogViewAction.ExportLog)
            isShowExportDialog = false
        }
    }

    AnimatedVisibility(
        visible = isShowDetail, enter = fadeIn(), exit = fadeOut()
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { isShowDetail = false })
    }

    AnimatedVisibility(
        visible = isShowDetail,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(0.8f), colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.card,
                ), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    Text(
                        text = states.logFileName,
                        fontSize = 14.sp,
                        color = AppTheme.colors.hintColor
                    )
                    VerticalSpace(dp = 8)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = states.logContent,
                            fontSize = 12.sp,
                            color = AppTheme.colors.textColor
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = states.isShowLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        AppLoadingWidget()
    }
}

@Composable
private fun LogFileItem(
    fileName: String, isSelected: Boolean, onSelected: (Boolean) -> Unit, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick,
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fileName,
                color = AppTheme.colors.hintColor,
                modifier = Modifier.padding(8.dp),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier
                .height(50.dp)
                .width(80.dp)
                .padding(8.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    onSelected(!isSelected)
                }) {
                Image(
                    painter = painterResource(
                        id = if (isSelected) R.drawable.ic_selected else R.drawable.ic_unselect
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(AppTheme.colors.primaryColor)
                )
            }
        }
    }
}

@Composable
@Preview
private fun LogFileItemPreview() {
    var isSelected by remember { mutableStateOf(false) }
    MCDevManagerTheme {
        LogFileItem(fileName = "2021-09-01 12:00:00_log.txt",
            isSelected = isSelected,
            onSelected = {
                isSelected = it
            },
            onClick = {})
    }
}

@Composable
@Preview(showBackground = true)
private fun LogViewPagePreview() {
    MCDevManagerTheme {
        Box(modifier = Modifier.background(color = AppTheme.colors.background)) {
            LogViewPage()
        }
    }
}