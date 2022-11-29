package com.csanders.commentarii.ui.screens.textreader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.Book
import com.csanders.commentarii.datamodel.Chapter
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.utilities.TEIWorkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    @Composable
    fun getBook(): Book {
        val parser = TEIWorkParser()
        return parser.getBookFromResource(R.raw.plato_symposium_grc)
    }

    //Todo: Eventually we'll want getText running in the background or something.
    @Composable
    fun displaySections(chapter: Chapter): AnnotatedString {
        return buildAnnotatedString {
            append("asdf")
        }
    }

    @Composable
    fun displayTitle(work: Work): AnnotatedString {
        return buildAnnotatedString {
            pushStyle(SpanStyle(fontSize = 24.sp))
            append(work.header.title)
            pop()
            append("\n")
            append(work.header.author)
            append("\n")
            append(work.header.languagesUsed.joinToString(", "))
        }
    }

    //Todo: we'll want to make Lists of sections their own class to improve readability and to avoid having to do this weird logic here
//    fun getTOC(book: Book): List<Section2> {
//        return book.text.fold(listOf(Section2())) { acc, section2 ->
//            when (section2.isStartOfMajorSection) {
//                true -> acc + section2
//                false -> {
//                    val changedString = acc.last().printedString + section2.printedString
//                    val changedSection = Section2(changedString, acc.last().isStartOfMajorSection)
//                    acc.dropLast(1)  + changedSection
//                }
//            }
//        }
//
//    }

    companion object {
        const val route = "text_reader"
    }
}