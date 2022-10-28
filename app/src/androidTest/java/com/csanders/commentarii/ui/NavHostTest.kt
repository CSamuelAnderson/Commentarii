package com.csanders.commentarii.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.csanders.commentarii.CommentariiMainActivity
import com.csanders.commentarii.StartNav
import com.csanders.commentarii.ui.screens.home.HomeViewModel
import com.csanders.commentarii.ui.shared.CmtiiNavHost
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavHostTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<CommentariiMainActivity>()


    private lateinit var mainActivity: CommentariiMainActivity

//    lateinit var navController: TestNavHostController

    @Before
    fun setupNavigation() {
        composeTestRule.activityRule.scenario.onActivity {
            mainActivity = it
        }
    }

    @Test
    fun isScreenHome() {
       composeTestRule
            .onNodeWithTag(HomeViewModel.semanticName)
            .assertIsDisplayed()
    }
}