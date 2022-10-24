package com.csanders.commentarii.utilities

import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.util.Xml
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.csanders.commentarii.R
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
        val resources = Resources.getSystem()
        val goldenAss = R.raw.apuleius_golden_ass_lat
        val context = LocalContext.current
        val stream = context.resources.openRawResource(goldenAss)
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
        lateinit var header: WorkHeader

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

        //Todo: We're only guaranteed a header if we hit a header tag. But that means we do header null-safety in two places already
        //Todo: Is there a way (maybe from a monad? Maybe from default values?) that we can abstract defualt values away?
        val work = Work(header)
        return listOf(work)
    }


    //parses the start tag, and don't ask it to save any data.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.skip(tag: String): Map<String, String> {
        parserLoop(this, tag)
        return mapOf()
    }


    //Todo: Would it be more readable to turn this into a parser property?
    @Throws(XmlPullParserException::class, IOException::class)
    private fun parserLoop(
        parser: XmlPullParser,
        startTag: String,
        onTagRead: (String) -> Map<String, String> = { mapOf() }
        //Todo: to generalize, it's possible we'll need to change the value in this pair to a List. That way something like language can have multiple
        //Todo: That, or order the language account for multiple keys.
    ): Map<String, String> {
        parser.require(XmlPullParser.START_TAG, ns, startTag)
        //We don't want anything in the top-most node, so we skip it for now.
        val map = mutableMapOf<String,String>()

        tailrec fun helper(
            parser: XmlPullParser,
            parsedElements: MutableMap<String, String> = mutableMapOf()
        ): Map<String, String> {
            //End case--we've reached the end tag and are ready to return.
            if (parser.name == startTag && parser.eventType == XmlPullParser.END_TAG) {
                return parsedElements.toMap()
            }

            //Skip at least once so that we don't need to handle the start tag in the lambda
            parser.next()

            //Step case:
            if (parser.eventType == XmlPullParser.START_TAG) {
//                parsedElements.putIfAbsent(parser.name, parsingEvents(parser.name))
                //note the '+' operator allows the second map to override values of the first map.
//              parsedElements + parsingEvents(parser.name)
                parsedElements.putAll(onTagRead(parser.name))
            }

            return helper(parser, parsedElements)
        }
        //We don't want anything from the top-most node.
        //If we do end up wanting something, which could allow us to make text-reading an instance of this function, we might want to change this behavior
        //We could also just move the nextTag in the helper block to happen before the step case, but that would make this decision less apparent.
        parser.next()
        return helper(parser,map)
    }

    //Parses the contents of a teiHeader.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeader(parser: XmlPullParser): WorkHeader {
        val map = parserLoop(parser, TEIElement.TeiHeader.element) { tagInHeader ->
            //Todo: we can see that the current way of doing things duplicates a lot. Can we shorten this?
            when (tagInHeader) {
                TEIFileDescription.FileDescription.element -> parserLoop(parser, tagInHeader) { tagInFileDesc ->
                    when (tagInFileDesc) {
                        TEIFileDescription.TitleStatement.element -> parserLoop(parser, tagInFileDesc) { tagInTitleStatement ->
                            when (tagInTitleStatement) {
                                TEIFileDescription.Title.element -> readTag(parser, tagInTitleStatement)
                                TEIFileDescription.Author.element -> readTag(parser, tagInTitleStatement)
                                else -> parser.skip(tagInTitleStatement)
                            }
                        }
                        else -> parser.skip(tagInFileDesc)
                    }
                }
                TEIFileDescription.ProfileDescription.element -> parserLoop(parser, tagInHeader) { tag ->
                    when (tag) {
                        //This fails, because language used can be a subset.
//                        TEIFileDescription.LanguageUsed.element -> readTag(parser, tag)
                        TEIFileDescription.LanguageUsed.element -> mapOf(Pair(tag,"LATIN BABEEE!"))
                        else -> parser.skip(tag)
                    }
                }
                //Tags we don't care about should be skipped.
                else -> parser.skip(tagInHeader)
            }
        }

        return WorkHeader(
            title = map.getOrDefault(TEIFileDescription.Title.element, "Unknown Work"),
            author = map.getOrDefault(TEIFileDescription.Author.element, "Unknown author"),
            languagesUsed = listOf<String>("Unknown language")
        )
    }


    /**
     * Parses a single
     * Side effects: Moves the parser into the tag body, reads, and then moves it to the tag close.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): Map<String, String> {
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
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

}