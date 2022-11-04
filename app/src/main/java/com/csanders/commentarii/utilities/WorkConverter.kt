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
    val bodyTag = parsedXml
        .findTag(TEIElement.Text.element)
        .findTag(TEIElement.TextBody.element)
    val body = convertToSection(bodyTag)

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

private fun convertToSection(parsedXml: ParsedXml): Section {

    //Todo: We'll later use attributes and tag names to set other properties in the section
    //      Such as :
    //      the table of contents names/whether a section should be in the TOC
    //      styling, like whether something should be a Header, italicized, etc.

    tailrec fun getSubsections(acc: List<Section> = listOf(), remainder: List<ParsedXml>): List<Section>{
        //If we have no more Xml to parse, return the stack
        if (remainder.isEmpty()) {
            return acc
        }
        //If it isn't, then pop off the first thing we still have
        val subSection = remainder.first()

        //the subsection might have its own subsections to worry about
        //Note this is actual recursion, not tailrec
        if(subSection.subXml.isNotEmpty()) {
            return getSubsections(acc + convertToSection(subSection), remainder.drop(1))
        }
        //If there is no subXml, it could be a text node. If so, save it to our accumulator
        else if(subSection.text.isNotBlank()) {
            return getSubsections(acc + Section(text = subSection.text), remainder.drop(1))
        }
        //we've finished with this subsection, so drop it off and go again
        return getSubsections(acc, remainder.drop(1))

        //End result: a list of subsections, where either the subsection was populated again using this method
        //or the subsection is just empty text
    }

    val subsections = getSubsections(listOf(), parsedXml.subXml)

    return Section(
        subsections = subsections) //TODO: Take until we actually handle incremental loading
}