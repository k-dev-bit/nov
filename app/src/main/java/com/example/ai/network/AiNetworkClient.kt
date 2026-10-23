package com.example.ai.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object AiNetworkClient {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun callGemini(
        apiKey: String,
        model: String,
        messages: List<Pair<String, String>>,
        imageBase64: String? = null,
        systemPrompt: String = "You are NOVA, a smart, helpful, concise AI personal assistant."
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Google Gemini API key is required. Add it in Settings or secrets.")
        }
        val safeModel = if (model.isBlank()) "gemini-3.5-flash" else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        
        // System instruction
        if (systemPrompt.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysObj)
        }

        // Contents
        val contentsArray = JSONArray()
        for (i in messages.indices) {
            val (role, text) = messages[i]
            val mappedRole = if (role == "assistant") "model" else "user"
            val contentObj = JSONObject()
            contentObj.put("role", mappedRole)
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", text))
            if (i == messages.lastIndex && !imageBase64.isNullOrBlank() && mappedRole == "user") {
                val inlineObj = JSONObject()
                inlineObj.put("mimeType", "image/jpeg")
                inlineObj.put("data", imageBase64)
                partsArray.put(JSONObject().put("inlineData", inlineObj))
            }
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }
        rootJson.put("contents", contentsArray)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            throw IllegalStateException("Gemini API error ($safeModel): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw IllegalStateException("Gemini returned empty response candidates.")
        }
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        if (parts == null || parts.length() == 0) {
            throw IllegalStateException("Gemini candidate contains no parts.")
        }
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            sb.append(part.optString("text", ""))
        }
        sb.toString().trim()
    }

    suspend fun callOpenAiCompatible(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<Pair<String, String>>,
        systemPrompt: String = "You are NOVA, a smart, helpful, concise AI personal assistant."
    ): String = withContext(Dispatchers.IO) {
        val cleanBase = baseUrl.trim().removeSuffix("/")
        val endpoint = if (cleanBase.endsWith("/v1")) "$cleanBase/chat/completions" 
                       else if (cleanBase.contains("/chat/completions")) cleanBase 
                       else "$cleanBase/v1/chat/completions"

        val rootJson = JSONObject()
        rootJson.put("model", model)

        val msgArray = JSONArray()
        if (systemPrompt.isNotBlank()) {
            msgArray.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        for ((role, text) in messages) {
            msgArray.put(JSONObject().put("role", role).put("content", text))
        }
        rootJson.put("messages", msgArray)
        rootJson.put("temperature", 0.7)

        val reqBuilder = Request.Builder()
            .url(endpoint)
            .post(rootJson.toString().toRequestBody(JSON_MEDIA_TYPE))

        if (apiKey.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val response = client.newCall(reqBuilder.build()).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            throw IllegalStateException("API error: $errorMsg")
        }

        val json = JSONObject(responseBody)
        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            throw IllegalStateException("No choices returned from model.")
        }
        val firstChoice = choices.getJSONObject(0)
        val msgObj = firstChoice.optJSONObject("message")
        msgObj?.optString("content")?.trim() ?: throw IllegalStateException("Choice contains no text.")
    }

    suspend fun callPollinationsText(
        prompt: String,
        messages: List<Pair<String, String>>,
        model: String = "openai"
    ): String = withContext(Dispatchers.IO) {
        // Pollinations supports OpenAI-compatible endpoint without API key!
        val url = "https://text.pollinations.ai/"
        val rootJson = JSONObject()
        rootJson.put("model", if (model.isNotBlank()) model else "openai")
        val msgArray = JSONArray()
        msgArray.put(JSONObject().put("role", "system").put("content", "You are NOVA, a smart, helpful personal AI assistant. Support Hindi, English, and Hinglish naturally."))
        
        for ((role, text) in messages) {
            msgArray.put(JSONObject().put("role", role).put("content", text))
        }
        rootJson.put("messages", msgArray)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty().trim()

        if (!response.isSuccessful) {
            // Fallback to simple GET endpoint if POST payload had issues
            val encoded = URLEncoder.encode(prompt, "UTF-8")
            val getReq = Request.Builder()
                .url("https://text.pollinations.ai/$encoded?model=openai")
                .get()
                .build()
            val getResp = client.newCall(getReq).execute()
            val getBody = getResp.body?.string().orEmpty().trim()
            if (!getResp.isSuccessful || getBody.isBlank()) {
                throw IllegalStateException("Pollinations returned HTTP ${response.code}")
            }
            return@withContext getBody
        }

        if (responseBody.isBlank()) {
            throw IllegalStateException("Pollinations returned empty answer.")
        }
        // Pollinations sometimes returns plain text or JSON
        if (responseBody.startsWith("{") && responseBody.contains("\"content\"")) {
            try {
                val json = JSONObject(responseBody)
                val choices = json.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content")
                    if (!content.isNullOrBlank()) return@withContext content
                }
            } catch (e: Exception) {
                // Return raw body
            }
        }
        responseBody
    }

    suspend fun testConnection(
        provider: com.example.ai.models.AiProvider,
        apiKey: String,
        baseUrl: String,
        model: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            when (provider) {
                com.example.ai.models.AiProvider.GEMINI -> {
                    if (apiKey.isBlank()) return@withContext Pair(false, "API key is missing")
                    val result = callGemini(apiKey, model, listOf("user" to "Ping. Reply with 'Pong'"))
                    Pair(true, "Connected: ${result.take(50)}")
                }
                com.example.ai.models.AiProvider.GROQ -> {
                    if (apiKey.isBlank()) return@withContext Pair(false, "Groq API key is missing")
                    val result = callOpenAiCompatible(
                        "https://api.groq.com/openai/v1",
                        apiKey,
                        if (model.isNotBlank()) model else "llama-3.3-70b-versatile",
                        listOf("user" to "Ping. Reply with 'Pong'")
                    )
                    Pair(true, "Connected: ${result.take(50)}")
                }
                com.example.ai.models.AiProvider.POLLINATIONS -> {
                    val result = callPollinationsText("Hi", listOf("user" to "Ping"))
                    Pair(true, "Connected! Pollinations active.")
                }
                com.example.ai.models.AiProvider.CUSTOM -> {
                    val result = callOpenAiCompatible(
                        baseUrl,
                        apiKey,
                        model,
                        listOf("user" to "Ping")
                    )
                    Pair(true, "Connected: ${result.take(50)}")
                }
                com.example.ai.models.AiProvider.LOCAL -> {
                    val result = callOpenAiCompatible(
                        baseUrl,
                        "",
                        model,
                        listOf("user" to "Ping")
                    )
                    Pair(true, "Connected to Local inference: ${result.take(50)}")
                }
                else -> Pair(true, "Auto Coordinator active")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection test failed")
        }
    }
}
