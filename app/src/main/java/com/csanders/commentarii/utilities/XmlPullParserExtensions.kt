package com.csanders.commentarii.utilities

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

//All in-line XmlPullParser functions + anything that should belong to parsing in general

//todo: we may need to change this to the TEI namespace.
private val ns: String? = null

@Throws(XmlPullParserException::class, IOException::class)
fun XmlPullParser.loop(
    startTag: String,
    onTagRead: (String) -> Map<String, List<String>>
): Map<String, List<String>> {
    this.require(XmlPullParser.START_TAG, ns, startTag)
    val map = mutableMapOf<String, List<String>>()

    tailrec fun loop(
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

        return loop(parser, parsedElements)
    }
    return loop(this, map)
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

//parses the start tag, and don't ask it to save any data.
@Throws(XmlPullParserException::class, IOException::class)
fun XmlPullParser.skip(tag: String): Map<String, List<String>> {
    this.loop(tag) { mapOf() }
    return mapOf()
}

/**
 * Parses a single
 * Side effects: Moves the parser into the tag body, reads, and then moves it to the tag close.
 */
@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.readTag(tag: String): Map<String, List<String>> {
    this.require(XmlPullParser.START_TAG, ns, tag)
    val decodedString = readText(this)
    this.require(XmlPullParser.END_TAG, ns, tag)
    return mapOf(Pair(tag, decodedString))
}


@Throws(IOException::class, XmlPullParserException::class)
private fun readText(parser: XmlPullParser): List<String> {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.text
        parser.nextTag()
    }
    return listOf(result)
}

@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.readTagAttributes(startTag: String): Map<String, String> {
    this.require(XmlPullParser.START_TAG, ns, startTag)
    val numOfAttributes = this.attributeCount

    tailrec fun helper(attributeIndex: Int = 0, parsedAttributes: MutableMap<String, String> = mutableMapOf()): Map<String, String> {

        if (attributeIndex > numOfAttributes - 1) {
            return parsedAttributes.toMap()
        }
        parsedAttributes[this.getAttributeName(attributeIndex)] =
            this.getAttributeValue(attributeIndex)

        return helper(attributeIndex + 1, parsedAttributes)
    }
    return helper()
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
