package com.example.deviceactions

enum class ActionType(val label: String, val iconName: String) {
    OPEN_APP("Open App", "Apps"),
    SEARCH_YOUTUBE("Search YouTube", "PlayArrow"),
    SEARCH_GOOGLE("Search Google", "Search"),
    OPEN_URL("Open Website", "Language"),
    TOGGLE_FLASHLIGHT("Toggle Flashlight", "FlashlightOn"),
    CHECK_BATTERY("Check Battery", "BatteryFull"),
    ADJUST_VOLUME("Adjust Volume", "VolumeUp"),
    LAUNCH_CAMERA_MODE("Camera Mode", "CameraAlt"),
    OPEN_SETTINGS("Open Settings", "Settings"),
    SET_ALARM("Set Alarm", "Alarm"),
    SET_TIMER("Set Timer", "Timer"),
    OPEN_MAPS("Open Maps Navigation", "Map"),
    DIAL_PHONE("Open Phone Dialer", "Phone"),
    SEND_SMS("Prepare SMS", "Chat"),
    SHARE_TEXT("Share Content", "Share")
}

data class ParsedCommand(
    val isDeviceAction: Boolean,
    val isImageRequest: Boolean,
    val actionType: ActionType? = null,
    val target: String? = null,
    val extraParams: Map<String, String> = emptyMap(),
    val rawQuery: String,
    val confidenceDescription: String = ""
)

data class ActionResult(
    val success: Boolean,
    val message: String,
    val actionType: ActionType,
    val target: String? = null
)
