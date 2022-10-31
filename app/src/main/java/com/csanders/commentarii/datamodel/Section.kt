package com.csanders.commentarii.datamodel

//We want to keep the lines of actual text separate from the body of the work.
//Because we're allowing the app user to make changes via footnotes.
//In other words, The Work's structure is read-only, while the structure of the work is read-write

data class Section(
    val text: String = "",
    val tag: String = "",
    val footnotes: MutableList<Footnote> = mutableListOf(),
    val attributes: Map<String,String> = mapOf(),
    val subsections: List<Section> = listOf(),
    val shouldBeInTOC: Boolean = true
) {

}