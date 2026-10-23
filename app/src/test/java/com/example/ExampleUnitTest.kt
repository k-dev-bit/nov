package com.example

import com.example.deviceactions.ActionType
import com.example.deviceactions.CommandParser
import com.example.ui.components.parseMarkdownSections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCommandParser_HindiAppLaunch() {
        val parsed = CommandParser.parse("YouTube kholo")
        assertTrue(parsed.isDeviceAction)
        assertEquals(ActionType.OPEN_APP, parsed.actionType)
        assertEquals("youtube", parsed.target)
    }

    @Test
    fun testCommandParser_DynamicAppLauncher() {
        val parsed1 = CommandParser.parse("Open Spotify")
        assertTrue(parsed1.isDeviceAction)
        assertEquals(ActionType.OPEN_APP, parsed1.actionType)
        assertEquals("spotify", parsed1.target)

        val parsed2 = CommandParser.parse("Netflix chalao")
        assertTrue(parsed2.isDeviceAction)
        assertEquals(ActionType.OPEN_APP, parsed2.actionType)
        assertEquals("netflix", parsed2.target)
    }

    @Test
    fun testCommandParser_VolumeControls() {
        val parsedUp = CommandParser.parse("Volume badhao")
        assertTrue(parsedUp.isDeviceAction)
        assertEquals(ActionType.ADJUST_VOLUME, parsedUp.actionType)
        assertEquals("up", parsedUp.target)

        val parsedDown = CommandParser.parse("Decrease volume")
        assertTrue(parsedDown.isDeviceAction)
        assertEquals(ActionType.ADJUST_VOLUME, parsedDown.actionType)
        assertEquals("down", parsedDown.target)

        val parsedMute = CommandParser.parse("Mute volume")
        assertTrue(parsedMute.isDeviceAction)
        assertEquals(ActionType.ADJUST_VOLUME, parsedMute.actionType)
        assertEquals("mute", parsedMute.target)
    }

    @Test
    fun testCommandParser_CameraModes() {
        val parsedVideo = CommandParser.parse("Record video")
        assertTrue(parsedVideo.isDeviceAction)
        assertEquals(ActionType.LAUNCH_CAMERA_MODE, parsedVideo.actionType)
        assertEquals("video", parsedVideo.target)

        val parsedPhoto = CommandParser.parse("Take photo")
        assertTrue(parsedPhoto.isDeviceAction)
        assertEquals(ActionType.LAUNCH_CAMERA_MODE, parsedPhoto.actionType)
        assertEquals("photo", parsedPhoto.target)
    }

    @Test
    fun testCommandParser_EnglishFlashlightToggle() {
        val parsedOn = CommandParser.parse("Turn on flashlight")
        assertTrue(parsedOn.isDeviceAction)
        assertEquals(ActionType.TOGGLE_FLASHLIGHT, parsedOn.actionType)
        assertEquals("on", parsedOn.target)

        val parsedOff = CommandParser.parse("Torch band karo")
        assertTrue(parsedOff.isDeviceAction)
        assertEquals(ActionType.TOGGLE_FLASHLIGHT, parsedOff.actionType)
        assertEquals("off", parsedOff.target)
    }

    @Test
    fun testCommandParser_BatteryCheck() {
        val parsed = CommandParser.parse("Battery kitni hai")
        assertTrue(parsed.isDeviceAction)
        assertEquals(ActionType.CHECK_BATTERY, parsed.actionType)
    }

    @Test
    fun testCommandParser_ImageGeneration() {
        val parsed = CommandParser.parse("Generate an image of a neon cyber city")
        assertFalse(parsed.isDeviceAction)
        assertTrue(parsed.isImageRequest)
        assertNotNull(parsed.target)
    }

    @Test
    fun testCommandParser_GeneralAiQuery() {
        val parsed = CommandParser.parse("Explain recursion in Kotlin")
        assertFalse(parsed.isDeviceAction)
        assertFalse(parsed.isImageRequest)
    }

    @Test
    fun testMarkdownParser_CodeBlocks() {
        val sample = "Here is the code:\n```kotlin\nval x = 42\n```\nDone."
        val sections = parseMarkdownSections(sample)
        assertTrue(sections.size >= 2)
    }
}
