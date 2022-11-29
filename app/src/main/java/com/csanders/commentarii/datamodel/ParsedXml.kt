package com.csanders.commentarii.datamodel

/**
 * ParsedXml is an object which stores the Xml data that has been parsed from the file, but has not yet been converted to a type.
 *      Tag is a subclass responsible for holding the tag, attributes, and any nested xml.
 *      Text is a subclass responsible for hilding the text of the xml involved.
 */
sealed class ParsedXml() {
    class Tag(
        val tag: String = "",
        val attributes: Map<String, String> = mapOf(),
        val subXml: List<ParsedXml> = listOf()
    ) : ParsedXml() {
        fun findTag(tagToFind: String): Tag {
            return this.subXml.filterIsInstance<Tag>().find { it.tag == tagToFind }
                ?: Tag()
        }

        fun getFirstText(): String? {
            return this.subXml.filterIsInstance<Text>().find { it.text.isNotBlank() }?.text
        }

    }

    class Text(
        val text: String = ""
    ) : ParsedXml()
}