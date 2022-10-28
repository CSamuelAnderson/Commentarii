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


//Right onw this is just a copy of this guy https://developer.android.com/training/basics/network-ops/xml
//TODO: But one day it will be more..
//TODO: Handle all of these errors somewhere.

class TEIParser() {

    //todo: we may need to change this to the TEI namespace.
    private val ns: String? = null

    //composable so we can grab the context
    @Composable
    @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
    fun parseFromResource(resourceID: Int): List<Work> {
        val context = LocalContext.current
        val stream = context.resources.openRawResource(resourceID)
        return parse(stream)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Work> {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return readWork(parser)
        }
    }

    private fun readWork(parser: XmlPullParser): List<Work> {
        var header = WorkHeader()

        //Todo: since a lower-level function returned a data-class instead of a map, we save off the class and just ignore that it returned a map.
        //Todo: Fine for now, but is there a more elegant way?
        parserLoop(parser, TEIElement.TEI.element) { tag ->
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

    //parses the start tag, and don't ask it to save any data.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.skip(tag: String): Map<String, List<String>> {
        parserLoop(this, tag)
        return mapOf()
    }


    //Todo: Would it be more readable to turn this into a parser property?
    @Throws(XmlPullParserException::class, IOException::class)
    private fun parserLoop(
        parser: XmlPullParser,
        startTag: String,
        onTagRead: (String) -> Map<String, List<String>> = { mapOf() }
        //Todo: to generalize, it's possible we'll need to change the value in this pair to a List. That way something like language can have multiple
        //Todo: That, or order the language account for multiple keys.
    ): Map<String, List<String>> {
        parser.require(XmlPullParser.START_TAG, ns, startTag)
        val map = mutableMapOf<String, List<String>>()

        tailrec fun helper(
            parser: XmlPullParser,
            parsedElements: MutableMap<String, List<String>> = mutableMapOf()
        ): Map<String, List<String>> {

            if (parser.name == startTag && parser.eventType == XmlPullParser.END_TAG) {
                return parsedElements.toMap()
            }
            //Skip at least once so that we don't need to handle the start tag in the lambda
            parser.next()
            if (parser.eventType == XmlPullParser.START_TAG) {
                parsedElements.mergeAll(onTagRead(parser.name))
            }

            return helper(parser, parsedElements)
        }
        return helper(parser, map)
    }

    private fun <A, B> MutableMap<A, List<B>>.mergeAll(map: Map<A, List<B>>) {
        map.entries.forEach { entry ->
            this.merge(entry.key, entry.value) { old, new ->
                val coolList = mutableListOf<B>()
                coolList.addAll(old)
                coolList.addAll(new)
                coolList.toList()
            }
        }
    }

    //Parses the contents of a teiHeader.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeader(parser: XmlPullParser): WorkHeader {
        var languageUsed: Map<String, List<String>> = mapOf()
        val map = parserLoop(parser, TEIElement.TeiHeader.element) { tagInHeader ->
            //Todo: we can see that the current way of doing things duplicates a lot. Can we shorten this?
            when (tagInHeader) {
                TEIFileDescription.FileDescription.element -> parserLoop(
                    parser,
                    tagInHeader
                ) { tagInFileDesc ->
                    when (tagInFileDesc) {
                        TEIFileDescription.TitleStatement.element -> parserLoop(
                            parser,
                            tagInFileDesc
                        ) { tagInTitleStatement ->
                            when (tagInTitleStatement) {
                                TEIFileDescription.Title.element -> readTag(
                                    parser,
                                    tagInTitleStatement
                                )
                                TEIFileDescription.Author.element -> readTag(
                                    parser,
                                    tagInTitleStatement
                                )
                                else -> parser.skip(tagInTitleStatement)
                            }
                        }
                        else -> parser.skip(tagInFileDesc)
                    }
                }
                TEIFileDescription.ProfileDescription.element -> parserLoop(
                    parser,
                    tagInHeader
                ) { tagInProfileDesc ->
                    when (tagInProfileDesc) {
                        TEIFileDescription.LanguagesUsed.element -> {
                            languageUsed = parserLoop(
                                parser,
                                tagInProfileDesc
                            ) { tagInLanguage ->
                                when (tagInLanguage) {
                                    TEIFileDescription.Language.element -> readTag(
                                        parser,
                                        tagInLanguage
                                    )
                                    else -> parser.skip(tagInLanguage)
                                }
                            }
                            mapOf()
                        }
                        else -> parser.skip(tagInProfileDesc)
                    }
                }
                else -> parser.skip(tagInHeader)
            }
        }
        return WorkHeader(
            title = map.getOrDefault(TEIFileDescription.Title.element, listOf("Unknown Work")).first(),
            author = map.getOrDefault(TEIFileDescription.Author.element, listOf("Unknown author")).first(),
            languagesUsed = languageUsed.getOrDefault(TEIFileDescription.Language.element, listOf("Unknown languages"))
        )
    }


    /**
     * Parses a single
     * Side effects: Moves the parser into the tag body, reads, and then moves it to the tag close.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): Map<String, List<String>> {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val decodedString = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return mapOf(Pair(tag, decodedString))
    }

    // Processes link tags in the feed.
//Todo: Google's example for using tags. We'll want to copy this and then discard later.
//    @Throws(IOException::class, XmlPullParserException::class)
//    private fun readLink(parser: XmlPullParser): String {
//        var link = ""
//        parser.require(XmlPullParser.START_TAG, ns, "link")
//        val tag = parser.name
//        val relType = parser.getAttributeValue(null, "rel")
//        if (tag == "link") {
//            if (relType == "alternate") {
//                link = parser.getAttributeValue(null, "href")
//                parser.nextTag()
//            }
//        }
//        parser.require(XmlPullParser.END_TAG, ns, "link")
//        return link
//    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): List<String> {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return listOf(result)
    }

}