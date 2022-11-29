package com.csanders.commentarii.utilities

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.csanders.commentarii.datamodel.*
import com.csanders.commentarii.ui.theme.Typography

/**
 * Converts ParsedXml into a Work data class.
 */

fun convertToBook(parsedXml: ParsedXml): Book {
    if (parsedXml.tag != TEIElement.TEI.element) {
        /**
         * We'll eventually want to wrap this in a Result or something, so we can handle bad cases like this.
         */
//        return EmptyBook()
    }
    val header = parsedXml.convertToHeader()

    val chapters = parsedXml
        .findTag(TEIElement.Text.element)
        .findTag(TEIElement.TextBody.element)
        .convertToChapters()

//    val body = convertToStupidSection(bodyTag)
//    return Work(header, body)
    return Book(chapters = chapters, header = header)
}

/**
 * Basic, one use-case function to help with the header
 */
private fun ParsedXml.convertToHeader(): Header {
    val titleStatementTag = this
        .findTag(TEIElement.TeiHeader.element)
        .findTag(TEIHeader.FileDescription.element)
        .findTag(TEIHeader.TitleStatement.element)

    val languagesUsedTag = this
        .findTag(TEIElement.TeiHeader.element)
        .findTag(TEIHeader.ProfileDescription.element)
        .findTag(TEIHeader.LanguagesUsed.element)

    val author = titleStatementTag
        .findTag(TEIHeader.Author.element)
        .getFirstText()

    val title = titleStatementTag
        .findTag(TEIHeader.Title.element)
        .getFirstText()

    val languages = languagesUsedTag.subXml.fold(listOf<Language>()) { acc, languageTag ->
        when (val language = languageTag.getFirstText()) {
            null -> acc
            else -> acc + Language(language)
        }
    }

    return Header(Title(title ?: "Unknown Author"), Author(author ?: "Unknown Work"), languages)
}

//Todo: This is complicated enough to need documentation
//  also, one result here that isn't easy to see is that ParsedXml with no text will still add a new passage.
//  This will probably be handled better when we change up the type system for ParsedXml, but it's unintuitive and unnecessary.
private fun ParsedXml.convertToChapters(): Chapters {

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
                val parsedXml = stackOfXml.removeLast()
                stackOfXml.addAll(parsedXml.subXml.reversed())

                //Todo: This can be handled by the type system
                when (parsedXml.shouldPageBreak()) {
                    true ->
                        convertSubXmlToChapters(
                            stackOfXml,
                            chapters + accChapter,
                            Chapter(parsedXml.getChapterHeading(), listOf())
                        )
                    false ->
                        convertSubXmlToChapters(
                            stackOfXml,
                            chapters,
                            addSectionWithMediumBody(accChapter)(parsedXml.text)
                        )
                }
            }
        }
    }

    val allChapters =
        convertSubXmlToChapters(
            stackOfXml = this.subXml.reversed().toMutableList(),
            accChapter = Chapter(this.getChapterHeading(), listOf())
        )
    return Chapters(
        openedChapter = allChapters.first(),
        previousChapters = listOf(),
        futureChapters = allChapters.drop(1)
    )
}

private fun addPassage(styling: TextStyle): (Chapter) -> (String) -> Chapter {
    return { chapter ->
        { sectionString ->
            Chapter(chapter.chapterHeading, chapter.passages + Passage(sectionString, styling))
        }
    }
}

private fun ParsedXml.getChapterHeading(): ChapterHeading {
    val attributeHeader = this.attributes.getOrDefault(
        TEIAttributes.Type.attribute, ""
    ) + " " + this.attributes.getOrDefault(
        TEIAttributes.Subtype.attribute, ""
    ) + " " + this.attributes.getOrDefault(TEIAttributes.ReferenceNumber.attribute, "")
    return when (attributeHeader.isBlank()) {
        true -> ChapterHeading("New Chapter!")
        false -> ChapterHeading(attributeHeader)
    }
}


private fun ParsedXml.shouldPageBreak(): Boolean {
    return this.tag == TEIElement.Div.element
}

//TODO: We'll need to create a refreshSectionAnnotations to manage a change in font, footnote?.
private fun convertToOldSection(
    parentAnnotation: AnnotatedString = buildAnnotatedString {},
    parsedXml: ParsedXml
): List<Section2> {

    val topLevelAnnotation: AnnotatedString = parentAnnotation + buildTopLevelAnnotation(parsedXml)
    val inheritedAnnotation: AnnotatedString =
        parentAnnotation + buildInheritedAnnotation(parsedXml)

    return listOf(
        Section2(
            topLevelAnnotation,
            isMajorSection(parsedXml)
        )
    ) + parsedXml.subXml.flatMap { child ->
        convertToOldSection(inheritedAnnotation, child)
    }
}

private fun isMajorSection(parsedXml: ParsedXml): Boolean {
    return parsedXml.tag == TEIElement.Div.element
}


private fun buildTopLevelAnnotation(parsedXml: ParsedXml): AnnotatedString {
    return buildAnnotatedString {
        if (parsedXml.tag == TEIElement.Div.element) {
            pushStyle(SpanStyle(fontSize = 24.sp))
            val attributeHeader = parsedXml.attributes.getOrDefault(
                TEIAttributes.Type.attribute,
                ""
            ) + " " + parsedXml.attributes.getOrDefault(
                TEIAttributes.Subtype.attribute,
                ""
            ) + " " + parsedXml.attributes.getOrDefault(TEIAttributes.ReferenceNumber.attribute, "")
            val chapterHeading = when (attributeHeader.isEmpty()) {
                true -> "New chapter!"
                false -> attributeHeader
            } + "\n"
            append(chapterHeading)
        }
        if (parsedXml.text.isNotBlank()) {
            append(parsedXml.text)
        }
    }
}

private fun buildInheritedAnnotation(parsedXml: ParsedXml): AnnotatedString {
    return buildAnnotatedString { }
}