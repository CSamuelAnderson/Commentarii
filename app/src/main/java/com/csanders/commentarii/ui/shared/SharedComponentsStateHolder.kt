package com.csanders.commentarii.ui.shared

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharedComponentsStateHolder @OptIn(ExperimentalMaterial3Api::class) constructor(
    val drawerState: DrawerState,
    //this one might need to be saveable?
    val navHostController: NavHostController,
    private val coroutineScope: CoroutineScope
) {

    @OptIn(ExperimentalMaterial3Api::class)
    fun onDrawerShouldClose() {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onDrawerChange() {
        coroutineScope.launch {
            if(drawerState.isOpen) {
                drawerState.close()
            }
            else drawerState.open()
        }
    }

    fun onNavigationRequested(route: String) {
        navHostController.navigate(route)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSharedComponentsState(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    navHostController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),

) = remember(drawerState, navHostController, coroutineScope) {
    SharedComponentsStateHolder(drawerState, navHostController, coroutineScope)
}