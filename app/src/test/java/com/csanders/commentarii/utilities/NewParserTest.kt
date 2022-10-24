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
class NewParserTest {
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