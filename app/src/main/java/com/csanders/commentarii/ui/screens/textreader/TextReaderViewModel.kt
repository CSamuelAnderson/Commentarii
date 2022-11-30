package com.csanders.commentarii.ui.screens.textreader

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.*
import com.csanders.commentarii.utilities.TEIBookParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    fun convertBookFromXml(context: Context): Book {
        val parser = TEIBookParser()
//        return parser.getBookFromResource(R.raw.plato_symposium_grc)
        return parser.getBookFromResource(resourceID = R.raw.plato_symposium_eng, context = context)
    }

    //Todo: Eventually we'll want getText running in the background or something.
    @Composable
    fun displayPage(page: Page): AnnotatedString {
        return page.passages.map { passage ->
            AnnotatedString(
                text = passage.text,
                spanStyle = passage.styling.toSpanStyle()
            )
        }.fold(buildAnnotatedString{}) { acc, string ->
            acc.plus(string)
        }
    }

    @Composable
    fun displayTitle(book: Book): AnnotatedString {
        return buildAnnotatedString {
            pushStyle(SpanStyle(fontSize = 24.sp))
            append(book.header.title.value)
            pop()
            append("\n")
            append(book.header.author.value)
            append("\n")
            append(book.header.languagesUsed.joinToString(", ") { it.value })
        }
    }


    //todo: this is creating a new list every time, which is inefficient.
    //  Since we don't want to mix mutable state and mutable list, we should make the Joy of Kotlin linked list or borrow from Arrow
    //  We'll also want to move pages to an ID so we don't have to create a new work every time either
    fun turnPageForward(book: Book): Either<IllegalStateException, Book> {
        return when (book.pages.futurePages.isEmpty()) {
            true -> Either.Left(IllegalStateException("End of book"))
            false -> {
                val newPages = Pages(
                    -1,
                    book.pages.futurePages.last(),
                    book.pages.previousPages + book.pages.openedPage,
                    book.pages.futurePages.dropLast(1)
                )
                Either.Right(Book(-1, newPages, book.header))
            }
        }
    }

    fun turnPageBackward(book: Book): Either<IllegalStateException, Book> {
        return when (book.pages.previousPages.isEmpty()) {
            true -> Either.Left(IllegalStateException("Start of book"))
            false -> {
                val newPages = Pages(
                    -1,
                    book.pages.previousPages.last(),
                    book.pages.previousPages.dropLast(1),
                    book.pages.futurePages + book.pages.openedPage
                )
                Either.Right(Book(-1, newPages, book.header))
            }
        }
    }

    companion object {
        const val route = "text_reader"
    }
}