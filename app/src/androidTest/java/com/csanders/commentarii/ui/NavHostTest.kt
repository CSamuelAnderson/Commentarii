package com.csanders.commentarii.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.csanders.commentarii.ui.screens.home.HomeScreen
import com.csanders.commentarii.ui.shared.CmtiiNavHost
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    lateinit var navController: NavController

    @Before
    fun setupNavigation() {
        composeTestRule.setContent {
            navController =
                TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(
                ComposeNavigator()
            )
            CmtiiNavHost(navController = navController as TestNavHostController)
        }
    }

    @Test
    fun startDestinationIs_Home() {
        composeTestRule
            .onNodeWithTag(HomeScreen.semanticName)
            .assertIsDisplayed()
    }
}