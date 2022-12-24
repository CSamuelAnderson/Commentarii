package com.csanders.commentarii.utilities

import android.util.Xml
import com.csanders.commentarii.datamodel.DivisionTag
import com.csanders.commentarii.datamodel.MetadataAttribute
import com.csanders.commentarii.datamodel.TeiAttribute
import org.junit.Assert
import org.junit.Test
import org.xmlpull.v1.XmlPullParser

//Parser tests independent of any particular Xml file
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
        val observedAttributes = parser.readTagAttributes(DivisionTag.Div)

        val expectedAttributes = buildMap {
            put(TeiAttribute.Type, "textpart")
            put(MetadataAttribute.ReferenceNumber, "5")
            put(TeiAttribute.Subtype, "chapter")
        }

        expectedAttributes.forEach {
            Assert.assertEquals(it.value, observedAttributes[it.key])
        }
    }

    @Test
    fun tagWithNoAttributesReturnsEmptyMap() {
        val tagWithNoAttributes = "<div>"
        val parser = setupParser(tagWithNoAttributes)
        val observedAttributes = parser.readTagAttributes(DivisionTag.Div)

        val expectedAttributes = mapOf<String, String>()

        Assert.assertEquals(expectedAttributes, observedAttributes)
    }
}