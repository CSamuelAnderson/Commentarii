package com.csanders.commentarii.utilities

import androidx.compose.ui.text.TextStyle
import com.csanders.commentarii.datamodel.*
import com.csanders.commentarii.ui.theme.Typography

/**
 * Converts ParsedXml into a Work data class.
 */

fun convertToBook(parsedXmlTag: ParsedXml.Tag): Book {
    if (parsedXmlTag.tag != TEIElement.TEI.element) {
        /**
         * We'll eventually want to wrap this in a Result or something, so we can handle bad cases like this.
         */
//        return EmptyBook()
    }
    val header = parsedXmlTag.convertToHeader()

    val chapters = parsedXmlTag
        .findTag(TEIElement.Text.element)
        .findTag(TEIElement.TextBody.element)
        .convertToChapters()

    return Book(pages = chapters, header = header)
}

/**
 * Basic, one use-case function to help with the header
 */
private fun ParsedXml.Tag.convertToHeader(): Header {
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

    val languages = languagesUsedTag.subXml.filterIsInstance<ParsedXml.Tag>()
        .fold(listOf<Language>()) { acc, languageTag ->
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
                        when (parsedXml.shouldPageBreak()) {
                            true -> convertSubXmlToChapters(
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
//                when (parsedXml.shouldPageBreak()) {
//                    true ->
//                        convertSubXmlToChapters(
//                            stackOfXml,
//                            chapters + accChapter,
//                            Chapter(parsedXml.getChapterHeading(), listOf())
//                        )
//                    false ->
//                        convertSubXmlToChapters(
//                            stackOfXml,
//                            chapters,
//                            addSectionWithMediumBody(accChapter)(parsedXml.text)
//                        )
//                }
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
        TEIAttributes.Type.attribute, ""
    ) + " " + this.attributes.getOrDefault(
        TEIAttributes.Subtype.attribute, ""
    ) + " " + this.attributes.getOrDefault(TEIAttributes.ReferenceNumber.attribute, "")
    return when (attributeHeader.isBlank()) {
        true -> ChapterHeading("New Chapter!")
        false -> ChapterHeading(attributeHeader)
    }
}


private fun ParsedXml.Tag.shouldPageBreak(): Boolean {
    return this.tag == TEIElement.Div.element
}