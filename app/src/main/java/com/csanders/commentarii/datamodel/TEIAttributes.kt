package com.csanders.commentarii.datamodel

/**
 * Attributes are flags encoded within a Tei tag that give further instructions within that passage
 * Stylistic attributes instruct how a passage should be formatted, e.g. in italics, as a spoken line, etc.
 * Marginal attributes indicate that an editor has added metatext of some kind, e.g. a date conversion, a note, abbreviations, etc.
 * Metadata attributes indicate other data added to a passage, e.g. the language
 */
sealed class TeiAttribute(val attribute: String) {
    object Type : TeiAttribute("type")
    object Subtype : TeiAttribute("subtype")
}

sealed class StylisticAttribute(attribute: String) : TeiAttribute(attribute) {
    object Render : StylisticAttribute("rend")
    object Highlighted : StylisticAttribute("hi")
}

sealed class EditorialAttribute(attribute: String) : TeiAttribute(attribute)

sealed class MetadataAttribute(attribute: String) : TeiAttribute(attribute) {
    object ReferenceNumber: MetadataAttribute("n")
}

fun getXmlAttributeStrings(): Map<String, TeiAttribute> {
    return TeiAttribute::class.listNestedSealedSubClasses().mapNotNull { it.objectInstance }.associateBy { it.attribute }
}
