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
import com.csanders.commentarii.datamodel.Section
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.datamodel.WorkHeader


@Composable
fun TextReaderScreen(viewModel: TextReaderViewModel = hiltViewModel()) {
    val scrollState = rememberScrollState()

    //Todo: Obviously we'll want to put state in the view model or in a StateHolder. For now we'll just set it here.
    var work by remember { mutableStateOf(Work(WorkHeader(), Section())) }
    work = viewModel.getWork()
    val toc by remember { mutableStateOf(viewModel.getTOC(work)) }

    var forwardPages by remember { mutableStateOf(toc.reversed()) }
    var backPages by remember { mutableStateOf(listOf<Section>()) }

    var currentSection by remember { mutableStateOf(forwardPages.last()) }

    val contextForToast = LocalContext.current.applicationContext

    //Todo: obviously this could use some refactoring
    fun onPreviousSectionRequested() {
        when (backPages.isEmpty()) {
            true -> Toast.makeText(
                contextForToast,
                "all out of pages!",
                Toast.LENGTH_SHORT
            )
                .show()
            false -> {
                //todo: this is creating a new list every time, which is inefficient.
                //  Since we don't want to mix mutable state and mutable list, we should make the Joy of Kotlin linked list or borrow from Arrow
                forwardPages += currentSection
                currentSection = backPages.last()
                backPages = backPages.dropLast(1)
            }
        }
    }

    fun onNextSectionRequested() {
        when (forwardPages.isEmpty()) {
            true -> Toast.makeText(
                contextForToast,
                "all out of pages!",
                Toast.LENGTH_SHORT
            )
                .show()
            false -> {
                backPages += currentSection
                currentSection = forwardPages.last()
                forwardPages = forwardPages.dropLast(1)
            }
        }

    }

    //Columns do not recompose on their own
    //Wrap or move all state into a new area.
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
            //Todo: lazy columns can have bad performance
            LazyColumn() {
                item {
                    Text(
                        text = viewModel.displayTitle(work),
                    )
                }
                item {
                    Text(
                        text = viewModel.displaySection(currentSection),
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

        }
        TurnPageBanner(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .align(Alignment.BottomCenter),
            leftButtonTapped = ::onPreviousSectionRequested,
            rightButtonTapped = ::onNextSectionRequested
        )
    }
}

@Composable
private fun TurnPageBanner(
    modifier: Modifier = Modifier,
    leftButtonTapped: () -> Unit,
    rightButtonTapped: () -> Unit
) {
    Row(
        modifier = modifier
            .background(Color.Transparent)
    ) {
        TurnPageButton(
            modifier = Modifier.fillMaxWidth(0.5f),
            nextPassageName = "Previous Passage",
            iconImageVector = Icons.Default.ArrowBack,
            contentDescription = "Button to load previous passage",
            onButtonTapped = leftButtonTapped
        )
        TurnPageButton(
            modifier = Modifier.fillMaxWidth(),
            nextPassageName = "Next Passage",
            iconImageVector = Icons.Default.ArrowForward,
            contentDescription = "Button to load next passage",
            onButtonTapped = rightButtonTapped
        )
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
    TextReaderScreen(viewModel = hiltViewModel<TextReaderViewModel>())
}