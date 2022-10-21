package com.csanders.commentarii.ui.screens.textreader

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun TextReaderScreen(viewModel: TextReaderViewModel) {

    val textStyle = MaterialTheme.typography.bodyMedium
    //Columns do not recompose on their own
    //Wrap or move all state into a new area.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
//                modifier = Modifier.fillMaxSize(),
                text = viewModel.getText(),
                style = textStyle)
        }
        TurnPageBanner(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .fillMaxHeight()
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
            turnPage = TurnPage.Backward,
            contentDescription = "Button to load previous passage"
        )
        TurnPageButton(
            modifier = Modifier.fillMaxWidth(),
            nextPassageName = "Next Passage",
            turnPage = TurnPage.Forward,
            contentDescription = "Button to load net passage"
        )
    }
}

@Composable
private fun TurnPageButton(
    modifier: Modifier = Modifier,
    onButtonTapped: () -> Unit = {},
    nextPassageName: String,
    turnPage: TurnPage,
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
            .show()},
    ) {
        Row {
            @Composable
            fun ButtonIcon(imageVector: ImageVector) {
                Icon(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    imageVector = imageVector,
                    contentDescription = contentDescription
                )
            }

            @Composable
            fun ButtonText() {
                Text(text = nextPassageName, style = TextStyle(fontSize = 16.sp))
            }

            when (turnPage) {
                TurnPage.Forward -> {
                    ButtonText()
                    ButtonIcon(imageVector = Icons.Default.ArrowForward)
                }
                TurnPage.Backward -> {
                    ButtonIcon(imageVector = Icons.Default.ArrowBack)
                    ButtonText()
                }
            }

        }
    }
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