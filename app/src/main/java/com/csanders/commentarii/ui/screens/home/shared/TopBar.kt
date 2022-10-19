package com.csanders.commentarii.ui.screens.home.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csanders.commentarii.ui.screens.home.HomeScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBarScaffold(Body: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopBar(
                onMenuClicked = {
                    coroutineScope.launch {
                        if (drawerState.isOpen) {
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                }
            )
        },
        content = {
            Box(
                modifier = Modifier.padding(it)
            ) {
                Body()
                Drawer(
                    drawerState,
                    onDrawerClick =  {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onNotDrawerClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                })
                if (!drawerState.isOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .fillMaxHeight()
                            //Replace with a swipeable eventually
                            .clickable {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Drawer(drawerState: DrawerState, onDrawerClick: () -> Unit, onNotDrawerClick:() -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    DismissibleNavigationDrawer(
        modifier = Modifier
            .background(Color.Transparent),
        drawerContent = {
            DrawerContent() {
                coroutineScope.launch {
                    onDrawerClick()
                    //navigate or something?
                }
            }
        },
        drawerState = drawerState
    ) {
        if(drawerState.isOpen){
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)
                    .clickable(onClick = onNotDrawerClick)
            )
        }
    }
}

@Composable
fun DrawerContent(onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        repeat(5) {
            Text(
                text = "Item number $it",
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onItemClick),
                color = MaterialTheme.colorScheme.primary
            )
        }

    }

}

@Preview
@Composable
fun MenuPreview() {
    MenuBarScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(text = "Hello World!")
        }
    }
}