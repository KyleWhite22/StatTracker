package com.mobileapps.stattracker.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CreateGroupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createButton_doesNotTriggerSubmit_whenFieldsAreBlank() {
        var submitCalled = false

        composeTestRule.setContent {
            CreateGroupScreen(
                onSubmitClick = { _, _ -> submitCalled = true },
                onCancelClick = {}
            )
        }


        composeTestRule
            .onNodeWithText("Create Group")
            .performClick()


        assert(!submitCalled) {
            "onSubmitClick should not be called when fields are blank"
        }
    }


    @Test
    fun createButton_triggersSubmit_withCorrectValues_whenFieldsAreFilled() {
        var receivedName = ""
        var receivedLocation = ""

        composeTestRule.setContent {
            CreateGroupScreen(
                onSubmitClick = { name, location ->
                    receivedName = name
                    receivedLocation = location
                },
                onCancelClick = {}
            )
        }


        composeTestRule
            .onNodeWithText("Group Name")
            .performClick()
        composeTestRule
            .onNodeWithText("Group Name")
            .performTextInput("Sunday League")


        composeTestRule
            .onNodeWithText("Location")
            .performClick()
        composeTestRule
            .onNodeWithText("Location")
            .performTextInput("Columbus, OH")


        composeTestRule
            .onNodeWithText("Create Group")
            .performClick()


        assert(receivedName == "Sunday League") {
            "Expected name 'Sunday League' but got '$receivedName'"
        }
        assert(receivedLocation == "Columbus, OH") {
            "Expected location 'Columbus, OH' but got '$receivedLocation'"
        }
    }


    @Test
    fun cancelButton_triggersOnCancelClick() {
        var cancelCalled = false

        composeTestRule.setContent {
            CreateGroupScreen(
                onSubmitClick = { _, _ -> },
                onCancelClick = { cancelCalled = true }
            )
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        assert(cancelCalled) {
            "onCancelClick should be called when Cancel is tapped"
        }
    }
}