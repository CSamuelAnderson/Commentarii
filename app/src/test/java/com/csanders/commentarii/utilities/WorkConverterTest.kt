package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.ParsedXml
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.forAll


//generator for the Greek extended codepoints: https://codepoints.net/greek_extended
fun Codepoint.Companion.ancientGreek(): Arb<Codepoint> =
    Arb.of((0x1F00..0x1FFF).map(::Codepoint))

internal class WorkConverterTest : StringSpec({
    "Converts correct work" {

        forAll(
            Arb.choice(Arb.string(16), Arb.string(16, Codepoint.ancientGreek())),
            Arb.choice(Arb.string(16), Arb.string(16, Codepoint.ancientGreek())),
            Arb.list(Arb.choice(Arb.string(14), Arb.string(16, Codepoint.ancientGreek())), 1..5),
            Arb.list(
                Arb.choice(Arb.string(1..280), Arb.string(1..280), Codepoint.ancientGreek()),
                1..250
            )
        ) { authorName, workName, languages, passages ->
            val parsedXml = convertGeneratorsToParsedXml(authorName, workName, languages, passages)
            val work = convertToWork(parsedXml)

            work.header.author == authorName
                    && work.header.title == workName
                    && work.header.languagesUsed == languages
        }
    }
})

fun convertGeneratorsToParsedXml(
    authorName: String,
    workName: String,
    languages: List<String>,
    passages: List<Any>
): ParsedXml {
    return ParsedXml(
        tag = TEIElement.TEI.element,
        subXml = listOf(
            ParsedXml(
                tag = TEIElement.TeiHeader.element,
                subXml = listOf(
                    ParsedXml(
                        tag = TEIHeader.FileDescription.element,
                        subXml = listOf(
                            ParsedXml(
                                tag = TEIHeader.TitleStatement.element,
                                subXml = listOf(
                                    ParsedXml(
                                        tag = TEIHeader.Author.element,
                                        subXml = listOf(
                                            ParsedXml(
                                                text = authorName
                                            )
                                        )
                                    ),
                                    ParsedXml(
                                        tag = TEIHeader.Title.element,
                                        subXml = listOf(
                                            ParsedXml(
                                                text = workName
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    ParsedXml(
                        tag = TEIHeader.ProfileDescription.element,
                        subXml = listOf(
                            ParsedXml(
                                tag = TEIHeader.LanguagesUsed.element,
                                subXml = languages.fold(listOf()) { languageXml, languageName ->
                                    val subTag = ParsedXml(
                                        tag = TEIHeader.Language.element,
                                        subXml = listOf(
                                            ParsedXml(
                                                text = languageName
                                            )
                                        )
                                    )
                                    languageXml.plus(subTag)
                                }
                            )
                        )
                    )
                )
            )
        )
    )
}
