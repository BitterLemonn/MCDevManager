package com.lemon.mcdevmanager.ui.page

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.viewModel.OneClickPriceFeedbackViewModel

@Composable
fun OneClickPriceFeedbackPage(
    navController: NavController = rememberNavController(),
    showToast: (String, String) -> Unit = { _, _ -> },
    viewModel: OneClickPriceFeedbackViewModel = viewModel()
) {

}