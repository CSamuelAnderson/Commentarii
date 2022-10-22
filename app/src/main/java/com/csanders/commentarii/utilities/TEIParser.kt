package com.csanders.commentarii.utilities

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.util.Xml
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.datamodel.WorkHeader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


//Right onw this is just a copy of this guy https://developer.android.com/training/basics/network-ops/xml
//TODO: But one day it will be more..
//TODO: Handle all of these errors somewhere.
abstract class ParsedXml(vararg data: kotlin.String)

@JvmInline
value class String(private val tag: kotlin.String)

class TEIParser() {

    private val ns: kotlin.String? = null

    @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
    fun parseFromResource(resourceID: Int): List<Work> {
        val resources = Resources.getSystem()
        val stream = resources.openRawResource(resourceID)
        return parse(stream)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Work> {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return read(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun read(parser: XmlPullParser): Work {
        val work = Work()
        //TODO: Find better initial tag
        //TODO: What about tei Corpus?
        parser.require(XmlPullParser.START_TAG, ns, TEIElement.TEI.element)

        //TODO: I hate while loops. How could you do this in a functional way?
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            //TODO start in a new place
            if (parser.name == TEIElement.TeiHeader.element) {
                readWork()
                return work
            } else {
                skip(parser)
            }
        }
        return Work()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.skip(): XmlPullParser {
        if (this.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (this.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
        return this
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun <A: ParsedXml> parserLoop(parser: XmlPullParser, tag: kotlin.String, map: Map<kotlin.String,List<() -> kotlin.String>>): A {
        parser.require(XmlPullParser.START_TAG, ns, tag)

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when
        }

    }

    //Parses the contents of a teiHeader.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeader(parser: XmlPullParser): workHeader {
        parser.require(XmlPullParser.START_TAG, ns, TEIElement.TeiHeader.element)
        var title: kotlin.String? = null
        var author: kotlin.String? = null
        val languagesUsed = mutableListOf<kotlin.String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

        }
        return WorkHeader(title ?: "Unknown Work", author ?: "Unknown author", languagesUsed)
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun refactoring(parser: XmlPullParser, map: Map<String,(XmlPullParser) -> List<kotlin.String>>): Map<kotlin.String,List<kotlin.String>> {
        if(!map.containsKey(parser.name)){
           refactoring(parser.skip(), map)
        }
        return mapOf(parser.name,map[parser.name](parser))
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLanguagesFromProfileDescription(parser: XmlPullParser): MutableList<kotlin.String> {
        parser.require(XmlPullParser.START_TAG, ns, TEIFileDescription.ProfileDescription.element)
        var languagesUsed = mutableListOf<kotlin.String>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when(parser.name) {
                TEIFileDescription.LanguageUsed.element -> languagesUsed.addAll(readLanguageUsed(parser))
                else -> skip(parser)
            }
        }
        return languagesUsed
    }

    /**
     * parses a single, non-nested string
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readForString(parser: XmlPullParser, tag: kotlin.String): kotlin.String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val decodedString = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return decodedString
    }

    // Processes title tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): kotlin.String {
        parser.require(XmlPullParser.START_TAG, ns, TEIFileDescription.Title.element)
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    // Processes link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): kotlin.String {
        var link = ""
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val tag = parser.name
        val relType = parser.getAttributeValue(null, "rel")
        if (tag == "link") {
            if (relType == "alternate") {
                link = parser.getAttributeValue(null, "href")
                parser.nextTag()
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "link")
        return link
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readSummary(parser: XmlPullParser): kotlin.String {
        parser.require(XmlPullParser.START_TAG, ns, "summary")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "summary")
        return summary
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): kotlin.String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

}