package com.csanders.commentarii.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.csanders.commentarii.ui.screens.home.HomeScreen
import com.csanders.commentarii.ui.screens.home.HomeViewModel

@Composable
fun CmtiiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onNavigationRequested: (String) -> Unit
) {
    val startDestination = HomeViewModel.route

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(HomeViewModel.route) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(viewModel = homeViewModel, onNavigationRequested)
        }
    }
}
