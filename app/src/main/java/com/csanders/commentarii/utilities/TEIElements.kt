package com.csanders.commentarii.utilities

import kotlin.String

//For now we only encode the TEI Lite package
//Some of which is missing here:
//Links
//TODo: We'll probably want the Encoding description somewhere
//TODO: Some of the attributes should be re-organized so that elements are not in it.

//Part of the TEI Lite package. See https://tei-c.org/release/doc/tei-p5-exemplars/pdf/tei_lite.doc.pdf
enum class TEIElement(val element: String) {
    //Primary elements
    TEI("TEI"),
    TeiHeader("teiHeader"),
    Text("text"),
    TeiCorpus("teiCorpus"),
    Item("item"),

    //Encodes the body
    FrontMatter("front"),
    Group("group"),
    TextBody("body"),
    BackMatter("back"),

    //Text division elements
    Paragraph("p"),
    Div("div"), //Divs may be further defined, by other tags, and may also nest within other divs.
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
    StageDirection("stage")


}

enum class TEIAttributes(val attribute: String) {
    //Overall
    XmlID("xml:id"),
    ReferenceNumber("n"),
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

//Not all are encoded, just the ones relevant to this project
enum class TEIFileDescription(val element: String) {
   FileDescription("fileDesc"),

    //Title
    TitleStatement("titleStmt"),
    Title("title"),
    Author("author"),

    //Publication
    PublicationStatement("publicationStmet"),

    //Source
    SourceDescription("sourceDesc"),
    BibliographicCitation("bibl"),
    BibliographicList("listBible"),

    //Profile
    ProfileDescription("profileDesc"),
    LanguageUsed("langUsage"),
    Language("language")

}

enum class TEILanguage(val language: String) {
    Latin("la"),
    AncientGreek("grc"),
    English("en")
}

