package com.csanders.commentarii.datamodel

data class ParsedXml(
    val tag: String = "",
    val attributes: Map<String, String> = mapOf(),
    //TODO: The way we handle text is pretty unintuitive. Text is mutually exclusive with having a tag. So tags have no text, but have child texts saved in their subtags
    //  While text might have a tag parent, it has no tag stuff on its own.
    //  We should probably make text a subclass here OR make this class more clear on how it handles this stuff.
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