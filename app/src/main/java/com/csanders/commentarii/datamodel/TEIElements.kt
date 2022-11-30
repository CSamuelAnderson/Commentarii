package com.csanders.commentarii.datamodel

import kotlin.reflect.KClass

//For now we only encode the TEI Lite package
//Some of which is missing here:
//Links

//Part of the TEI Lite package. See https://tei-c.org/release/doc/tei-p5-exemplars/pdf/tei_lite.doc.pdf
//TODO: Convert to Sealed class that categorizes these
//      Reasons to prefer sealed classes:
//          1) it allows us to document a relationship between tags and attributes
//          2) Encourages the use of strongly typing functions involved with Xml. e.g. findTag must take a Tag object, not any string, or createStyling must take Stylistic attribute, not any attribute.
//      This conversion will continue to be a work in progress as account for more tags

sealed class Tei

/**
 * Basically what I want is a bijective function from TeiTag to String. Enums get that pretty easily.
 * Right now I have a tag -> String through giving each string a tag property it wraps around.
 * But this doesn't guarantee that each Tag has a unique string. How would I create that, while also ensuring that each item is unique?
 *  Maps are one way to guarantee uniqueness. You could create a new data structure called BijectiveMap, or you could create a property-based function that guarantees it.
 */

/**
 * Here's one approach:
 *      Create an inverse list based on the tags, and then query that.
 *      We'll need a unit test to guarantee each map is unique.
 *      Now if we can find a good way generate the list of subclasses/objects
 */

/**
 * returns a list of the class, and its subclasses. If any subclass is also a sealed class, then it calls itself for that subclass and appends the results
 */
fun<T : Any> KClass<T>.listNestedSealedSubClasses(): List<KClass<out T>> {
    return listOf(this) + this.sealedSubclasses.flatMap { it.listNestedSealedSubClasses() }
}

/**
 * Returns a map of each string a TeiTag is associated with.
 * This should be bijective. A unit test will confirm this.
 */
fun getXmlTagStrings(): Map<String, TeiTag> {
    return TeiTag::class.listNestedSealedSubClasses().mapNotNull { it.objectInstance }.associateBy { it.tag }
}

fun getXmlAttributeStrings(): Map<String, TeiAttribute> {
    return TeiAttribute::class.listNestedSealedSubClasses().mapNotNull { it.objectInstance }.associateBy { it.attribute }
}

/**
 * Tags are the sections which categorize Xml texts or sub-tags
 * Primary tags categorize fundamental components of a TEI file or a component of a book, e.g. "TEI" or "TeiCorpus" for the start of a new book or library
 */
sealed class TeiTag(val tag: String) : Tei()
sealed class PrimaryTag(tag: String) : TeiTag(tag) {
    object TeiBook : PrimaryTag("TEI")
    object TeiCorpus : PrimaryTag("teiCorpus")
}

sealed class HeaderTag(tag: String) : TeiTag(tag) {
    object TeiHeader : HeaderTag("teiHeader")


}

sealed class TextTag(tag: String) : TeiTag(tag) {
    object Text : TextTag("text")
    object TextBody : TextTag("body")
}

sealed class DivisionTag(tag: String) : TeiTag(tag) {
    object Div : DivisionTag("div")
}

/**
 * Returns the map of TEI tag names and the associated app-specific types they encode.
 * This is unsorted and has no information for what each tag is supposed to do. For information on each tag, see the class structure in TEIElements
 */


@Deprecated(message = "Used to document which tags have not yet been implemented.")
private enum class TEIElement(val element: String) {
    //Primary elements
//    TEI("TEI"),
//    TeiHeader("teiHeader"),
//    Text("text"),
//    TeiCorpus("teiCorpus"),
    Item("item"),

    //Encodes the body
    FrontMatter("front"),
    Group("group"),
    TextBody("body"),
    BackMatter("back"),

    //Text division elements
//    Div("div"), //Divs may be further defined by other tags, and may also nest within other divs.
    Paragraph("p"),
    Heading("head"),
    Trailer("trailer"),

    //Prose
    PageBeginning("pb"),
    LineBeginning("lb"),
    Milestone("milestone"), //a boundary separating sections of text, but not necessarily by a standard reference system or a structural element

    //Poetry
    PoeticLine("l"),
    PoeticLineGroup("lg"),
    Speech("sp"),
    Speaker("speaker"),
    StageDirection("stage"),

    //Header stuff
    FileDescription("fileDesc"),

    //Title
    TitleStatement("titleStmt"),
    Title("title"),
    Author("author"),

    //Publication
    PublicationStatement("publicationStmt"),

    //Source
    SourceDescription("sourceDesc"),
    BibliographicCitation("bibl"),
    BibliographicList("listBible"),

    //Profile
    ProfileDescription("profileDesc"),
    LanguagesUsed("langUsage"),
    Language("language"),

    //Languages
    Latin("la"),
    AncientGreek("grc"),
    English("en")
}

/**
 * Attributes are flags encoded within a Tei tag that give further instructions within that passage
 * Stylistic attributes instruct how a passage should be formatted, e.g. in italics, as a spoken line, etc.
 * Marginal attributes indicate that an editor has added metatext of some kind, e.g. a date conversion, a note, abbreviations, etc.
 * Metadata attributes indicate other data added to a passage, e.g. the language
 */
sealed class TeiAttribute(val attribute: String) : Tei()
sealed class StylisticAttribute(attribute: String) : TeiAttribute(attribute) {
    object Render : StylisticAttribute("rend")
    object Highlighted : StylisticAttribute("hi")
}

sealed class EditorialAttribute(attribute: String) : TeiAttribute(attribute)
sealed class MetadataAttribute(attribute: String) : TeiAttribute(attribute)

private enum class TEIAttributeElements(val attribute: String) {
    //Overall
    XmlID("xml:id"),
    ReferenceNumber("n"),
    Responsibility("resp"), //Encodes which person or body is responsible for the formatting, e.g. Perseus
    Language("xml:lang"),

    //Emphatics and rendering
    Render("rend"),
    Highlighted("hi"), //marks a text as distinct without giving a reason why
    Emphasized("emph"),
    Foreign("foreign"),
    Gloss("gloss"),
    Label("label"),
    Title("title"), //Title for any kind of work

    //Quotations
    Quoted("q"),
    Mentioned("mentioned"),
    SoCalled("soCalled"),

    //Notes
    Note("note"), //may have attributes name, n, resp, etc.

    //Cross reference
    Reference("ref"),
    Pointer("ptr"), //may use the xml:id to mark where it points. ID's used in this way use a #. e.g. #PN12
    Segment("seg"),

    //Editorial interventions
    Correction("corr"),
    Sic("sic"),
    OriginalForm("orig"),
    Regularization("reg"),
    Choice("choice"), //Offers a number of alternative encodings for the same point in a text
    Addition("add"),
    Gap("gap"),
    Deletion("del"),
    Unclear("unclear"),
    Abbreviation("abbr"),
    Expansion("expan"),

    //Names,Dates,Numbers
    ReferenceString("rs"),
    Name("name"),
    Type("type"),
    Subtype("subtype"),
    Index("index"),
    Date("date"), //Uses when=to offer a specific time
    When("when"),
    Time("time"),
    Number("num"), //uses value= and type=
    Value("value"),
    List("list"),

    //Skipping Bibliographic citations
    //Skipping Tables
    //Skipping figures, graphs

    OrthographicSentence("s"),
    Interpretation("interp"),

    //TODO: Encode anything post-interp


}