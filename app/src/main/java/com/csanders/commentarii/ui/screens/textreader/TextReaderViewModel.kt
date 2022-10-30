package com.csanders.commentarii.ui.screens.textreader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.csanders.commentarii.R
import com.csanders.commentarii.utilities.TEIWorkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    //Todo: Eventually we'll want getText running in the background or something.
    @Composable
    fun getText(): AnnotatedString {
        val parser = TEIWorkParser()
        val work = parser.getWorkFromResource(R.raw.apuleius_golden_ass_lat).last()
        return buildAnnotatedString {
            pushStyle(SpanStyle(fontSize = 24.sp))
            append(work.header.title)
            pop()
            append("\n")
            append(work.header.author)
            append("\n")
            append(work.header.languagesUsed.first())
            toAnnotatedString()
        }
    }

    companion object {
        const val route = "text_reader"
    }
}