package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.forAll


//generator for the Greek extended codepoints: https://codepoints.net/greek_extended
fun Codepoint.Companion.ancientGreek(): Arb<Codepoint> =
    Arb.of((0x1F00..0x1FFF).map(::Codepoint))

internal class WorkConverterTest : StringSpec({
    "Preserves all strings" {

        forAll(
            Arb.choice(Arb.string(16), Arb.string(16, Codepoint.ancientGreek())),
            Arb.choice(Arb.string(16), Arb.string(16, Codepoint.ancientGreek())),
            Arb.list(Arb.choice(Arb.string(14), Arb.string(16, Codepoint.ancientGreek())), 1..5),
            Arb.list(Arb.choice(Arb.string(360), Arb.string(360, Codepoint.ancientGreek())), 1..50)
        ) { authorName, title, languages, passages ->
            val parsedXml = convertGeneratorsToParsedXml(authorName, title, languages, passages)
            val book = convertToBook(parsedXml)

            val texts = book.gatherAllTexts().filter { it.isNotBlank() }

            book.header.author == Author(authorName)
                    && book.header.title == Title(title)
                    && book.header.languagesUsed == languages.map { Language(it) }
                    && texts == passages
        }
    }
})

fun Book.gatherAllTexts(): List<String> {
    val allChapters =
        this.pages.previousPages + this.pages.openedPage + this.pages.futurePages.reversed()
    return allChapters.flatMap { chapter ->
        chapter.passages.map { it.text }
    }
}

fun convertGeneratorsToParsedXml(
    authorName: String,
    workName: String,
    languages: List<String>,
    passages: List<String>
): ParsedXml.Tag {
    return ParsedXml.Tag(tag = TEIElement.TEI.element)
        .insertSubXml(
            ParsedXml.Tag(tag = TEIElement.TeiHeader.element)
                .insertSubXml(
                    ParsedXml.Tag(tag = TEIHeader.FileDescription.element)
                        .insertSubXml(
                            ParsedXml.Tag(tag = TEIHeader.TitleStatement.element)
                                .insertSubXml(
                                    ParsedXml.Tag(tag = TEIHeader.Title.element)
                                        .insertText(workName)
                                )
                                .insertSubXml(
                                    ParsedXml.Tag(tag = TEIHeader.Author.element)
                                        .insertText(authorName)
                                )
                        )
                )
                .insertSubXml(
                    ParsedXml.Tag(tag = TEIHeader.ProfileDescription.element)
                        .insertSubXml(
                            ParsedXml.Tag(
                                tag = TEIHeader.LanguagesUsed.element,
                                subXml = languages.fold(listOf()) { languageXml, languageName ->
                                    val subTag = ParsedXml.Tag(
                                        tag = TEIHeader.Language.element,
                                        subXml = listOf(
                                            ParsedXml.Text(
                                                text = languageName
                                            )
                                        )
                                    )
                                    languageXml + subTag
                                })
                        )
                )
        )
        .insertSubXml(
            ParsedXml.Tag(tag = TEIElement.Text.element)
                .insertSubXml(
                    ParsedXml.Tag(
                        tag = TEIElement.TextBody.element,
                        //TODO: Right now each text gets one div and one paragraph. This doesn't provide confidence that we handle nested divs or other tags.
                        subXml = passages.fold(listOf()) { passageXml, passage ->
                            val subTag = ParsedXml.Tag(
                                tag = TEIElement.Div.element
                            ).insertSubXml(
                                ParsedXml.Tag(tag = TEIElement.Paragraph.element)
                            ).insertText(passage)
                            passageXml + subTag
                        })
                )
        )
}

fun ParsedXml.Tag.insertSubXml(subXml: ParsedXml): ParsedXml.Tag {
    return ParsedXml.Tag(
        tag = tag,
        attributes = attributes,
        subXml = this.subXml + subXml
    )
}

fun ParsedXml.Tag.insertText(subText: String): ParsedXml.Tag {
    return ParsedXml.Tag(
        tag = tag,
        attributes = attributes,
        subXml = this.subXml + ParsedXml.Text(text = subText)
    )
}