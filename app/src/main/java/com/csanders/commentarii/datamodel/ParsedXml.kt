package com.csanders.commentarii.datamodel

//Todo: Convert to Sealed class
data class ParsedXml(
    val tag: String = "",
    val attributes: Map<String, String> = mapOf(),
    val text: String = "",
    val subXml: List<ParsedXml> = listOf()
) {

    fun findTag(tagToFind: String): ParsedXml {
        return this.subXml.find { it.tag == tagToFind } ?: ParsedXml()
    }

    fun getFirstText(): String? {
        return this.subXml.find { it.text.isNotBlank() }?.text
    }
}