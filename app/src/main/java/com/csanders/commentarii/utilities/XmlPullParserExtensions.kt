package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.ParsedXml
import com.csanders.commentarii.datamodel.Section
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

//All in-line XmlPullParser functions + anything that should belong to parsing in general

val ns: String? = null

/**
 * General parsing
 */
@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.parseTag(startTag: String): ParsedXml {
    this.require(XmlPullParser.START_TAG, ns, startTag)
    val tag = this.name
    val attributes = this.readTagAttributes(startTag)

    tailrec fun getSubTags(
        parser: XmlPullParser,
        //Todo: this does not save the text in order with subtags!
        subTags: MutableList<ParsedXml> = mutableListOf(),
    ): List<ParsedXml> {

        if (parser.name == startTag && parser.eventType == XmlPullParser.END_TAG) {
            return subTags.toList()
        }

        parser.next()

        if (parser.eventType == XmlPullParser.TEXT && parser.text.isNotBlank()) {
            //Todo: Add the available text into our text element, but don't assert that we've finished reading.
            //Todo: make the text tag special. ie indicate there it has no tags with an Either or something.
            val textTag = ParsedXml(text = parser.text)
            subTags.add(textTag)
        }

        //Since this isn't a call to the tailrec part of this section, this could overflow the stack if the XMl file has too many nested tags.
        if (parser.eventType == XmlPullParser.START_TAG) {
            subTags.add(parser.parseTag(parser.name))
        }

        //Now that we're on a new piece, start it over again.
        return getSubTags(parser, subTags)
    }

    val subTags = getSubTags(this)

    return ParsedXml(
        tag = tag,
        attributes = attributes,
        subXml = subTags
    )
}

@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.readTagAttributes(startTag: String): Map<String, String> {
    this.require(XmlPullParser.START_TAG, ns, startTag)
    val numOfAttributes = this.attributeCount

    tailrec fun helper(
        attributeIndex: Int = 0,
        parsedAttributes: MutableMap<String, String> = mutableMapOf()
    ): Map<String, String> {

        if (attributeIndex > numOfAttributes - 1) {
            return parsedAttributes.toMap()
        }
        parsedAttributes[this.getAttributeName(attributeIndex)] =
            this.getAttributeValue(attributeIndex)

        return helper(attributeIndex + 1, parsedAttributes)
    }
    return helper()
}