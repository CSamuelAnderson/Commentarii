package com.csanders.commentarii.datamodel

/**
 * A page is different than a section. A page could be made up of multiple sections, each with their own syntax, etc.
 * we want it to be a Composable eventually(?)
 */

data class Page(
    val headerSections: Section = Section(),
    val textSections: Section = Section()
)