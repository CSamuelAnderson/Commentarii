package com.csanders.commentarii.utilities

import androidx.compose.ui.test.junit4.createComposeRule
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.WorkHeader
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TEIWorkParserTest {
    lateinit var parser: TEIWorkParser
    lateinit var header: WorkHeader

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupWork() {
        parser = TEIWorkParser()
        composeTestRule.setContent {
            val goldenAssResource = R.raw.apuleius_golden_ass_lat
            header = parser.getWorkFromResource(goldenAssResource).header
        }
    }

    @Test
    fun goldenAssHasCorrectHeader() {
        val expectedHeader = WorkHeader(
            title = "Metamorphoses",
            author = "Apuleius",
            languagesUsed = listOf("English", "Greek", "Latin")
        )
        Assert.assertEquals(expectedHeader.title, header.title)
        Assert.assertEquals(expectedHeader.author, header.author)
        Assert.assertEquals(
            expectedHeader.languagesUsed.toSortedSet(),
            header.languagesUsed.toSortedSet()
        )
    }

}