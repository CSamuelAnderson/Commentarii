package com.csanders.commentarii.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBarScaffold(onNavigationRequested: (String) -> Unit, Destination: @Composable () -> Unit) {
    val stateHolder: SharedComponentsStateHolder = rememberSharedComponentsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                onMenuClicked = {
                    stateHolder.onDrawerChange()
                }
            )
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                Destination()
                Drawer(
                    onNavigationRequested = onNavigationRequested,
                    drawerState = stateHolder.drawerState,
                    closeDrawerCallback = {
                        stateHolder.onDrawerShouldClose()
                    })
                if (stateHolder.drawerState.isClosed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .fillMaxHeight()
                            //Replace with a swipeable eventually
                            .clickable(
                                onClick = {
                                    stateHolder.onDrawerChange()
                                },
                                interactionSource = MutableInteractionSource(),
                                indication = null
                            )

                    )
                }
            }

        }
    )
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
        }
    )
}

@Preview
@Composable
fun MenuPreview() {
    MenuBarScaffold(onNavigationRequested = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(text = "Hello World!")
        }
    }
}