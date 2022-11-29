package com.csanders.commentarii.utilities

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.Author
import com.csanders.commentarii.datamodel.Header
import com.csanders.commentarii.datamodel.Language
import com.csanders.commentarii.datamodel.Title
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TEIBookParserTest {
    lateinit var parser: TEIBookParser
    lateinit var header: Header

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setupBook() {
        parser = TEIBookParser()
        composeTestRule.setContent {
            val context = LocalContext.current
            val goldenAssResource = R.raw.apuleius_golden_ass_lat
            header = parser.getBookFromResource(goldenAssResource, context).header
        }
    }

    @Test
    fun goldenAssHasCorrectHeader() {
        val expectedHeader = Header(
            title = Title("Metamorphoses"),
            author = Author("Apuleius"),
            languagesUsed = listOf("English", "Greek", "Latin").map { Language(it) }
        )
        Assert.assertEquals(expectedHeader.title, header.title)
        Assert.assertEquals(expectedHeader.author, header.author)
        Assert.assertEquals(
            expectedHeader.languagesUsed.map { it.value }.toSortedSet(),
            header.languagesUsed.map { it.value }.toSortedSet()
        )
    }

}