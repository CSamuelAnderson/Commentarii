package com.csanders.commentarii.utilities

import android.content.res.Resources
import androidx.annotation.RawRes
import io.kotest.core.spec.style.StringSpec
import com.csanders.commentarii.R
import io.kotest.matchers.shouldBe
import io.mockk.MockK
import io.mockk.every
import io.mockk.mockk
import java.io.InputStream

/**
 * Overall tests for the TEI Parser
 * TODO: Eventually, we'll want to test this using an Arbitrary for XML files. For now we'll just use examples.
 * */
internal class TEIParserTest: StringSpec({

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



    "Golden Ass has correct header" {
        val parser = TEIParser()
        //val goldenAssFilePath = "app/src/main/res/raw/apuleius_golden_ass_lat.xml"
        val goldenAssFilePath = "apuleius_golden_ass_lat.xml"
//        val resources = getStubbedResourceCall(R.raw.apuleius_golden_ass_lat, goldenAssFilePath)
//        val stream = resources.openRawResource(R.raw.apuleius_golden_ass_lat)
//        val stream = this.javaClass.classLoader!!.getResourceAsStream(goldenAssFilePath)
//        val stream = this::class.java.classLoader!!.getResourceAsStream(goldenAssFilePath)
        val stream = ClassLoader.getSystemResourceAsStream(goldenAssFilePath)
        val goldenAss = parser.parse(stream).first()
        goldenAss.header.title.shouldBe("")
        goldenAss.header.author.shouldBe("")
        goldenAss.header.languagesUsed.shouldBe("")
    }
})