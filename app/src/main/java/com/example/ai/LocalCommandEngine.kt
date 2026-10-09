package com.example.ai

import com.example.model.LanguageType
import com.example.model.ToolActionResult
import com.example.tools.DeviceToolManager

data class CommandEvaluation(
    val handledLocally: Boolean,
    val responseText: String,
    val toolResult: ToolActionResult? = null,
    val detectedLanguage: LanguageType = LanguageType.ENGLISH
)

class LocalCommandEngine(private val deviceToolManager: DeviceToolManager) {

    fun evaluate(command: String): CommandEvaluation? {
        val raw = command.trim()
        val lower = raw.lowercase()
        val lang = LanguageDetector.detect(raw)

        // 1. Language switch request: "Gujarati ma vaat karo"
        if (lower.contains("gujarati ma vaat") || lower.contains("gujarati ma bolo") ||
            lower.contains("gujarati bolo") || lower.contains("ગુજરાતી માં વાત") ||
            (lower.contains("gujarati") && (lower.contains("vaat") || lower.contains("talk") || lower.contains("speak")))
        ) {
            return CommandEvaluation(
                handledLocally = true,
                responseText = "હા, હવે હું તમારી સાથે ગુજરાતીમાં વાત કરીશ.",
                detectedLanguage = LanguageType.GUJARATI
            )
        }

        if (lower.contains("hindi me baat") || lower.contains("hindi me bolo") || lower.contains("हिन्दी में बात")) {
            return CommandEvaluation(
                handledLocally = true,
                responseText = "हाँ, अब मैं आपसे हिन्दी में बात करूँगी। बताइए मैं क्या मदद करूँ?",
                detectedLanguage = LanguageType.HINDI
            )
        }

        if (lower.contains("speak in english") || lower.contains("talk in english")) {
            return CommandEvaluation(
                handledLocally = true,
                responseText = "Sure, I will speak with you in English now. How can I help you?",
                detectedLanguage = LanguageType.ENGLISH
            )
        }

        // 2. Specific prompt example: "Mujhe ek AI app ka idea do."
        if (lower.contains("ai app") && (lower.contains("idea") || lower.contains("batao") || lower.contains("do") || lower.contains("sugest"))) {
            val response = when (lang) {
                LanguageType.GUJARATI -> "ચોક્કસ. હું તમને એક વ્યવહારુ વિચાર આપી શકું છું: એક સ્થાનિક ભાષાનું વૉઇસ AI સહાયક અથવા સ્માર્ટ કિચન ઇન્વેન્ટરી ટ્રેકર!"
                LanguageType.HINDI -> "बिल्कुल। मैं तुम्हें AI app के कुछ practical ideas दे सकती हूँ: जैसे कि एक वॉइस-बेस्ड लोकल टास्क मैनेजर या रियल-टाइम ट्रांसलेशन असिस्टेंट।"
                LanguageType.HINGLISH -> "Bilkul. Main tumhe AI app ke kuch practical ideas de sakti hoon."
                LanguageType.ENGLISH -> "Sure! A great idea is a multilingual smart voice assistant for everyday phone automation and local task management."
            }
            return CommandEvaluation(
                handledLocally = true,
                responseText = response,
                detectedLanguage = lang
            )
        }

        // 3. YouTube open: "YouTube kholo" / "Open YouTube"
        if (lower.contains("youtube")) {
            if (lower.contains("kholo") || lower.contains("open") || lower.contains("chalu") ||
                lower.contains("shuru") || lower.contains("खोलो") || lower.contains("ખોલો") || lower.contains("start")
            ) {
                val toolResult = deviceToolManager.openApp("youtube")
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "YouTube ખોલી રહી છું."
                        LanguageType.HINDI -> "YouTube खोल रही हूँ।"
                        LanguageType.HINGLISH -> "YouTube open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening YouTube now."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "YouTube ખોલવા માટે ટૂલ કનેક્ટેડ નથી."
                        LanguageType.HINDI -> "YouTube टूल अभी कनेक्टेड नहीं है।"
                        LanguageType.HINGLISH -> "YouTube open karne ka feature abhi connected nahi hai."
                        LanguageType.ENGLISH -> "The YouTube app tool is not connected or available yet."
                    }
                }
                return CommandEvaluation(
                    handledLocally = true,
                    responseText = response,
                    toolResult = toolResult,
                    detectedLanguage = lang
                )
            }
        }

        // 4. Flashlight control: "Flashlight on karo" / "Flashlight band karo"
        val isFlashlightQuery = lower.contains("flashlight") || lower.contains("torch") ||
                lower.contains("ફ્લેશલાઇટ") || lower.contains("ટોર્ચ") || lower.contains("फ्लैशलाइट")

        if (isFlashlightQuery) {
            val isTurnOn = lower.contains("on") || lower.contains("chalu") || lower.contains("shuru") ||
                    lower.contains("kholo") || lower.contains("ચાલુ") || lower.contains("चालू") ||
                    lower.contains("જગાવો") || lower.contains("turn on") || lower.contains("enable")

            val isTurnOff = lower.contains("off") || lower.contains("band") || lower.contains("stop") ||
                    lower.contains("બંધ") || lower.contains("बंद") || lower.contains("turn off") || lower.contains("disable")

            if (isTurnOn) {
                val toolResult = deviceToolManager.setFlashlight(true)
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "ફ્લેશલાઇટ ચાલુ કરી રહી છું."
                        LanguageType.HINDI -> "फ़्लैशलाइट ऑन कर रही हूँ।"
                        LanguageType.HINGLISH -> "Flashlight on kar rahi hoon."
                        LanguageType.ENGLISH -> "Flashlight turned on."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "આ ડિવાઇસ પર ફ્લેશલાઇટ ટૂલ કનેક્ટેડ નથી."
                        LanguageType.HINDI -> "इस डिवाइस पर फ़्लैशलाइट फ़ीचर कनेक्टेड नहीं है।"
                        LanguageType.HINGLISH -> "Flashlight feature abhi connected nahi hai ya device par available nahi hai."
                        LanguageType.ENGLISH -> "The flashlight tool is not connected or unavailable on this device."
                    }
                }
                return CommandEvaluation(
                    handledLocally = true,
                    responseText = response,
                    toolResult = toolResult,
                    detectedLanguage = lang
                )
            } else if (isTurnOff) {
                val toolResult = deviceToolManager.setFlashlight(false)
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "ફ્લેશલાઇટ બંધ કરી રહી છું."
                        LanguageType.HINDI -> "फ़्लैशलाइट बंद कर रही हूँ।"
                        LanguageType.HINGLISH -> "Flashlight band kar rahi hoon."
                        LanguageType.ENGLISH -> "Flashlight turned off."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "ફ્લેશલાઇટ બંધ કરી શકાઈ નથી, ટૂલ કનેક્ટેડ નથી."
                        LanguageType.HINDI -> "फ़्लैशलाइट टूल उपलब्ध नहीं है।"
                        LanguageType.HINGLISH -> "Flashlight tool connected nahi hai."
                        LanguageType.ENGLISH -> "Flashlight tool is not connected."
                    }
                }
                return CommandEvaluation(
                    handledLocally = true,
                    responseText = response,
                    toolResult = toolResult,
                    detectedLanguage = lang
                )
            }
        }

        // 5. WhatsApp: "WhatsApp kholo" / "Open WhatsApp"
        if (lower.contains("whatsapp")) {
            if (lower.contains("kholo") || lower.contains("open") || lower.contains("chalu") ||
                lower.contains("ખોલો") || lower.contains("खोलो")
            ) {
                val toolResult = deviceToolManager.openApp("whatsapp")
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "WhatsApp ખોલી રહી છું."
                        LanguageType.HINDI -> "WhatsApp खोल रही हूँ।"
                        LanguageType.HINGLISH -> "WhatsApp open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening WhatsApp now."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "WhatsApp આ ડિવાઇસ પર ઇન્સ્ટોલ કરેલ નથી."
                        LanguageType.HINDI -> "WhatsApp इस डिवाइस पर इंस्टॉल नहीं है।"
                        LanguageType.HINGLISH -> "WhatsApp device par installed nahi hai."
                        LanguageType.ENGLISH -> "WhatsApp is not installed on this device."
                    }
                }
                return CommandEvaluation(true, response, toolResult, lang)
            }
        }

        // 6. Camera: "Camera kholo" / "Photo khincho"
        if (lower.contains("camera") || lower.contains("kamera") || lower.contains("કેમેરા") || lower.contains("कैमरा")) {
            if (lower.contains("kholo") || lower.contains("open") || lower.contains("chalu") ||
                lower.contains("start") || lower.contains("ખોલો") || lower.contains("खोलो") || lower.contains("photo")
            ) {
                val toolResult = deviceToolManager.openApp("camera")
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "કેમેરા શરૂ કરી રહી છું."
                        LanguageType.HINDI -> "कैमरा शुरू कर रही हूँ।"
                        LanguageType.HINGLISH -> "Camera open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening camera now."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "કેમેરા ટૂલ કનેક્ટેડ નથી."
                        LanguageType.HINDI -> "कैमरा टूल कनेक्टेड नहीं है।"
                        LanguageType.HINGLISH -> "Camera feature abhi connected nahi hai."
                        LanguageType.ENGLISH -> "Camera tool is not connected."
                    }
                }
                return CommandEvaluation(true, response, toolResult, lang)
            }
        }

        // 7. Maps: "Google Maps kholo"
        if (lower.contains("maps") || lower.contains("map") || lower.contains("naksha") || lower.contains("નકશો")) {
            if (lower.contains("kholo") || lower.contains("open") || lower.contains("chalu") || lower.contains("ખોલો")) {
                val toolResult = deviceToolManager.openApp("maps")
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "ગૂગલ મેપ્સ ખોલી રહી છું."
                        LanguageType.HINDI -> "Google Maps खोल रही हूँ।"
                        LanguageType.HINGLISH -> "Google Maps open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening Google Maps."
                    }
                } else {
                    "Google Maps tool is not connected yet."
                }
                return CommandEvaluation(true, response, toolResult, lang)
            }
        }

        // 8. Settings: "Settings kholo"
        if (lower.contains("setting") || lower.contains("સેટિંગ")) {
            if (lower.contains("kholo") || lower.contains("open") || lower.contains("खोलो") || lower.contains("ખોલો")) {
                val toolResult = deviceToolManager.openApp("settings")
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "સેટિંગ્સ ખોલી રહી છું."
                        LanguageType.HINDI -> "फ़ोन सेटिंग्स खोल रही हूँ।"
                        LanguageType.HINGLISH -> "Settings open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening Settings."
                    }
                } else {
                    "Settings tool not connected."
                }
                return CommandEvaluation(true, response, toolResult, lang)
            }
        }

        // 9. Battery: "Battery kitni hai"
        if (lower.contains("battery") || lower.contains("betri") || lower.contains("બેટરી") || lower.contains("बैटरी")) {
            val toolResult = deviceToolManager.getBatteryStatus()
            val response = if (toolResult.isSuccess) {
                when (lang) {
                    LanguageType.GUJARATI -> "તમારી બેટરી ${toolResult.details} છે."
                    LanguageType.HINDI -> "${toolResult.details}"
                    LanguageType.HINGLISH -> "Aapki phone battery: ${toolResult.details}"
                    LanguageType.ENGLISH -> toolResult.details
                }
            } else {
                "Battery telemetry is not connected yet."
            }
            return CommandEvaluation(true, response, toolResult, lang)
        }

        // 10. Web search: "search karo ..." / "search for ..."
        if (lower.startsWith("search ") || lower.contains("search karo") || lower.contains("shodh karo") || lower.contains("શોધ કરો")) {
            val query = lower.replace("search karo", "")
                .replace("search for", "")
                .replace("search", "")
                .replace("google par", "")
                .replace("par", "")
                .trim()
            if (query.isNotEmpty()) {
                val toolResult = deviceToolManager.searchWeb(query)
                val response = if (toolResult.isSuccess) {
                    when (lang) {
                        LanguageType.GUJARATI -> "\"$query\" માટે વેબ સર્ચ કરી રહી છું."
                        LanguageType.HINDI -> "\"$query\" सर्च कर रही हूँ।"
                        LanguageType.HINGLISH -> "\"$query\" search kar rahi hoon."
                        LanguageType.ENGLISH -> "Searching the web for \"$query\"."
                    }
                } else {
                    "Search tool is not connected."
                }
                return CommandEvaluation(true, response, toolResult, lang)
            }
        }

        // 11. GitHub & Download Links: "GitHub kholo", "GitHub download link", "Download link"
        val isGitHubQuery = lower.contains("github") || lower.contains("git hub") ||
                (lower.contains("download") && (lower.contains("link") || lower.contains("apk") || lower.contains("code") || lower.contains("repo") || lower.contains("app")))

        if (isGitHubQuery) {
            val isDownloadSpecific = lower.contains("download") || lower.contains("release") || lower.contains("apk") || lower.contains("zip")
            val toolResult = if (lower.contains("zip") || lower.contains("source")) {
                deviceToolManager.downloadSourceCode()
            } else if (isDownloadSpecific) {
                deviceToolManager.openGitHubReleases()
            } else {
                deviceToolManager.openGitHub()
            }

            val response = if (toolResult.isSuccess) {
                if (isDownloadSpecific) {
                    when (lang) {
                        LanguageType.GUJARATI -> "હું GitHub APK ડાઉનલોડ પેજ ખોલી રહી છું."
                        LanguageType.HINDI -> "मैं GitHub APK डाउनलोड पेज खोल रही हूँ।"
                        LanguageType.HINGLISH -> "Main GitHub download page open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening GitHub APK download page."
                    }
                } else {
                    when (lang) {
                        LanguageType.GUJARATI -> "હું GitHub રિપોઝિટરી ખોલી રહી છું."
                        LanguageType.HINDI -> "मैं GitHub रिपॉजिटरी खोल रही हूँ।"
                        LanguageType.HINGLISH -> "Main GitHub repository open kar rahi hoon."
                        LanguageType.ENGLISH -> "Opening GitHub repository."
                    }
                }
            } else {
                "GitHub tool is not available."
            }

            return CommandEvaluation(
                handledLocally = true,
                responseText = response,
                toolResult = toolResult,
                detectedLanguage = lang
            )
        }

        // Not handled by direct local hardware/app triggers -> pass to Gemini AI Service
        return null
    }
}
