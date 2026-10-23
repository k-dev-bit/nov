package com.example.ui.screens.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.PermissionsDashboard
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.voice.VoiceState

@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val assistantReply by viewModel.assistantReply.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val selectedLanguage by viewModel.preferences.speechLanguage.collectAsState()
    val speechSpeed by viewModel.preferences.speechSpeed.collectAsState()
    val isContinuous by viewModel.preferences.continuousVoice.collectAsState()
    val isBackgroundVoice by viewModel.preferences.backgroundVoice.collectAsState()

    var testSpeechInput by remember { mutableStateOf("") }
    var showPermissionsCard by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasMicPermission = results[Manifest.permission.RECORD_AUDIO] == true
        if (hasMicPermission) {
            viewModel.startListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "NOVA VOICE",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "Real-time multilingual voice assistant",
                fontSize = 12.sp,
                color = NovaCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Permission Warning Banner if Microphone not granted
            if (!hasMicPermission) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF381519),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B6B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Microphone Access Required",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NOVA needs mic access to hear your voice commands. Tap Grant Access below or open Android Settings.",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val perms = mutableListOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    micPermissionLauncher.launch(perms.toTypedArray())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("voice_grant_mic_button")
                            ) {
                                Text("Grant Permission", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                            ) {
                                Text("App Settings", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Mode Toggles (Continuous & Background)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Continuous Voice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleContinuousVoice() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = if (isContinuous) NovaGreen else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Continuous Loop",
                            color = if (isContinuous) NovaGreen else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isContinuous) "Hands-free active" else "Auto-listen off",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                // Background Voice Service
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleBackgroundVoice() }
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isBackgroundVoice) NovaCyan else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Background Mode",
                            color = if (isBackgroundVoice) NovaCyan else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isBackgroundVoice) "Active service" else "Tap to enable",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Language Selector Chips (English, Hindi, Hinglish)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val languages = listOf(
                    "en-US" to "English",
                    "hi-IN" to "हिन्दी (Hindi)",
                    "en-IN" to "Hinglish"
                )
                languages.forEach { (code, label) ->
                    val isSelected = selectedLanguage == code
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NovaCyan else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NovaCyan else CyberBorder
                        ),
                        modifier = Modifier
                            .testTag("lang_chip_$code")
                            .clickable { viewModel.setLanguage(code) }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Voice Orb
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            VoiceOrb(
                state = voiceState,
                audioRms = audioRms,
                size = 170.dp,
                onClick = {
                    if (!hasMicPermission) {
                        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        micPermissionLauncher.launch(perms.toTypedArray())
                    } else {
                        viewModel.toggleListening()
                    }
                },
                modifier = Modifier.testTag("voice_orb_interactive")
            )

            Spacer(modifier = Modifier.height(10.dp))

            val stateText = when (voiceState) {
                VoiceState.LISTENING -> "Listening to microphone..."
                VoiceState.THINKING -> "NOVA is processing..."
                VoiceState.SPEAKING -> "Speaking answer..."
                VoiceState.ERROR -> "Microphone issue. Tap to retry"
                VoiceState.IDLE -> if (isContinuous) "Continuous hands-free ready" else "Tap orb to speak"
            }
            val stateColor = when (voiceState) {
                VoiceState.LISTENING -> NovaCyan
                VoiceState.THINKING -> NovaPurple
                VoiceState.SPEAKING -> NovaPink
                VoiceState.ERROR -> Color(0xFFFF3366)
                VoiceState.IDLE -> if (isContinuous) NovaGreen else Color.Gray
            }

            Text(
                text = stateText,
                color = stateColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (!errorMsg.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMsg ?: "",
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Transcript & Reply Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberCardDark)
                .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "HEARD SPEECH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NovaCyan,
                letterSpacing = 1.sp
            )
            val displayText = when {
                voiceState == VoiceState.LISTENING && partialText.isNotBlank() -> partialText
                transcript.isNotBlank() -> transcript
                else -> "Say: 'Open YouTube', 'Volume badhao', 'Flashlight on', 'Check battery'..."
            }
            Text(
                text = displayText,
                color = if (transcript.isNotBlank() || partialText.isNotBlank()) Color.White else Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (assistantReply.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "NOVA ANSWER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaPurple,
                    letterSpacing = 1.sp
                )
                Text(
                    text = assistantReply,
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Command Input & Simulation Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = testSpeechInput,
                onValueChange = { testSpeechInput = it },
                placeholder = { Text("Speak or test command...", color = Color.Gray, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_command_test_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberCardDark,
                    unfocusedContainerColor = CyberCardDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (testSpeechInput.isNotBlank()) {
                        viewModel.voiceManager.simulateSpeech(testSpeechInput)
                        testSpeechInput = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(NovaCyan, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Test voice", tint = Color.Black, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Voice Controls (Stop speech, Speed slider)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Voice Speed: ${(speechSpeed * 10).toInt() / 10f}x",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
                if (voiceState == VoiceState.SPEAKING) {
                    Button(
                        onClick = { viewModel.stopSpeaking() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("voice_stop_speaking_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop Speaking", fontSize = 12.sp)
                    }
                }
            }

            Slider(
                value = speechSpeed,
                onValueChange = { viewModel.setSpeechSpeed(it) },
                valueRange = 0.6f..1.6f,
                steps = 4,
                colors = SliderDefaults.colors(
                    thumbColor = NovaCyan,
                    activeTrackColor = NovaCyan,
                    inactiveTrackColor = CyberBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle Full Permissions Dashboard
        OutlinedButton(
            onClick = { showPermissionsCard = !showPermissionsCard },
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (showPermissionsCard) "Hide Permissions Dashboard" else "Manage All Device Permissions",
                fontSize = 12.sp,
                color = NovaCyan
            )
        }

        if (showPermissionsCard) {
            Spacer(modifier = Modifier.height(10.dp))
            PermissionsDashboard(
                onPermissionsUpdated = {
                    hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                }
            )
        }
    }
}
