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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import arrow.core.Either
import com.csanders.commentarii.datamodel.Book
import com.csanders.commentarii.datamodel.ComposeUIElement


@Composable
fun PageReaderScreen(viewModel: TextReaderViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var book by remember { mutableStateOf(viewModel.convertBookFromXml(context)) }

    val contextForToast = context.applicationContext
    val scrollState = rememberScrollState()

    fun onPageTurnRequested(turnPage: (Book) -> Either<IllegalStateException, Book>) {
        when (val updatedBook = turnPage(book)) {
            is Either.Left -> Toast.makeText(
                contextForToast,
                updatedBook.value.message,
                Toast.LENGTH_SHORT
            )
                .show()
            is Either.Right -> {
                book = updatedBook.value
            }
        }
    }

    fun onNextPageRequested() {
        onPageTurnRequested { viewModel.turnPageForward(it) }
    }

    fun onPreviousPageRequested() {
        onPageTurnRequested { viewModel.turnPageBackward(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SelectionContainer(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(scrollState, Orientation.Vertical)
        ) {
            //Todo: lazy columns can have bad performance because they must trigger a recompose one level higher than themselves. If you wrap them it should go away
            LazyColumn {
                //Todo: Make Display page return a list of things we can use as items. That way we don't have to load everything all at once.
                item {
                    Text(
                        text = viewModel.displayTitle(book),
                    )
                }
                item {
                    Text(
                        text = viewModel.displayPage(page = book.pages.openedPage),
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

        }
        val buttons = getTurnPageButtons(book)(::onPreviousPageRequested)(::onNextPageRequested)
        ButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .align(Alignment.BottomCenter),
            buttons = buttons
        )
    }
}

@Composable
private fun ButtonRow(
    modifier: Modifier = Modifier,
    buttons: List<ComposeUIElement>
) {
    if (buttons.isNotEmpty()) {
        Row(
            modifier = modifier.background(Color.Transparent)
        ) {
            buttons.forEach { it() }
        }
    }
}

private fun getTurnPageButtons(book: Book): (backButtonTapped: () -> Unit) -> (nextButtonTapped: () -> Unit) -> List<ComposeUIElement> {
    return { backButtonTapped ->
        { nextButtonTapped ->
            val buttons = mutableListOf<ComposeUIElement>()
            if (book.pages.previousPages.isNotEmpty()) {
                buttons.add {
                    TurnPageButton(
                        //TODO: This is a bad place to manage the width formatting. Can we move it to the ButtonRow?
                        modifier = Modifier.fillMaxWidth(0.5f),
                        nextPassageName = "Previous Page",
                        iconImageVector = Icons.Default.ArrowBack,
                        contentDescription = "Button to load previous page",
                        onButtonTapped = backButtonTapped
                    )
                }
            }
            if (book.pages.futurePages.isNotEmpty()) {
                buttons.add {
                    TurnPageButton(
                        modifier = Modifier.fillMaxWidth(),
                        nextPassageName = "Next page",
                        iconImageVector = Icons.Default.ArrowForward,
                        contentDescription = "Button to load next page",
                        onButtonTapped = nextButtonTapped
                    )
                }
            }
            buttons.toList()
        }
    }
}

@Composable
private fun TurnPageButton(
    modifier: Modifier = Modifier,
    onButtonTapped: () -> Unit,
    nextPassageName: String,
    iconImageVector: ImageVector,
    contentDescription: String
) {
    ExtendedFloatingActionButton(
        modifier = modifier
            .background(Color.Transparent)
            .fillMaxHeight()
            .fillMaxWidth(0.3f),
        onClick = {
            onButtonTapped()
        },
        text = { Text(text = nextPassageName, style = TextStyle(fontSize = 16.sp)) },
        icon = { Icon(imageVector = iconImageVector, contentDescription = contentDescription) }
    )
}

@Preview
@Composable
fun PreviewTextReader() {
    PageReaderScreen(viewModel = hiltViewModel())
}