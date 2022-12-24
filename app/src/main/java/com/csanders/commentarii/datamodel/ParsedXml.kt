package com.csanders.commentarii.datamodel

import arrow.core.Either

/**
 * ParsedXml is an object which stores the Xml data that has been parsed from the file, but has not yet been converted to a type.
 *      Tag is a subclass responsible for holding the tag, attributes, and any nested xml.
 *      Text is a subclass responsible for hilding the text of the xml involved.
 */

sealed class XmlConversionError(val message: String)
object NotTeiError : XmlConversionError("The starting tag for the file was not TEI")
class TextNotFound(tag: TeiTag) : XmlConversionError("$tag does not have any nested text xml")
class TagNotFound(tagToFind: TeiTag, tagToSearch: TeiTag) :
    XmlConversionError("$tagToFind could not be found in $tagToSearch")

sealed class ParsedXml {
    class Tag(
        val tag: TeiTag,
        val attributes: Map<TeiAttribute, String> = mapOf(),
        val subXml: List<ParsedXml> = listOf()
    ) : ParsedXml() {
        fun findTag(tagToFind: TeiTag): Either<XmlConversionError, Tag> {
            return when (val foundTag =
                this.subXml.filterIsInstance<Tag>().find { it.tag == tagToFind }) {
                null -> Either.Left(TagNotFound(tagToFind, this.tag))
                else -> Either.Right(foundTag)
            }
        }

        fun getFirstText(): Either<XmlConversionError, Text> {
            return when (val text =
                this.subXml.filterIsInstance<Text>().find { it.text.isNotBlank() }) {
                null -> Either.Left(TextNotFound(this.tag))
                else -> Either.Right(text)
            }
        }

    }

    class Text(
        val text: String = ""
    ) : ParsedXml()
}