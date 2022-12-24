package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.*
import com.csanders.commentarii.datamodel.MetadataTag.TeiHeader
import com.csanders.commentarii.datamodel.StartingTag.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.forAll


//generator for the Greek extended codepoints: https://codepoints.net/greek_extended
fun Codepoint.Companion.ancientGreek(): Arb<Codepoint> =
    Arb.of((0x1F00..0x1FFF).map(::Codepoint))

internal class BookConverterTest : StringSpec({
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
    bookTitle: String,
    languages: List<String>,
    passages: List<String>
): ParsedXml.Tag {
    return ParsedXml.Tag(tag = TeiBook)
        .insertSubXml(
            ParsedXml.Tag(tag = TeiHeader)
                .insertSubXml(
                    ParsedXml.Tag(tag = MetadataTag.FileDescription)
                        .insertSubXml(
                            ParsedXml.Tag(tag = MetadataTag.TitleStatement)
                                .insertSubXml(
                                    ParsedXml.Tag(tag = MetadataTag.Title)
                                        .insertText(bookTitle)
                                )
                                .insertSubXml(
                                    ParsedXml.Tag(tag = MetadataTag.Author)
                                        .insertText(authorName)
                                )
                        )
                )
                .insertSubXml(
                    ParsedXml.Tag(tag = MetadataTag.ProfileDescription)
                        .insertSubXml(
                            ParsedXml.Tag(
                                tag = MetadataTag.LanguagesUsed,
                                subXml = languages.fold(listOf()) { languageXml, languageName ->
                                    val subTag = ParsedXml.Tag(
                                        tag = MetadataTag.Language,
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
            ParsedXml.Tag(tag = TextTag.TeiText)
                .insertSubXml(
                    ParsedXml.Tag(
                        tag = TextTag.TextBody,
                        //TODO: Right now each text gets one div and one paragraph. This doesn't provide confidence that we handle nested divs or other tags.
                        subXml = passages.fold(listOf()) { passageXml, passage ->
                            val subTag = ParsedXml.Tag(
                                tag = DivisionTag.Div
                            ).insertSubXml(
                                ParsedXml.Tag(tag = SectiionTag.Paragraph)
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