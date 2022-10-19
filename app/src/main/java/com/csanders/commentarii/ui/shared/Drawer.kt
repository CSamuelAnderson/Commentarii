package com.csanders.commentarii.ui.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    drawerState: DrawerState,
    closeDrawerCallback: () -> Unit,
    onNavigationRequested: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val contextForToast = LocalContext.current.applicationContext

    DismissibleNavigationDrawer(
        modifier = Modifier
            .background(Color.Transparent),
        drawerContent = {
            DrawerContent() {
                coroutineScope.launch {
                    closeDrawerCallback()
                    Toast
                        .makeText(contextForToast, "TODO: Navigate!", Toast.LENGTH_SHORT)
                        .show()
                    onNavigationRequested
                }
            }
        },
        drawerState = drawerState
    ) {
        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)
                    .clickable(onClick = closeDrawerCallback)
            )
        }
    }
}

@Composable
private fun DrawerContent(onItemClick: () -> Unit) {
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
