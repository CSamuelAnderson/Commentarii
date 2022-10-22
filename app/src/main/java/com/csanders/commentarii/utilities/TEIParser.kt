package com.csanders.commentarii.utilities

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.util.Xml
import com.csanders.commentarii.datamodel.TextOfWork
import com.csanders.commentarii.datamodel.Work
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


//Right onw this is just a copy of this guy https://developer.android.com/training/basics/network-ops/xml
//TODO: But one day it will be more..
//TODO: Handle all of these errors somewhere.
data class Entry(val title: String?, val summary: String?, val link: String?)

class TEIParser() {
    companion object{
        val parser = TEIParser()
        @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
        fun parseFromResource(resourceID: Int): Pair<Work,List<TextOfWork>> {
            val resources = Resources.getSystem()
            val stream = resources.openRawResource(resourceID)
            return parser.parse(stream)
        }
    }
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Pair<Work,List<TextOfWork>> {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Pair<Work,List<TextOfWork>> {
        val lines = mutableListOf<TextOfWork>()
        //TODO: Find better initial tag
        parser.require(XmlPullParser.START_TAG, ns, "feed")

        //TODO: I hate while loops. How could you do this in a functional way?
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            //TODO start in a new place
            if (parser.name == "entry") {
                val work = readWork(parser)
                return Pair(work,lines)
            } else {
                skip(parser)
            }
        }
       return Pair(Work(),lines)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }



    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readWork(parser: XmlPullParser): Work {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var title: String? = null
        var summary: String? = null
        var link: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "summary" -> summary = readSummary(parser)
                "link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }
        return Work()
    }

    // Processes title tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    // Processes link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): String {
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
    private fun readSummary(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "summary")
        val summary = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "summary")
        return summary
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

}