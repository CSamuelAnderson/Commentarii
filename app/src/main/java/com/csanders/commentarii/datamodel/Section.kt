package com.csanders.commentarii.datamodel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString


//data class Section(
//    val text: String = "",
//    val footnotes: MutableList<Footnote> = mutableListOf(),
//    val subsections: List<Section> = listOf(),
//)

/**
 * New section. aims to resolve the following issues:
 *      Be the first step from parsedXml to a new format, but not an immediate outcome
 *      Alongside the new Page, change from a tree format to a list
 *      Store footnotes as state
 *
 */
data class Section2(
    val printedString: AnnotatedString = buildAnnotatedString {},
    val isStartOfMajorSection: Boolean = false,
//    val footnotes: MutableList<Footnote> = mutableListOf()
)