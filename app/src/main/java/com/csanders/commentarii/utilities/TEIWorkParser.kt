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

        parser.loop(TEIElement.TEI.element) { tag ->
            when (tag) {
                TEIElement.TeiHeader.element -> {
                    header = readHeader(parser)
                    mapOf()
                }
                else -> parser.skip(tag)
            }
        }
        val work = Work(header, Section())
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
                                TEIHeader.Title.element -> parser.readTag(tagInTitleStatement)
                                TEIHeader.Author.element -> parser.readTag(tagInTitleStatement)
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
                        TEIHeader.Language.element -> parser.readTag(tagInLanguage)
                        else -> parser.skip(tagInLanguage)
                    }
                }
                else -> parser.skip(tagInProfileDesc)
            }
        }
        return languageUsed
    }


}