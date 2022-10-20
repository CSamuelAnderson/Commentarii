package com.csanders.commentarii.ui.screens.textreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun TextReaderScreen() {
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            state = lazyListState
        ) {

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f),
        ) {

        }
    }
}

@Composable
private fun ChangeTextButton(
    modifier: Modifier = Modifier,
    onButtonTapped: () -> Unit = {},
    nextPassageName: String,
    turnPage: TurnPage,
    contentDescription: String
) {
    TextButton(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .fillMaxSize(),
        onClick = onButtonTapped,
    ) {
        Row {
            @Composable
            fun ButtonIcon(imageVector: ImageVector) {
                Icon(
                    modifier = Modifier.padding(horizontal = 4.dp),
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

@Preview
@Composable
fun PreviewChangeTextButton() {
    Row(
        modifier = Modifier
            .width(1000.dp)
            .height(80.dp)
    ) {
        ChangeTextButton(
            modifier = Modifier.fillMaxWidth(0.4f),
            nextPassageName = "Next Passage",
            turnPage = TurnPage.Forward,
            contentDescription = "Button to load net passage"
        )
        Box(modifier = Modifier.fillMaxWidth(0.2f))
        ChangeTextButton(
            modifier = Modifier.fillMaxWidth(0.4f),
            nextPassageName = "Previous Passage",
            turnPage = TurnPage.Backward,
            contentDescription = "Button to load previous passage"
        )
    }
}

//@Preview
//@Composable
//fun PreviewTextReader() {
//
//}