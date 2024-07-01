package com.lemon.mcdevmanager.ui.widget

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.lemon.mcdevmanager.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val SNACK_INFO = "确定"
const val SNACK_WARN = " "
const val SNACK_ERROR = "  "
const val SNACK_SUCCESS = "OK"

@Composable
fun AppSnackbar(
    data: SnackbarData
) {
    Snackbar(
        snackbarData = data,
        containerColor = when (data.visuals.actionLabel) {
            SNACK_INFO -> AppTheme.colors.info
            SNACK_WARN -> AppTheme.colors.warn
            SNACK_ERROR -> AppTheme.colors.error
            SNACK_SUCCESS -> AppTheme.colors.success
            else -> AppTheme.colors.info
        }
    )
}


fun popupSnackBar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    label: String,
    message: String,
    onDismissCallback: () -> Unit = {}
) {
    scope.launch {
        snackbarHostState.showSnackbar(
            actionLabel = label,
            message = message,
            duration = SnackbarDuration.Short
        )
        onDismissCallback.invoke()
    }
}