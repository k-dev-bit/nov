package com.example.deviceactions

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder

class DeviceActionExecutor(private val context: Context) {

    private var isTorchOn = false

    fun execute(actionType: ActionType, target: String?): ActionResult {
        return try {
            when (actionType) {
                ActionType.TOGGLE_FLASHLIGHT -> toggleFlashlight(target)
                ActionType.CHECK_BATTERY -> checkBattery()
                ActionType.ADJUST_VOLUME -> adjustVolume(target)
                ActionType.LAUNCH_CAMERA_MODE -> launchCameraMode(target)
                ActionType.OPEN_APP -> openApplication(target ?: "chrome")
                ActionType.SEARCH_YOUTUBE -> searchYouTube(target ?: "")
                ActionType.SEARCH_GOOGLE -> searchGoogle(target ?: "")
                ActionType.OPEN_URL -> openUrl(target ?: "https://google.com")
                ActionType.OPEN_SETTINGS -> openSettings(target ?: "main")
                ActionType.SET_ALARM -> setAlarm(target)
                ActionType.SET_TIMER -> setTimer(target)
                ActionType.OPEN_MAPS -> openMaps(target ?: "")
                ActionType.DIAL_PHONE -> dialPhone(target ?: "")
                ActionType.SEND_SMS -> prepareSms(target ?: "")
                ActionType.SHARE_TEXT -> shareContent(target ?: "")
            }
        } catch (e: Exception) {
            ActionResult(
                success = false,
                message = "Failed to execute: ${e.message}",
                actionType = actionType,
                target = target
            )
        }
    }

    private fun adjustVolume(direction: String?): ActionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ActionResult(false, "Audio service unavailable", ActionType.ADJUST_VOLUME, direction)

        return when (direction?.lowercase()) {
            "mute" -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                ActionResult(true, "Media volume muted 🔇", ActionType.ADJUST_VOLUME, "Muted")
            }
            "down" -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                ActionResult(true, "Volume decreased 🔉", ActionType.ADJUST_VOLUME, "Lowered")
            }
            else -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                ActionResult(true, "Volume increased 🔊", ActionType.ADJUST_VOLUME, "Raised")
            }
        }
    }

    private fun launchCameraMode(mode: String?): ActionResult {
        val intent = if (mode == "video") {
            Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
        } else {
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            val label = if (mode == "video") "Video Recorder" else "Camera"
            ActionResult(true, "Opened $label 📷", ActionType.LAUNCH_CAMERA_MODE, mode)
        } else {
            ActionResult(false, "Camera app not found", ActionType.LAUNCH_CAMERA_MODE, mode)
        }
    }

    private fun toggleFlashlight(targetState: String?): ActionResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return ActionResult(false, "Camera service not available on device", ActionType.TOGGLE_FLASHLIGHT)

        val cameraId = cameraManager.cameraIdList.firstOrNull()
            ?: return ActionResult(false, "No camera with flash found on this device", ActionType.TOGGLE_FLASHLIGHT)

        val turnOn = when (targetState?.lowercase()) {
            "on", "enable", "chalu", "jalao" -> true
            "off", "disable", "band" -> false
            else -> !isTorchOn
        }

        cameraManager.setTorchMode(cameraId, turnOn)
        isTorchOn = turnOn
        val msg = if (turnOn) "Flashlight turned ON 🔦" else "Flashlight turned OFF 🌑"
        return ActionResult(true, msg, ActionType.TOGGLE_FLASHLIGHT, if (turnOn) "ON" else "OFF")
    }

    private fun checkBattery(): ActionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            ?: return ActionResult(false, "Battery manager service unavailable", ActionType.CHECK_BATTERY)

        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = bm.isCharging
        val statusText = if (isCharging) "Charging ⚡" else "Discharging"
        return ActionResult(
            success = true,
            message = "Battery is at $level% ($statusText)",
            actionType = ActionType.CHECK_BATTERY,
            target = "$level%"
        )
    }

    private fun openApplication(appName: String): ActionResult {
        val cleanName = appName.lowercase().trim()
        val pm = context.packageManager

        // 1. Well-known packages mapping
        val packageMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "instagram" to "com.instagram.android",
            "whatsapp" to "com.whatsapp",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "camera" to "com.android.camera",
            "photos" to "com.google.android.apps.photos",
            "clock" to "com.google.android.deskclock",
            "calculator" to "com.google.android.calculator",
            "spotify" to "com.spotify.music",
            "netflix" to "com.netflix.mediaclient",
            "telegram" to "org.telegram.messenger",
            "discord" to "com.discord",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android"
        )

        val directPkg = packageMap[cleanName]
        if (directPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(directPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return ActionResult(true, "Opened $cleanName successfully 🚀", ActionType.OPEN_APP, cleanName)
            }
        }

        // 2. Dynamic Installed Application Search by app label
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val launcherApps = pm.queryIntentActivities(mainIntent, 0)
            val matchedApp = launcherApps.firstOrNull { resolveInfo ->
                val label = resolveInfo.loadLabel(pm).toString().lowercase()
                label == cleanName || label.contains(cleanName) || cleanName.contains(label)
            }

            if (matchedApp != null) {
                val pkgName = matchedApp.activityInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(pkgName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    val appLabel = matchedApp.loadLabel(pm).toString()
                    return ActionResult(true, "Opened $appLabel ($pkgName) 🚀", ActionType.OPEN_APP, appLabel)
                }
            }
        } catch (e: Exception) {
            // Fall through to web / store fallback
        }

        // 3. Fallback to web URL or Play Store
        val fallbackUrls = mapOf(
            "youtube" to "https://www.youtube.com",
            "instagram" to "https://www.instagram.com",
            "whatsapp" to "https://web.whatsapp.com",
            "chrome" to "https://www.google.com",
            "maps" to "https://maps.google.com",
            "gmail" to "https://mail.google.com",
            "spotify" to "https://open.spotify.com",
            "netflix" to "https://www.netflix.com",
            "telegram" to "https://web.telegram.org",
            "discord" to "https://discord.com"
        )

        val fallbackUrl = fallbackUrls[cleanName]
        if (fallbackUrl != null) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            return ActionResult(true, "$cleanName opened in web browser", ActionType.OPEN_APP, cleanName)
        }

        // General Play Store search fallback
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$cleanName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (marketIntent.resolveActivity(pm) != null) {
            context.startActivity(marketIntent)
            ActionResult(true, "$cleanName not installed. Searched on Play Store", ActionType.OPEN_APP, cleanName)
        } else {
            ActionResult(false, "App '$cleanName' not found on device", ActionType.OPEN_APP, cleanName)
        }
    }

    private fun searchYouTube(query: String): ActionResult {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val appUri = Uri.parse("vnd.youtube:results?search_query=$encoded")
        val webUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")

        val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "Searching YouTube for: $query", ActionType.SEARCH_YOUTUBE, query)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "Opened YouTube search in browser for: $query", ActionType.SEARCH_YOUTUBE, query)
        }
    }

    private fun searchGoogle(query: String): ActionResult {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.google.com/search?q=$encoded"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Searching Google for: $query", ActionType.SEARCH_GOOGLE, query)
    }

    private fun openUrl(url: String): ActionResult {
        val safeUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Opened $safeUrl", ActionType.OPEN_URL, safeUrl)
    }

    private fun openSettings(target: String): ActionResult {
        val intent = when (target.lowercase()) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "apps" -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            "sound" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Opened ${target.uppercase()} settings", ActionType.OPEN_SETTINGS, target)
    }

    private fun setAlarm(target: String?): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "NOVA Assistant Alarm")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "Alarm setup opened for ${target ?: "requested time"} ⏰", ActionType.SET_ALARM, target)
        } else {
            ActionResult(false, "No Alarm app found on this device", ActionType.SET_ALARM, target)
        }
    }

    private fun setTimer(target: String?): ActionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "NOVA Timer")
            putExtra(AlarmClock.EXTRA_LENGTH, 300)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionResult(true, "Timer set for ${target ?: "5 minutes"} ⏱️", ActionType.SET_TIMER, target)
        } else {
            ActionResult(false, "No Timer app found on this device", ActionType.SET_TIMER, target)
        }
    }

    private fun openMaps(query: String): ActionResult {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val uri = if (query.isNotBlank()) Uri.parse("geo:0,0?q=$encoded") else Uri.parse("geo:0,0")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            ActionResult(true, "Opened Google Maps for: $query 🗺️", ActionType.OPEN_MAPS, query)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$encoded")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            ActionResult(true, "Opened Maps in browser for: $query", ActionType.OPEN_MAPS, query)
        }
    }

    private fun dialPhone(numberOrName: String): ActionResult {
        val digitsOnly = numberOrName.filter { it.isDigit() || it == '+' }
        val uri = if (digitsOnly.isNotBlank()) Uri.parse("tel:$digitsOnly") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Opened Phone dialer ($numberOrName) 📞", ActionType.DIAL_PHONE, numberOrName)
    }

    private fun prepareSms(details: String): ActionResult {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
            putExtra("sms_body", details)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionResult(true, "Prepared SMS message in default messaging app 💬", ActionType.SEND_SMS, details)
    }

    private fun shareContent(text: String): ActionResult {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
        return ActionResult(true, "Opened share sheet", ActionType.SHARE_TEXT, text)
    }
}
