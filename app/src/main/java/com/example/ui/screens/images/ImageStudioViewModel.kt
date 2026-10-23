package com.example.ui.screens.images

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.OutputStream
import java.net.URL
import java.net.URLEncoder

enum class ImageStyle(val displayName: String, val promptEnhancer: String) {
    PHOTOREALISTIC("Photorealistic", "ultra realistic, 8k resolution, highly detailed, photorealistic photography, sharp focus"),
    ANIME("Anime", "masterpiece anime artwork, vivid colors, Studio Ghibli and Makoto Shinkai aesthetic, clean lines"),
    THREE_D("3D Render", "octane 3D render, raytracing, cinematic volumetric lighting, unreal engine 5, 8k"),
    CINEMATIC("Cinematic", "cinematic film still, dramatic lighting, anamorphic lens, shallow depth of field, 35mm"),
    DIGITAL_ART("Digital Art", "futuristic digital concept art, vibrant neon cyber palette, artstation trending"),
    CARTOON("Cartoon", "clean animated cartoon illustration, playful expression, expressive character design"),
    WATERCOLOR("Watercolor", "delicate watercolor painting, artistic paper texture, soft pastel gradients"),
    PIXEL_ART("Pixel Art", "16-bit retro pixel art, detailed pixel sprite aesthetic, crisp edges"),
    FANTASY("Fantasy", "epic high fantasy illustration, ethereal magical glow, mythical aura"),
    MINIMALIST("Minimalist", "clean minimalist vector design, sleek geometric shapes, elegant negative space")
}

class ImageStudioViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = PreferencesManager(application)
    private val httpClient = OkHttpClient()

    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _negativePrompt = MutableStateFlow("")
    val negativePrompt: StateFlow<String> = _negativePrompt.asStateFlow()

    private val _selectedStyle = MutableStateFlow(ImageStyle.PHOTOREALISTIC)
    val selectedStyle: StateFlow<ImageStyle> = _selectedStyle.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("1:1") // "1:1", "16:9", "9:16", "4:3"
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generatedImageUrl = MutableStateFlow<String?>(null)
    val generatedImageUrl: StateFlow<String?> = _generatedImageUrl.asStateFlow()

    private val _referenceImageUrl = MutableStateFlow<String?>(null)
    val referenceImageUrl: StateFlow<String?> = _referenceImageUrl.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    fun setReferenceImage(uri: String?) {
        _referenceImageUrl.value = uri
    }

    fun setPrompt(text: String) {
        _prompt.value = text
    }

    fun setNegativePrompt(text: String) {
        _negativePrompt.value = text
    }

    fun setStyle(style: ImageStyle) {
        _selectedStyle.value = style
    }

    fun setAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun generateImage() {
        val userPrompt = _prompt.value.trim()
        if (userPrompt.isBlank()) return

        _isGenerating.value = true
        _generatedImageUrl.value = null

        viewModelScope.launch {
            try {
                val enhancedPrompt = "$userPrompt, ${_selectedStyle.value.promptEnhancer}"
                val encodedPrompt = withContext(Dispatchers.IO) {
                    URLEncoder.encode(enhancedPrompt, "UTF-8")
                }

                val (width, height) = when (_selectedAspectRatio.value) {
                    "16:9" -> 1024 to 576
                    "9:16" -> 576 to 1024
                    "4:3" -> 1024 to 768
                    else -> 1024 to 1024
                }

                // Pollinations image generation endpoint with Flux model
                val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&model=flux&nologo=true"
                _generatedImageUrl.value = imageUrl
                _history.value = listOf(imageUrl) + _history.value.take(15)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Failed to generate: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun saveImageToGallery() {
        val url = _generatedImageUrl.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val bytes = response.body?.bytes()
                if (bytes == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Failed to download image", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val filename = "NOVA_AI_${System.currentTimeMillis()}.jpg"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NOVA_AI")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val resolver = getApplication<Application>().contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    val out: OutputStream? = resolver.openOutputStream(uri)
                    out?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Image saved to Gallery!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun randomizePrompt() {
        val presets = listOf(
            "Futuristic neon cyberpunk supercar driving through a rainy Tokyo street at night",
            "A tranquil bioluminescent alien forest with glowing neon flowers and crystal streams",
            "Heroic mecha warrior standing on top of a skyscraper overlooking a futuristic metropolis",
            "Cute astronaut red panda exploring a glowing Martian cave with floating crystals",
            "Majestic crystal dragon soaring above a misty mountain sanctuary at sunset",
            "Steampunk airship floating above golden clouds with glowing bronze gears"
        )
        _prompt.value = presets.random()
    }
}
