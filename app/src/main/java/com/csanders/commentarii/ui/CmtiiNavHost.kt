package com.csanders.commentarii.ui

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
    modifier: Modifier = Modifier
) {
    val startDestination = HomeScreen.route

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(HomeScreen.route) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
    }

}
