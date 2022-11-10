package com.csanders.commentarii.utilities

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.csanders.commentarii.datamodel.*

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
    val body = convertToSection(parsedXml = bodyTag)
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

private fun convertToStupidSection(parsedXml: ParsedXml): Section {

    //Todo: We'll later use attributes and tag names to set other properties in the section
    //      Such as :
    //      the table of contents names/whether a section should be in the TOC
    //      styling, like whether something should be a Header, italicized, etc.

    tailrec fun getSubsections(
        acc: List<Section> = listOf(),
        remainder: List<ParsedXml>
    ): List<Section> {
        if (remainder.isEmpty()) {
            return acc
        }
        val subSection = remainder.first()

        //Note this is actual recursion, not tailrec
        if (subSection.subXml.isNotEmpty()) {
            return getSubsections(acc + convertToStupidSection(subSection), remainder.drop(1))
        } else if (subSection.text.isNotBlank()) {
            return getSubsections(acc + Section(text = subSection.text), remainder.drop(1))
        }
        return getSubsections(acc, remainder.drop(1))

    }

    val subsections = getSubsections(listOf(), parsedXml.subXml)

    return Section(
        subsections = subsections
    ) //TODO: Take until we actually handle incremental loading
}

private fun convertToSection(
    parentAnnotation: AnnotatedString = buildAnnotatedString {},
    parsedXml: ParsedXml
): List<Section2> {

    val topLevelAnnotation: AnnotatedString = parentAnnotation + buildTopLevelAnnotation(parsedXml)

    //Generates whatever annotation should be set at the top.
    //e.g. 'italics' will make everything following italicized
    val inheritedAnnotation: AnnotatedString =
        parentAnnotation + buildInheritedAnnotation(parsedXml)


    return listOf(
        Section2(
            topLevelAnnotation,
            isMajorSection(parsedXml)
        )
    ) + parsedXml.subXml.flatMap { child ->
        convertToSection(inheritedAnnotation, child)
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
           val chapterHeading = when(attributeHeader.isEmpty()) {
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