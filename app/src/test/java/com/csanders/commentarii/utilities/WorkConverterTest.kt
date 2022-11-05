package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.ParsedXml
import com.csanders.commentarii.datamodel.Section
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
        ) { authorName, workName, languages, passages ->
            val parsedXml = convertGeneratorsToParsedXml(authorName, workName, languages, passages)
            val work = convertToWork(parsedXml)

            val workTexts = gatherWorksText(work.text)

            work.header.author == authorName
                    && work.header.title == workName
                    && work.header.languagesUsed == languages
                    && workTexts == passages
        }
    }
})

fun gatherWorksText(section: Section): List<String> {

    tailrec fun helpGather(acc: List<String>, stack: List<Section>): List<String> {
        if(stack.isEmpty()) {
            return acc
        }

        //If it isn't, then add the current section's text to our accumulator and go again
        val subsection = stack.last()
        if(subsection.text.isNotBlank()){
            return helpGather(acc + subsection.text, stack.dropLast(1))
        }

        val newStack = stack.dropLast(1) + subsection.subsections.reversed()
        return helpGather(acc, newStack)
    }
    return helpGather(listOf(), listOf(section))
}

fun convertGeneratorsToParsedXml(
    authorName: String,
    workName: String,
    languages: List<String>,
    passages: List<String>
): ParsedXml {
    return ParsedXml(tag = TEIElement.TEI.element)
        .insertSubXml(
            ParsedXml(tag = TEIElement.TeiHeader.element)
                .insertSubXml(
                    ParsedXml(tag = TEIHeader.FileDescription.element)
                        .insertSubXml(
                            ParsedXml(tag = TEIHeader.TitleStatement.element)
                                .insertSubXml(
                                    ParsedXml(tag = TEIHeader.Title.element)
                                        .insertText(workName)
                                )
                                .insertSubXml(
                                    ParsedXml(tag = TEIHeader.Author.element)
                                        .insertText(authorName)
                                )
                        )
                )
                .insertSubXml(
                    ParsedXml(tag = TEIHeader.ProfileDescription.element)
                        .insertSubXml(
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
                                    languageXml + subTag
                                })
                        )
                )
        )
        .insertSubXml(
            ParsedXml(tag = TEIElement.Text.element)
                .insertSubXml(
                    ParsedXml(
                        tag = TEIElement.TextBody.element,
                        //TODO: Right now each text gets one div and one paragraph. This doesn't provide confidence that we handle nested divs or other tags.
                        subXml = passages.fold(listOf()) { passageXml, passage ->
                            val subTag = ParsedXml(
                                tag = TEIElement.Div.element
                            ).insertSubXml(
                                ParsedXml(tag = TEIElement.Paragraph.element)
                            ).insertText(passage)
                            passageXml + subTag
                        })
                )
        )
}

fun ParsedXml.insertSubXml(subXml: ParsedXml): ParsedXml {
    return ParsedXml(
        tag = tag,
        attributes = attributes,
        text = text,
        subXml = this.subXml + subXml
    )
}

fun ParsedXml.insertText(subText: String): ParsedXml {
    return ParsedXml(
        tag = tag,
        attributes = attributes,
        text = text,
        subXml = this.subXml + ParsedXml(text = subText)
    )
}