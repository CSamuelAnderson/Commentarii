package com.csanders.commentarii.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.csanders.commentarii.CommentariiMainActivity
import com.csanders.commentarii.ui.screens.home.HomeViewModel
import com.csanders.commentarii.ui.shared.CmtiiNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule(CommentariiMainActivity::class.java)
    lateinit var navController: TestNavHostController

    @Before
    fun setupNavigation() {
        composeTestRule.activity.setContent {
            navController =
                TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(
                ComposeNavigator()
            )
            //Currently fails
            CmtiiNavHost(navController = navController) {}
        }
    }

    @Test
    fun startDestinationIs_Home() {
        composeTestRule
            .onNodeWithTag(HomeViewModel.semanticName)
            .assertIsDisplayed()
    }
}