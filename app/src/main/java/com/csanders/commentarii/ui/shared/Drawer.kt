package com.csanders.commentarii.ui.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    closeDrawerCallback: () -> Unit,
    onNavigationRequested: (String) -> Unit,
    Content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val contextForToast = LocalContext.current.applicationContext

    ModalNavigationDrawer(
        modifier = modifier
            .background(Color.Transparent),
        drawerContent = {
            DrawerContent() {
                coroutineScope.launch {
                    closeDrawerCallback()
                    Toast
                        .makeText(contextForToast, "TODO: Navigate!", Toast.LENGTH_SHORT)
                        .show()
                    onNavigationRequested("text_reader")
                }
            }
        },
        drawerState = drawerState
    ) {
        Content()
        //Layout for the part of the screen not covered by the drawer
        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.4f)
                    .clickable(
                        onClick = closeDrawerCallback,
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(onItemClick: () -> Unit) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
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
fun PreviewDrawerSheet() {
    DrawerContent {

    }
}
