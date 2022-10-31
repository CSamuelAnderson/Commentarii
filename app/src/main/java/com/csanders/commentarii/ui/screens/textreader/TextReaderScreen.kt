package com.csanders.commentarii.ui.screens.textreader

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun TextReaderScreen(viewModel: TextReaderViewModel = hiltViewModel()) {
    val scrollState = rememberScrollState()
    //Columns do not recompose on their own
    //Wrap or move all state into a new area.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SelectionContainer(
            modifier = Modifier
//                .fillMaxWidth()
                .fillMaxSize().scrollable(scrollState, Orientation.Vertical)
        ) {
            //Todo: lazy columns can have bad performance
            LazyColumn() {
                item {
                    Text(
//                modifier = Modifier.fillMaxSize(),
                        text = viewModel.getText(),
                    )
                }
            }

        }
        TurnPageBanner(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TurnPageBanner(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Transparent)
    ) {
        TurnPageButton(
            modifier = Modifier.fillMaxWidth(0.5f),
            nextPassageName = "Previous Passage",
            iconImageVector = Icons.Default.ArrowBack,
            contentDescription = "Button to load previous passage"
        )
        TurnPageButton(
            modifier = Modifier.fillMaxWidth(),
            nextPassageName = "Next Passage",
            iconImageVector = Icons.Default.ArrowForward,
            contentDescription = "Button to load net passage"
        )
    }
}

@Composable
private fun TurnPageButton(
    modifier: Modifier = Modifier,
    onButtonTapped: () -> Unit = {},
    nextPassageName: String,
    iconImageVector: ImageVector,
    contentDescription: String
) {
    val contextForToast = LocalContext.current.applicationContext
    ExtendedFloatingActionButton(
        modifier = modifier
            .background(Color.Transparent)
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        onClick = {
            onButtonTapped
            Toast.makeText(contextForToast, "TODO: Navigate!", Toast.LENGTH_SHORT)
                .show()
        },
        text = { Text(text = nextPassageName, style = TextStyle(fontSize = 16.sp)) },
        icon = { Icon(imageVector = iconImageVector, contentDescription = contentDescription) }
    )
}

private enum class TurnPage {
    Forward, Backward
}

//@Preview
//@Composable
//fun PreviewChangeTextButton() {
//  Turn
//}

@Preview
@Composable
fun PreviewTextReader() {
    TextReaderScreen(viewModel = hiltViewModel<TextReaderViewModel>())
}