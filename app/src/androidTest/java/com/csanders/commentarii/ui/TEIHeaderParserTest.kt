package com.csanders.commentarii.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.WorkHeader
import com.csanders.commentarii.utilities.TEIHeaderParser
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TEIHeaderParserTest {
    lateinit var parser: TEIHeaderParser
    lateinit var header: WorkHeader

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupWork() {
        parser = TEIHeaderParser()
        composeTestRule.setContent {
            val goldenAssResource = R.raw.apuleius_golden_ass_lat
            header = parser.getWorkFromResource(goldenAssResource).first().header
        }
    }

    @Test
    fun hasCorrectHeader() {
        val expectedHeader = WorkHeader(
            title = "Metamorphoses",
            author = "Apuleius",
            languagesUsed = listOf("English", "Greek", "Latin")
        )
        Assert.assertEquals(expectedHeader.title, header.title)
        Assert.assertEquals(expectedHeader.author, header.author)
        Assert.assertEquals(expectedHeader.languagesUsed.toSortedSet(), header.languagesUsed.toSortedSet())
    }
}