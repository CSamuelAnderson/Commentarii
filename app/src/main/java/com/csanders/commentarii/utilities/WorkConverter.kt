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

fun convertToWork(parsedXml: ParsedXml): Work {
    if (parsedXml.tag != TEIElement.TEI.element) {
        /**
         * We'll eventually want to wrap this in a Result or something, so we can handle bad cases like this.
         */
        return Work(WorkHeader(), listOf(Section2()))
    }

    val header = getHeader(parsedXml)
    val bodyTag = parsedXml
        .findTag(TEIElement.Text.element)
        .findTag(TEIElement.TextBody.element)
//    val body = convertToStupidSection(bodyTag)
    val body = convertToOldSection(parsedXml = bodyTag)
    return Work(header, body)
}

/**
 * Basic, one use-case function to help with the header
 */
private fun getHeader(parsedXml: ParsedXml): WorkHeader {
    val titleStatementTag = parsedXml
        .findTag(TEIElement.TeiHeader.element)
        .findTag(TEIHeader.FileDescription.element)
        .findTag(TEIHeader.TitleStatement.element)

    val languagesUsedTag = parsedXml
        .findTag(TEIElement.TeiHeader.element)
        .findTag(TEIHeader.ProfileDescription.element)
        .findTag(TEIHeader.LanguagesUsed.element)

    val author = titleStatementTag
        .findTag(TEIHeader.Author.element)
        .getFirstText()

    val title = titleStatementTag
        .findTag(TEIHeader.Title.element)
        .getFirstText()

    val languages = languagesUsedTag.subXml.fold(listOf<String>()) { acc, languageTag ->
        when (val language = languageTag.getFirstText()) {
            null -> acc
            else -> acc + language
        }
    }

    return WorkHeader(
        author = author ?: "Unknown Author",
        title = title ?: "Unknown Work",
        languagesUsed = languages
    )
}

private fun ParsedXml.convertToChapters(): Chapters {

    fun ParsedXml.convertSubXmlToChapters(
        pages: List<Chapter> = listOf(),
        accPage: Chapter
    ): List<Chapter> {
        val addSection = addSection(Typography.bodyMedium) //Partially applied based on the current section's idk what I'm doing I'm fiddling around lol
        return this.subXml.flatMap { child ->
            when (child.shouldPageBreak()) {
                true -> {
                    convertSubXmlToChapters(
                        pages + accPage,
                        Chapter(child.getChapterHeading(), listOf())
                    )
                }
                false -> {
                    convertSubXmlToChapters(pages, addSection(accPage)(child.text))
                }
            }
        }
    }

    val allChapters =
        this.convertSubXmlToChapters(accPage = Chapter(this.getChapterHeading(), listOf()))
    return Chapters(
        openedChapter = allChapters.first(),
        previousChapters = listOf(),
        futureChapters = allChapters.drop(1)
    )
}

private fun addSection(styling: TextStyle): (Chapter) -> (String) -> Chapter {
    return { chapter ->
        { sectionString ->
            Chapter(chapter.chapterHeading, chapter.Texts + Passage(sectionString, styling))
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