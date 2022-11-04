package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.ParsedXml
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

//TODO: One day we'll create a wonderful ParsedXml generator, but for now we'll just the nasty example in front of you
internal class WorkConverterTest : StringSpec({

    "Converts correct Header" {
        val authorName = "Apuleius"
        val workName = "The Golden Ass"
        val languages = listOf("English", "Greek", "Latin")
        val parsedXml = ParsedXml(
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
        val work = convertToWork(parsedXml)
        work.header.author.shouldBe(authorName)
        work.header.title.shouldBe(workName)
        work.header.languagesUsed.shouldBe(languages)
    }
})