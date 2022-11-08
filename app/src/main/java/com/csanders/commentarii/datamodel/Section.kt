package com.csanders.commentarii.datamodel

//TODO: It feels awkward that in the domain this ends up being either a header to a new section, or text, but not both.
//  Should probably be two or more classes

data class Section(
    val text: String = "",
    val footnotes: MutableList<Footnote> = mutableListOf(),
    val subsections: List<Section> = listOf(),
)