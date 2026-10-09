package com.example.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.model.ToolActionResult

class DeviceToolManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn: Boolean = false
    private var torchCameraId: String? = null

    init {
        findTorchCamera()
    }

    private fun findTorchCamera() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    torchCameraId = id
                    return
                }
            }
            // Fallback to any camera with flash
            if (torchCameraId == null) {
                cameraManager?.cameraIdList?.forEach { id ->
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                        torchCameraId = id
                        return
                    }
                }
            }
        } catch (_: Exception) {
            torchCameraId = null
        }
    }

    fun isFlashlightAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) && torchCameraId != null
    }

    fun getFlashlightState(): Boolean = isTorchOn

    fun setFlashlight(enable: Boolean): ToolActionResult {
        val hasFlashFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlashFeature || torchCameraId == null) {
            findTorchCamera()
        }

        val targetId = torchCameraId
        if (targetId == null || !hasFlashFeature || cameraManager == null) {
            return ToolActionResult(
                toolName = "Flashlight",
                isSuccess = false,
                details = "Flashlight hardware is not available on this device",
                iconKey = "flashlight_off"
            )
        }

        return try {
            cameraManager.setTorchMode(targetId, enable)
            isTorchOn = enable
            ToolActionResult(
                toolName = "Flashlight",
                isSuccess = true,
                details = if (enable) "Flashlight turned ON" else "Flashlight turned OFF",
                iconKey = if (enable) "flashlight_on" else "flashlight_off"
            )
        } catch (e: CameraAccessException) {
            ToolActionResult(
                toolName = "Flashlight",
                isSuccess = false,
                details = "Camera access error: ${e.localizedMessage ?: "Device busy"}",
                iconKey = "flashlight_off"
            )
        } catch (e: Exception) {
            ToolActionResult(
                toolName = "Flashlight",
                isSuccess = false,
                details = "Flashlight error: ${e.localizedMessage}",
                iconKey = "flashlight_off"
            )
        }
    }

    fun toggleFlashlight(): ToolActionResult {
        return setFlashlight(!isTorchOn)
    }

    fun openApp(target: String): ToolActionResult {
        val cleanTarget = target.trim().lowercase()
        val pm = context.packageManager

        // Known mapping of common names to packages or intents
        when {
            cleanTarget.contains("youtube") || cleanTarget == "yt" -> {
                val launchIntent = pm.getLaunchIntentForPackage("com.google.android.youtube")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ToolActionResult("Open App", true, "YouTube opened", "youtube")
                }
                // Fallback to browser YouTube
                return try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    ToolActionResult("Open App", true, "YouTube opened via browser", "youtube")
                } catch (e: Exception) {
                    ToolActionResult("Open App", false, "Could not open YouTube: ${e.message}", "youtube")
                }
            }

            cleanTarget.contains("whatsapp") -> {
                val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ToolActionResult("Open App", true, "WhatsApp opened", "whatsapp")
                }
                return ToolActionResult("Open App", false, "WhatsApp is not installed on this device", "whatsapp")
            }

            cleanTarget.contains("camera") || cleanTarget.contains("kamera") || cleanTarget.contains("photo") -> {
                return try {
                    val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(cameraIntent)
                    ToolActionResult("Open Camera", true, "Camera opened", "camera")
                } catch (_: Exception) {
                    try {
                        val simpleCam = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(simpleCam)
                        ToolActionResult("Open Camera", true, "Camera opened", "camera")
                    } catch (e: Exception) {
                        ToolActionResult("Open Camera", false, "Camera app not found: ${e.message}", "camera")
                    }
                }
            }

            cleanTarget.contains("map") || cleanTarget.contains("google map") || cleanTarget.contains("naksha") -> {
                return try {
                    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(mapsIntent)
                    ToolActionResult("Open Maps", true, "Google Maps opened", "map")
                } catch (_: Exception) {
                    val launchIntent = pm.getLaunchIntentForPackage("com.google.android.apps.maps")
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        ToolActionResult("Open Maps", true, "Maps opened", "map")
                    } else {
                        ToolActionResult("Open Maps", false, "Maps app not found", "map")
                    }
                }
            }

            cleanTarget.contains("setting") -> {
                return try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ToolActionResult("Open Settings", true, "Device Settings opened", "settings")
                } catch (e: Exception) {
                    ToolActionResult("Open Settings", false, "Failed to open settings: ${e.message}", "settings")
                }
            }

            cleanTarget.contains("wifi") || cleanTarget.contains("wi-fi") -> {
                return try {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ToolActionResult("Wi-Fi Settings", true, "Wi-Fi Settings opened", "wifi")
                } catch (e: Exception) {
                    ToolActionResult("Wi-Fi Settings", false, "Failed to open Wi-Fi settings: ${e.message}", "wifi")
                }
            }

            cleanTarget.contains("bluetooth") -> {
                return try {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ToolActionResult("Bluetooth Settings", true, "Bluetooth Settings opened", "bluetooth")
                } catch (e: Exception) {
                    ToolActionResult("Bluetooth Settings", false, "Failed to open Bluetooth settings: ${e.message}", "bluetooth")
                }
            }

            cleanTarget.contains("chrome") || cleanTarget.contains("browser") || cleanTarget.contains("internet") -> {
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ToolActionResult("Open Browser", true, "Web Browser opened", "browser")
                } catch (e: Exception) {
                    ToolActionResult("Open Browser", false, "Failed to open browser: ${e.message}", "browser")
                }
            }

            cleanTarget.contains("calculator") || cleanTarget.contains("hisab") -> {
                val launchIntent = pm.getLaunchIntentForPackage("com.google.android.calculator")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return ToolActionResult("Calculator", true, "Calculator opened", "calculator")
                }
                return ToolActionResult("Calculator", false, "Calculator app not found", "calculator")
            }

            cleanTarget.contains("alarm") || cleanTarget.contains("clock") || cleanTarget.contains("ghadi") -> {
                return try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ToolActionResult("Alarm Clock", true, "Clock/Alarms opened", "clock")
                } catch (e: Exception) {
                    ToolActionResult("Alarm Clock", false, "Could not open Alarm app: ${e.message}", "clock")
                }
            }

            else -> {
                // Try to find matching installed application
                val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                val matchedApp = installedPackages.firstOrNull { appInfo ->
                    val label = pm.getApplicationLabel(appInfo).toString().lowercase()
                    label.contains(cleanTarget) || cleanTarget.contains(label)
                }

                if (matchedApp != null) {
                    val intent = pm.getLaunchIntentForPackage(matchedApp.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        val name = pm.getApplicationLabel(matchedApp).toString()
                        return ToolActionResult("Open $name", true, "$name opened successfully", "app")
                    }
                }

                return ToolActionResult("Open App", false, "App \"$target\" is not connected or installed yet", "app")
            }
        }
    }

    fun searchWeb(query: String): ToolActionResult {
        if (query.isBlank()) {
            return ToolActionResult("Web Search", false, "Query is empty", "search")
        }

        return try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            ToolActionResult("Web Search", true, "Searching web for: \"$query\"", "search")
        } catch (_: Exception) {
            try {
                val encoded = Uri.encode(query)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ToolActionResult("Web Search", true, "Searching web for: \"$query\"", "search")
            } catch (e: Exception) {
                ToolActionResult("Web Search", false, "Search failed: ${e.message}", "search")
            }
        }
    }

    fun getBatteryStatus(): ToolActionResult {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isCharging = bm?.isCharging == true
            if (level >= 0) {
                val status = if (isCharging) "charging" else "discharging"
                ToolActionResult(
                    toolName = "Battery Status",
                    isSuccess = true,
                    details = "Battery level is $level% ($status)",
                    iconKey = "battery"
                )
            } else {
                ToolActionResult("Battery Status", false, "Battery info unavailable", "battery")
            }
        } catch (e: Exception) {
            ToolActionResult("Battery Status", false, "Failed reading battery: ${e.message}", "battery")
        }
    }

    fun getConnectedToolsInfo(): List<ConnectedToolInfo> {
        val hasFlash = isFlashlightAvailable()
        return listOf(
            ConnectedToolInfo("App Opening Tool", true, "Launches YouTube, WhatsApp, Camera, Maps, Settings, and installed apps via Android Intent Manager"),
            ConnectedToolInfo("Flashlight Control", hasFlash, if (hasFlash) "Hardware Camera Flash connected & ready" else "Device flash hardware not detected"),
            ConnectedToolInfo("Web Search Tool", true, "Google Web Search connected"),
            ConnectedToolInfo("Device Battery Status", true, "Real-time battery level and power telemetry connected"),
            ConnectedToolInfo("Speech Recognition", true, "Android SpeechRecognizer connected"),
            ConnectedToolInfo("Text-to-Speech", true, "Multi-language Android TTS engine (Hindi, Gujarati, English) connected"),
            ConnectedToolInfo("Gemini AI Engine", true, "Connected for natural multi-turn conversations and reasoning")
        )
    }
}

data class ConnectedToolInfo(
    val name: String,
    val isConnected: Boolean,
    val description: String
)
