package com.csanders.commentarii.utilities

import com.csanders.commentarii.datamodel.WorkHeader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner

/**
 * Overall tests for the TEI Parser
 * using Robolectric to grab the android.util.XML package
 * */
@RunWith(RobolectricTestRunner::class)
class TEIParserTest {
    private lateinit var parser: TEIParser

    @Before
    internal fun setup() {
        parser = TEIParser()
    }

    @Test
    fun correctHeader_goldenAss() {
        val goldenAssFilePath = "apuleius_golden_ass_lat.xml"
        val stream = ClassLoader.getSystemResourceAsStream(goldenAssFilePath)
        val goldenAss = parser.parse(stream).first()
        val expectedHeader = WorkHeader()
        assertEquals(goldenAss.header, expectedHeader)
    }
}

//internal class TEIParserTest2: StringSpec({

//    fun openXmlFile(filename: String): InputStream
//        = javaClass.classLoader!!.getResource(filename).openStream()
//
    //mock looking up a resource
    //TODO: Unnecessary here, but we may want a better mocking system later on, so leaving this as template
//    fun getStubbedResourceCall(id: Int, resource: String): Resources {
//        val resources: Resources = mockk()
//        every { resources.openRawResource(id) } returns openXmlFile(resource)
//        return resources
//    }



//    "Golden Ass has correct header" {
//        val parser = TEIParser()
//        //val goldenAssFilePath = "app/src/main/res/raw/apuleius_golden_ass_lat.xml"
//        val goldenAssFilePath = "apuleius_golden_ass_lat.xml"
////        val resources = getStubbedResourceCall(R.raw.apuleius_golden_ass_lat, goldenAssFilePath)
////        val stream = resources.openRawResource(R.raw.apuleius_golden_ass_lat)
////        val stream = this.javaClass.classLoader!!.getResourceAsStream(goldenAssFilePath)
////        val stream = this::class.java.classLoader!!.getResourceAsStream(goldenAssFilePath)
//        val stream = ClassLoader.getSystemResourceAsStream(goldenAssFilePath)
//        val goldenAss = parser.parse(stream).first()
//        goldenAss.header.title.shouldBe("")
//        goldenAss.header.author.shouldBe("")
//        goldenAss.header.languagesUsed.shouldBe("")
//    }
//})