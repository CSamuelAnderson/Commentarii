package com.csanders.commentarii.utilities

import android.util.Xml
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParser
import kotlin.math.exp

//Parser tests independent of any particular work
//Sadly must be an instrumentation test because XmlPullParser is a part of the Android library.
class ParserExtensionsTest {

    private fun setupParser(testXml: String): XmlPullParser {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        val stream = testXml.byteInputStream()
        parser.setInput(stream, null)
        parser.nextTag()
        return parser
    }

    @Test
    fun parsesMultipleAttributes() {
        val tagWithThreeAttributes = "<div type=\"textpart\" n=\"5\" subtype=\"chapter\">"
        val parser = setupParser(tagWithThreeAttributes)
        val observedAttributes = parser.readTagAttributes("div")

        val expectedAttributes = buildMap {
            put("type", "textpart")
            put("n", "5")
            put("subtype", "chapter")
        }

        expectedAttributes.forEach {
            Assert.assertEquals(it.value, observedAttributes[it.key])
        }
    }

    @Test
    fun tagWithNoAttributesReturnsEmptyMap() {
        val tagWithNoAttributes = "<div>"
        val parser = setupParser(tagWithNoAttributes)
        val observedAttributes = parser.readTagAttributes("div")

        val expectedAttributes = mapOf<String,String>()

        Assert.assertEquals(expectedAttributes,observedAttributes)
    }
}