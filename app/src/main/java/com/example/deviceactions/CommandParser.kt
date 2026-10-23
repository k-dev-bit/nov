package com.example.deviceactions

import java.util.Locale

object CommandParser {

    fun parse(input: String): ParsedCommand {
        val raw = input.trim()
        val text = raw.lowercase(Locale.ROOT)

        // 1. Image Generation Requests
        val imageKeywords = listOf(
            "generate image", "create image", "draw", "draw a", "generate an image",
            "make a photo", "make an image", "wallpaper banao", "photo banao", "tasveer banao",
            "chitra banao", "image generate karo", "picture of", "imagine a", "photo generate karo"
        )
        for (kw in imageKeywords) {
            if (text.startsWith(kw) || text.contains(kw)) {
                var prompt = text.replace(kw, "").trim()
                if (prompt.startsWith("of") || prompt.startsWith("for") || prompt.startsWith(":")) {
                    prompt = prompt.substring(1).trim()
                }
                return ParsedCommand(
                    isDeviceAction = false,
                    isImageRequest = true,
                    target = if (prompt.isNotBlank()) prompt else raw,
                    rawQuery = raw,
                    confidenceDescription = "Detected Image Generation Request"
                )
            }
        }

        // 2. Volume Controls (Volume up, down, mute, awaz badhao/kam karo)
        if (text.contains("volume") || text.contains("awaz") || text.contains("sound")) {
            val isMute = text.contains("mute") || text.contains("silent") || text.contains("chup")
            val isUp = text.contains("up") || text.contains("increase") || text.contains("badhao") || text.contains("tez") || text.contains("raise") || text.contains("high")
            val isDown = text.contains("down") || text.contains("decrease") || text.contains("kam") || text.contains("ghatao") || text.contains("lower") || text.contains("low")

            if (isMute) {
                return ParsedCommand(
                    isDeviceAction = true,
                    isImageRequest = false,
                    actionType = ActionType.ADJUST_VOLUME,
                    target = "mute",
                    rawQuery = raw,
                    confidenceDescription = "Mute Volume"
                )
            }
            if (isUp) {
                return ParsedCommand(
                    isDeviceAction = true,
                    isImageRequest = false,
                    actionType = ActionType.ADJUST_VOLUME,
                    target = "up",
                    rawQuery = raw,
                    confidenceDescription = "Increase Volume"
                )
            }
            if (isDown) {
                return ParsedCommand(
                    isDeviceAction = true,
                    isImageRequest = false,
                    actionType = ActionType.ADJUST_VOLUME,
                    target = "down",
                    rawQuery = raw,
                    confidenceDescription = "Decrease Volume"
                )
            }
        }

        // 3. Flashlight / Torch
        if (text.contains("flashlight") || text.contains("torch")) {
            val isOff = text.contains("off") || text.contains("band") || text.contains("disable")
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.TOGGLE_FLASHLIGHT,
                target = if (isOff) "off" else "on",
                rawQuery = raw,
                confidenceDescription = if (isOff) "Turn Off Flashlight" else "Turn On Flashlight"
            )
        }

        // 4. Battery Check
        if (text.contains("battery") || text.contains("charge") || text.contains("charging")) {
            if (text.contains("kitni") || text.contains("check") || text.contains("level") || text.contains("status") || text.contains("percent")) {
                return ParsedCommand(
                    isDeviceAction = true,
                    isImageRequest = false,
                    actionType = ActionType.CHECK_BATTERY,
                    rawQuery = raw,
                    confidenceDescription = "Check Battery Status"
                )
            }
        }

        // 5. Camera Photo / Video Recording
        if (text.contains("record video") || text.contains("video banao") || text.contains("video recording")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.LAUNCH_CAMERA_MODE,
                target = "video",
                rawQuery = raw,
                confidenceDescription = "Open Video Recorder"
            )
        }
        if (text.contains("take photo") || text.contains("take picture") || text.contains("photo kheecho") || text.contains("selfie")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.LAUNCH_CAMERA_MODE,
                target = "photo",
                rawQuery = raw,
                confidenceDescription = "Open Camera to Take Photo"
            )
        }

        // 6. YouTube Search vs Open YouTube
        if (text.contains("youtube")) {
            val searchPatterns = listOf("search", "khojo", "dekho", "search karo", "dhundo")
            val hasSearch = searchPatterns.any { text.contains(it) } || text.contains(" par ") || text.contains(" pe ") || text.contains(" for ")
            if (hasSearch && !text.matches(Regex("^(open|launch|kholo)?\\s*youtube\\s*(open|launch|kholo)?$"))) {
                var query = text
                    .replace("search youtube for", "")
                    .replace("search on youtube", "")
                    .replace("youtube search", "")
                    .replace("youtube par", "")
                    .replace("youtube pe", "")
                    .replace("search karo", "")
                    .replace("search", "")
                    .replace("kholo", "")
                    .replace("dekho", "")
                    .replace("open", "")
                    .replace("youtube", "")
                    .trim()
                if (query.isNotBlank()) {
                    return ParsedCommand(
                        isDeviceAction = true,
                        isImageRequest = false,
                        actionType = ActionType.SEARCH_YOUTUBE,
                        target = query,
                        rawQuery = raw,
                        confidenceDescription = "Search YouTube for: $query"
                    )
                }
            }
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_APP,
                target = "youtube",
                rawQuery = raw,
                confidenceDescription = "Launch YouTube"
            )
        }

        // 7. Google Search
        if (text.startsWith("google ") || text.contains("google search") || text.contains("google par search") || text.startsWith("search google for")) {
            val query = text
                .replace("google search for", "")
                .replace("google search", "")
                .replace("search google for", "")
                .replace("google par search karo", "")
                .replace("google par", "")
                .replace("google", "")
                .trim()
            if (query.isNotBlank()) {
                return ParsedCommand(
                    isDeviceAction = true,
                    isImageRequest = false,
                    actionType = ActionType.SEARCH_GOOGLE,
                    target = query,
                    rawQuery = raw,
                    confidenceDescription = "Search Google for: $query"
                )
            }
        }

        // 8. Alarms & Timers
        if (text.contains("alarm")) {
            val timePart = text.replace(Regex(".*(alarm|for|at|lagao|set|karo)"), "").trim()
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.SET_ALARM,
                target = if (timePart.isNotBlank()) timePart else "7:00 AM",
                rawQuery = raw,
                confidenceDescription = "Set System Alarm for ${if (timePart.isNotBlank()) timePart else "7:00 AM"}"
            )
        }
        if (text.contains("timer")) {
            val durationPart = text.replace(Regex(".*(timer|for|lagao|set|karo)"), "").trim()
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.SET_TIMER,
                target = if (durationPart.isNotBlank()) durationPart else "5 minutes",
                rawQuery = raw,
                confidenceDescription = "Set System Timer for ${if (durationPart.isNotBlank()) durationPart else "5 minutes"}"
            )
        }

        // 9. Maps / Navigation
        if (text.contains("map") || text.contains("navigation") || text.contains("navigate to") || text.contains("rasta dikhao")) {
            var dest = text
                .replace("navigate to", "")
                .replace("directions to", "")
                .replace("maps to", "")
                .replace("ka rasta dikhao", "")
                .replace("rasta dikhao", "")
                .replace("open maps", "")
                .replace("maps kholo", "")
                .trim()
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_MAPS,
                target = if (dest.isNotBlank()) dest else "Current Location",
                rawQuery = raw,
                confidenceDescription = "Open Google Maps"
            )
        }

        // 10. Phone Call / Dialer
        if (text.startsWith("call ") || text.startsWith("dial ") || text.contains("phone milao") || text.contains("call lagao")) {
            val contactOrNum = text
                .replace("call lagao", "")
                .replace("call karo", "")
                .replace("phone milao", "")
                .replace("call", "")
                .replace("dial", "")
                .trim()
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.DIAL_PHONE,
                target = contactOrNum,
                rawQuery = raw,
                confidenceDescription = "Open Phone Dialer for $contactOrNum"
            )
        }

        // 11. SMS
        if (text.startsWith("send message") || text.startsWith("send sms") || text.contains("sms bhejo") || text.contains("message karo")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.SEND_SMS,
                target = text,
                rawQuery = raw,
                confidenceDescription = "Prepare SMS Draft"
            )
        }

        // 12. Settings (Wi-Fi, Bluetooth, Display, Apps)
        if (text.contains("wifi") || text.contains("wi-fi")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_SETTINGS,
                target = "wifi",
                rawQuery = raw,
                confidenceDescription = "Open Wi-Fi Settings"
            )
        }
        if (text.contains("bluetooth")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_SETTINGS,
                target = "bluetooth",
                rawQuery = raw,
                confidenceDescription = "Open Bluetooth Settings"
            )
        }
        if (text.contains("display") || text.contains("brightness") || text.contains("screen")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_SETTINGS,
                target = "display",
                rawQuery = raw,
                confidenceDescription = "Open Display Settings"
            )
        }
        if (text.contains("setting") || text.contains("settings")) {
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_SETTINGS,
                target = "main",
                rawQuery = raw,
                confidenceDescription = "Open System Settings"
            )
        }

        // 13. Dynamic App Launcher for ANY app (e.g. "open spotify", "spotify kholo", "launch netflix", "telegram chalao")
        val openPrefixes = listOf("open ", "launch ", "start ")
        for (prefix in openPrefixes) {
            if (text.startsWith(prefix)) {
                val candidateApp = text.removePrefix(prefix).trim()
                if (candidateApp.isNotBlank() && candidateApp.split(" ").size <= 3) {
                    return ParsedCommand(
                        isDeviceAction = true,
                        isImageRequest = false,
                        actionType = ActionType.OPEN_APP,
                        target = candidateApp,
                        rawQuery = raw,
                        confidenceDescription = "Open $candidateApp application"
                    )
                }
            }
        }
        val hindiSuffixes = listOf(" kholo", " chalao", " open karo", " start karo")
        for (suffix in hindiSuffixes) {
            if (text.endsWith(suffix)) {
                val candidateApp = text.removeSuffix(suffix).trim()
                if (candidateApp.isNotBlank() && candidateApp.split(" ").size <= 3) {
                    return ParsedCommand(
                        isDeviceAction = true,
                        isImageRequest = false,
                        actionType = ActionType.OPEN_APP,
                        target = candidateApp,
                        rawQuery = raw,
                        confidenceDescription = "Open $candidateApp application"
                    )
                }
            }
        }

        // 14. Direct URL opening
        if (text.startsWith("http://") || text.startsWith("https://") || (text.contains(".com") && !text.contains(" "))) {
            val url = if (text.startsWith("http")) text else "https://$text"
            return ParsedCommand(
                isDeviceAction = true,
                isImageRequest = false,
                actionType = ActionType.OPEN_URL,
                target = url,
                rawQuery = raw,
                confidenceDescription = "Open URL: $url"
            )
        }

        // Otherwise: Standard AI Chat Request
        return ParsedCommand(
            isDeviceAction = false,
            isImageRequest = false,
            actionType = null,
            target = null,
            rawQuery = raw,
            confidenceDescription = "AI Conversation Query"
        )
    }
}
