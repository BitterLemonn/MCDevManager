package com.lemon.mcdevmanagermp.ui.pages.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainPage() {
    val viewModel = remember { MainViewModel() }
    val state by viewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.dispatch(MainAction.SelectTab(tab)) },
                        icon = { Text(tab.icon) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (state.selectedTab) {
                MainTab.Home -> HomeTabContent()
                MainTab.Profile -> ProfileTabContent()
                MainTab.Settings -> SettingsTabContent()
            }
        }
    }
}

@Composable
private fun HomeTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("首页", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun ProfileTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("我的", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun SettingsTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)
    }
}
