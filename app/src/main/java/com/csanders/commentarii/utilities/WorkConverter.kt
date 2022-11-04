package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.*

/**
 * Converts ParsedXml into a Work data class.
 */

fun convertToWork(parsedXml: ParsedXml): Work {
    if (parsedXml.tag != TEIElement.TEI.element) {
        /**
         * We'll eventually want to wrap this in a Result or something, so we can handle bad cases like this.
         */
        return Work(WorkHeader(), Section())
    }

    val header = getHeader(parsedXml)

    return Work(header, Section())
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
            else -> acc.plus(language)
        }
    }


    return WorkHeader(
        author = author ?: "Unknown Author",
        title = title ?: "Unknown Work",
        languagesUsed = languages
    )
}
