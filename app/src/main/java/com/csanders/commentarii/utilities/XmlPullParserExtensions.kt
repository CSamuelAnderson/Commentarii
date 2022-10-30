package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.Section
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

//All in-line XmlPullParser functions + anything that should belong to parsing in general

//todo: we may need to change this to the TEI namespace.
val ns: String? = null

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
fun XmlPullParser.readNonNestedTagText(tag: String): Map<String, List<String>> {
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

//Todo, having an overloaded function like this may be a little awkward to use.
//  Also, texts occasionally have notes already inside of them. We should consider adding them to the annotated notes.
@Throws(XmlPullParserException::class, IOException::class)
fun XmlPullParser.loop(
    startTag: String,
    onTagRead: (tag: String) -> Section
): Section {
    this.require(XmlPullParser.START_TAG, ns, startTag)
    val attributes = this.readTagAttributes(startTag)

    tailrec fun loop(
        parser: XmlPullParser,
        //Todo: Consider StringBuilder in this instance?
        parsedText: String = "",
        parsedSections: MutableList<Section>  = mutableListOf(),
    ): Pair<String, List<Section>> {

        //End case: we've completed the current section and can return.
        if (parser.name == startTag && parser.eventType == XmlPullParser.END_TAG) {
            return parsedSections
        }

        //Todo: Add the available text into our text element, but don't assert that we've finished reading.
        parser.next()

        //We've reached the start of a new tag.
        //The way we've set it up, we're expecting this new tag to be a Section
        //but it could very well be a smaller attribute, like a note or a change in emphasis
        //So maybe it isn't appropriate for onTagRead to return a lambda here.
        //Todo: Right now, table-of-content level sections (chapters, books) and styling sections (italics, foreign words) will be saved in the same way.
        //      This will make it difficult to create a table of contents or organize text styling
        //      Maybe if we saved two lists: subsections (proper) and subsection (stylized)
        if (parser.eventType == XmlPullParser.START_TAG) {
            parsedSections.add(onTagRead(parser.name))
        }

        //in any case, we are now done with whatever part of the parser we're one, so we should move it.
        parser.next()
        //Now that we're on a new piece, start it over again.
        return loop(parser, parsedText, parsedSections)
    }
    val parsedStuff = loop(this)

    return Section(text = parsedStuff.first, )
}