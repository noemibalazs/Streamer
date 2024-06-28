package com.noemi.streamer

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.noemi.streamer.screen.MainActivity
import com.noemi.streamer.screen.MastodonApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setupMastodonApp() {
        composeRule.activity.setContent {
            MastodonApp()
        }
    }

    @Test
    fun testAppBarIsDisplayed() {
        composeRule.onNodeWithStringId(R.string.label_popular_stream).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.label_icon_content_description)).assertIsDisplayed()
    }

    @Test
    fun testSearchTextFieldDisplayed() {
        composeRule.onNodeWithTag(composeRule.activity.getString(R.string.label_search_text_tag)).assertIsDisplayed()
    }

    @Test
    fun testLazyColumnDisplayed() {
        composeRule.waitUntil {
            composeRule.onNodeWithTag(composeRule.activity.getString(R.string.label_lazy_column_tag)).isDisplayed()
        }
    }
}