package com.csanders.commentarii.utilities

import android.util.Log
import androidx.compose.ui.text.TextStyle
import arrow.core.Either
import arrow.core.flatMap
import com.csanders.commentarii.datamodel.*
import com.csanders.commentarii.datamodel.Author
import com.csanders.commentarii.datamodel.Language
import com.csanders.commentarii.datamodel.MetadataAttribute.ReferenceNumber
import com.csanders.commentarii.datamodel.MetadataTag.*
import com.csanders.commentarii.datamodel.TextTag.TeiText
import com.csanders.commentarii.datamodel.TextTag.TextBody
import com.csanders.commentarii.datamodel.Title
import com.csanders.commentarii.ui.theme.Typography

/**
 * Converts ParsedXml into a Book data class.
 */
//Todo: Create a tag for error logging
//Todo: Separate the error strings/etc. into internationalized strings

typealias bookConversion = (ParsedXml) -> Book

fun convertToBook(parsedXml: ParsedXml.Tag): Book {
    if (parsedXml.tag != StartingTag.TeiBook) {
        /**
         * We'll eventually want to wrap this in a Result or something, so we can handle bad cases like this.
         */
        Log.d(null,NotTeiError.message)
        //Do something instead of returning null.
    }
    val header = parsedXml.convertToHeader()

    val startOfText = parsedXml.findTag(TeiText)
        .flatMap { it.findTag(TextBody) }

    val chapters =
        when (startOfText) {
            is Either.Right -> startOfText.value.convertToChapters()
            is Either.Left -> {
                Log.e(null, startOfText.value.message)
                emptyChapter()
            }
        }

    return Book(pages = chapters, header = header)
}

private fun emptyChapter(): Pages {
    return Pages(
        openedPage = Chapter(ChapterHeading(""), listOf()),
        previousPages = listOf(),
        futurePages = listOf()
    )
}


/**
 * Basic, one use-case function to help with the header
 */
private fun ParsedXml.Tag.convertToHeader(): Header {
    val titleStatementTag = this
        .findTag(TeiHeader)
        .flatMap { it.findTag(FileDescription) }
        .flatMap { it.findTag(TitleStatement) }

    val maybeLanguages = this
        .findTag(TeiHeader)
        .flatMap { it.findTag(ProfileDescription) }
        .flatMap { it.findTag(LanguagesUsed) }
        .map {
            it.subXml.filterIsInstance<ParsedXml.Tag>()
                .map { languageTag -> languageTag.getFirstText() }
        }

    val author =
        when (val maybeAuthor = titleStatementTag
            .flatMap { it.findTag(MetadataTag.Author) }
            .flatMap { it.getFirstText() }) {
            is Either.Right -> maybeAuthor.value.text
            is Either.Left -> {
                Log.e(null, maybeAuthor.value.message)
                "Unknown author"
            }
        }

    val title =
        when (val maybeTitle =
            titleStatementTag
                .flatMap { it.findTag(MetadataTag.Title) }
                .flatMap { it.getFirstText() }) {
            is Either.Right -> maybeTitle.value.text
            is Either.Left -> {
                Log.e(null, maybeTitle.value.message)
                "Unknown title"
            }
        }


    val languages =
        when (maybeLanguages) {
            is Either.Right -> {
                maybeLanguages.value.map {
                    when (it) {
                        is Either.Right -> it.value.text
                        is Either.Left -> {
                            Log.e(null, it.value.message)
                            "Unknown Language"
                        }
                    }
                }
            }
            is Either.Left -> {
                Log.e(null, maybeLanguages.value.message)
                listOf("Unknown Languages")
            }
        }

    return Header(
        Title(title),
        Author(author),
        languages.map { Language(it) }
    )
}

//Todo: This is complicated enough to need documentation
private fun ParsedXml.Tag.convertToChapters(): Pages {

    val addSectionWithMediumBody =
        addPassage(Typography.bodyMedium) //Playing around with partial applications, just right now we're just being fancy

    tailrec fun convertSubXmlToChapters(
        stackOfXml: MutableList<ParsedXml>,
        chapters: List<Chapter> = listOf(),
        accChapter: Chapter
    ): List<Chapter> {
        return when (stackOfXml.isEmpty()) {
            true -> chapters + accChapter
            false -> {
                when (val parsedXml = stackOfXml.removeLast()) {
                    is ParsedXml.Tag -> {
                        stackOfXml.addAll(parsedXml.subXml.reversed())

                        //Todo: we should probably make page break a core piece of the type.
                        when (parsedXml.tag) {
                            is DivisionTag -> convertSubXmlToChapters(
                                stackOfXml,
                                chapters + accChapter,
                                Chapter(parsedXml.getChapterHeading(), listOf())
                            )
                            else -> {
                                convertSubXmlToChapters(
                                    stackOfXml,
                                    chapters,
                                    accChapter
                                )
                            }
                        }
                    }
                    is ParsedXml.Text -> {
                        convertSubXmlToChapters(
                            stackOfXml,
                            chapters,
                            addSectionWithMediumBody(accChapter)(parsedXml.text)
                        )
                    }
                }
            }
        }
    }

    val allChapters =
        convertSubXmlToChapters(
            stackOfXml = this.subXml.reversed().toMutableList(),
            accChapter = Chapter(this.getChapterHeading(), listOf())
        )
    return Pages(
        openedPage = allChapters.first(),
        previousPages = listOf(),
        futurePages = allChapters.drop(1).reversed()
    )
}

private fun addPassage(styling: TextStyle): (Chapter) -> (String) -> Chapter {
    return { chapter ->
        { sectionString ->
            Chapter(chapter.chapterHeading, chapter.passages + Passage(sectionString, styling))
        }
    }
}

private fun ParsedXml.Tag.getChapterHeading(): ChapterHeading {
    val attributeHeader = this.attributes.getOrDefault(
        TeiAttribute.Type, ""
    ) + " " + this.attributes.getOrDefault(
        TeiAttribute.Subtype, ""
    ) + " " + this.attributes.getOrDefault(ReferenceNumber, "")
    return when (attributeHeader.isBlank()) {
        true -> ChapterHeading("New Chapter!")
        false -> ChapterHeading(attributeHeader)
    }
}