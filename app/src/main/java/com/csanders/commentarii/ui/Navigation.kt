package com.csanders.commentarii.ui

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.csanders.commentarii.ui.screens.home.HomeScreen
import com.csanders.commentarii.ui.screens.home.HomeViewModel


object Routes {
    const val Home = "home"
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val startRoute = Routes.Home

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Routes.Home) { backStackEntry ->
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
    }

}
