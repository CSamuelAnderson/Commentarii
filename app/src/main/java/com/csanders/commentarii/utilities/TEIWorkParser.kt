package com.csanders.commentarii.utilities

import android.content.res.Resources.NotFoundException
import android.util.Xml
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.csanders.commentarii.datamodel.Section
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.datamodel.WorkHeader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


class TEIWorkParser() {

    //composable so we can grab the context
    @Composable
    @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
    fun getWorkFromResource(resourceID: Int): List<Work> {
        val context = LocalContext.current
        val stream = context.resources.openRawResource(resourceID)
        return getWork(stream)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun getWork(inputStream: InputStream): List<Work> {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            //Todo: If (TEI), read work, if (TEICorpus), read many works, else return empty work or error
            return readWork(parser)
        }
    }

    //Loop set up to fully read a work
    //Requires parser set set on the TEI header.
    private fun readWork(parser: XmlPullParser): List<Work> {
        var header = WorkHeader()
        var section = Section()

        parser.loop(TEIElement.TEI.element) { tag ->
            when (tag) {
                TEIElement.TeiHeader.element -> {
                    header = readHeader(parser)
                    mapOf()
                }
                TEIElement.Text.element -> {
                    section = parser.readSection(tag)
                    mapOf()
                }
                else -> parser.skip(tag)
            }
        }
        val work = Work(header, section)
        return listOf(work)
    }


    //Parses the contents of a teiHeader.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeader(parser: XmlPullParser): WorkHeader {
        var languageUsed: Map<String, List<String>> = mapOf()
        val map = parser.loop(TEIElement.TeiHeader.element) { tagInHeader ->
            when (tagInHeader) {
                TEIHeader.FileDescription.element -> parser.loop(tagInHeader) { tagInFileDesc ->
                    when (tagInFileDesc) {
                        TEIHeader.TitleStatement.element -> parser.loop(tagInFileDesc) { tagInTitleStatement ->
                            when (tagInTitleStatement) {
                                TEIHeader.Title.element -> parser.readNonNestedTagText(
                                    tagInTitleStatement
                                )
                                TEIHeader.Author.element -> parser.readNonNestedTagText(
                                    tagInTitleStatement
                                )
                                else -> parser.skip(tagInTitleStatement)
                            }
                        }
                        else -> parser.skip(tagInFileDesc)
                    }
                }
                TEIHeader.ProfileDescription.element -> {
                    languageUsed = readProfileForLanguages(parser)
                    mapOf()
                }
                else -> parser.skip(tagInHeader)
            }
        }
        return WorkHeader(
            title = map.getOrDefault(TEIHeader.Title.element, listOf("Unknown Work"))
                .first(),
            author = map.getOrDefault(TEIHeader.Author.element, listOf("Unknown author"))
                .first(),
            languagesUsed = languageUsed.getOrDefault(
                TEIHeader.Language.element,
                listOf("Unknown languages")
            )
        )
    }

    //parses the profile Description for the languages used
    //Requires the parser be on the Profile description tag
    //returns a map of the languages used, or an empty map if none are found.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readProfileForLanguages(parser: XmlPullParser): Map<String, List<String>> {
        val languageUsed = parser.loop(TEIHeader.ProfileDescription.element) { tagInProfileDesc ->
            when (tagInProfileDesc) {
                TEIHeader.LanguagesUsed.element -> parser.loop(tagInProfileDesc) { tagInLanguage ->
                    when (tagInLanguage) {
                        TEIHeader.Language.element -> parser.readNonNestedTagText(tagInLanguage)
                        else -> parser.skip(tagInLanguage)
                    }
                }
                else -> parser.skip(tagInProfileDesc)
            }
        }
        return languageUsed
    }


    //Gets the high text. This
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTEIText(parser: XmlPullParser): Section {
        parser.require(XmlPullParser.START_TAG, ns, TEIElement.Text.element)
        val textAttributes = parser.readTagAttributes(TEIElement.Text.element)
        var section = Section()

        //Todo: keep in mind .loop returns a Map. It isn't the right tool (right now) for also returning attributes, and building the expected nest.
        val textOfWork = parser.loop(TEIElement.Text.element) { tagInText ->
            when (tagInText) {
                //Todo: basically this is just scaffolding for what we expect
                TEIElement.FrontMatter.element -> parser.skip(tagInText)
                TEIElement.BackMatter.element -> parser.skip(tagInText)
                TEIElement.TextBody.element -> {
                section = parser.readSection(tagInText)
                mapOf()
            }
                else -> parser.skip(tagInText)
            }
        }
        return section
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBody(parser: XmlPullParser): Section {
    return Section()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun getSection(parser: XmlPullParser): Section {
        val tag = TEIElement.Text.element
        val attributes = parser.readTagAttributes(tag)
        //This will crash. This assumes that the tag called is non-nested. This could still fail.
        //Also, something like div doesn't even have text.
        val text = parser.readNonNestedTagText(tag)
        val subsection = getSection(parser)
        return Section()
    }
}