package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

//All in-line XmlPullParser functions + anything that should belong to parsing in general

val ns: String? = null

/**
 * General parsing
 */
@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.parseTag(startTag: TeiTag): ParsedXml.Tag {
    //Todo: We can curry the parsing here with these three things. And then handle throwing via an Either block.
    this.require(XmlPullParser.START_TAG, ns, startTag.tag)
    val attributes = this.readTagAttributes(startTag)
    val tagMap = getXmlTagStrings()

    tailrec fun getSubTags(
        parser: XmlPullParser,
        subTags: MutableList<ParsedXml> = mutableListOf(),
    ): List<ParsedXml> {

        if (parser.name == startTag.tag && parser.eventType == XmlPullParser.END_TAG) {
            return subTags.toList()
        }

        parser.next()

        if (parser.eventType == XmlPullParser.TEXT && parser.text.isNotBlank()) {
            val textTag = ParsedXml.Text(text = parser.text.trim('\n'))
            subTags.add(textTag)
        }

        //Since this isn't a call to the tailrec part of this section, this could overflow the stack if the XMl file has too many nested tags.
        if (parser.eventType == XmlPullParser.START_TAG) {
            when (val tag = tagMap[parser.name]) {
                null -> {}
                else -> subTags.add(parser.parseTag(tag))
            }
        }

        //Now that we're on a new piece, start it over again.
        return getSubTags(parser, subTags)
    }

    val subTags = getSubTags(this)

    return ParsedXml.Tag(
        tag = startTag,
        attributes = attributes,
        subXml = subTags
    )
}

@Throws(IOException::class, XmlPullParserException::class)
fun XmlPullParser.readTagAttributes(startTag: TeiTag): Map<TeiAttribute, String> {
    this.require(XmlPullParser.START_TAG, ns, startTag.tag)
    val numOfAttributes = this.attributeCount
    val attributeMap = getXmlAttributeStrings()

    tailrec fun helper(
        attributeIndex: Int = 0,
        parsedAttributes: MutableMap<TeiAttribute, String> = mutableMapOf()
    ): Map<TeiAttribute, String> {

        if (attributeIndex > numOfAttributes - 1) {
            return parsedAttributes.toMap()
        }
        when (val attribute = attributeMap[this.getAttributeName(attributeIndex)]) {
            null -> {}
            else -> parsedAttributes[attribute] =
                    this.getAttributeValue(attributeIndex)
        }


        return helper(attributeIndex + 1, parsedAttributes)
    }
    return helper()
}