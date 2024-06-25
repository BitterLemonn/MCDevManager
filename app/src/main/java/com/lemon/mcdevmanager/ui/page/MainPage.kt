package com.lemon.mcdevmanager.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lemon.mcdevmanager.ui.widget.HeaderWidget
import com.lemon.mcdevmanager.viewModel.MainViewModel

@Composable
fun MainPage(
    navController: NavController,
    viewModel: MainViewModel = viewModel(),
    showToast: (String, String) -> Unit = { _, _ -> },
) {

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainPagePreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    MainPage(navController)
}