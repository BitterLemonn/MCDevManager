package com.lemon.mcdevmanager.ui.fragPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.viewModel.AnalyzeViewModel

@Composable
fun OverviewFragPage(
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: AnalyzeViewModel
) {

}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun OverviewFragPagePreview() {
    MCDevManagerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        ) {
            OverviewFragPage(viewModel = viewModel())
        }
    }
}