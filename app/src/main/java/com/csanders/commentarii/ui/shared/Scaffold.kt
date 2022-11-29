package com.csanders.commentarii.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBarScaffold() {
    val stateHolder: SharedComponentsStateHolder = rememberSharedComponentsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                onMenuClicked = {
                    stateHolder.onDrawerChange()
                }
            )
        })
    {


        Drawer(
            modifier = Modifier.padding(it),
            onNavigationRequested = stateHolder::onNavigationRequested,
            drawerState = stateHolder.drawerState,
            closeDrawerCallback = stateHolder::onDrawerShouldClose
        ) {

            CmtiiNavHost(
                navController = stateHolder.navHostController,
                onNavigationRequested = stateHolder::onNavigationRequested
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onMenuClicked: () -> Unit) {

    TopAppBar(
        title = {
            Text(text = "Carl's app", color = MaterialTheme.colorScheme.primary)
        },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier.clickable(onClick = onMenuClicked),
                tint = MaterialTheme.colorScheme.primary
            )
        },
    )
}

@Preview
@Composable
fun MenuPreview() {
    MenuBarScaffold()
}