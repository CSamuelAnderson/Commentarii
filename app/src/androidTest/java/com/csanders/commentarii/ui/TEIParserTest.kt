package com.csanders.commentarii.ui

import androidx.compose.ui.test.junit4.createComposeRule
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.WorkHeader
import com.csanders.commentarii.utilities.TEIParser
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TEIParserTest {
    lateinit var parser: TEIParser
    lateinit var header: WorkHeader

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupWork() {
        parser = TEIParser()
        composeTestRule.setContent {
            val goldenAssResource = R.raw.apuleius_golden_ass_lat
            header = parser.parseFromResource(goldenAssResource).first().header
        }
    }

    @Test
    fun apuleius_hasCorrectHeader() {
        val expectedHeader = WorkHeader(
            title = "Metamorphoses",
            author = "Apuleius",
            languagesUsed = listOf("Greek", "Latin")
        )
        Assert.assertEquals(expectedHeader.title, header.title)
        Assert.assertEquals(expectedHeader.author, header.author)
        Assert.assertEquals(expectedHeader.languagesUsed, header.languagesUsed)
    }
}