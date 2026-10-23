package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.Screen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.images.ImageStudioScreen
import com.example.ui.screens.images.ImageStudioViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.tools.PhoneControlScreen
import com.example.ui.screens.tools.PhoneControlViewModel
import com.example.ui.screens.voice.VoiceScreen
import com.example.ui.screens.voice.VoiceViewModel
import com.example.ui.theme.CyberBlack

@Composable
fun MainApp(
    chatViewModel: ChatViewModel = viewModel(),
    voiceViewModel: VoiceViewModel = viewModel(),
    imageViewModel: ImageStudioViewModel = viewModel(),
    toolsViewModel: PhoneControlViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.CHAT) }

    // If user presses system back button when not on Chat, return to Chat
    if (currentScreen != Screen.CHAT) {
        BackHandler {
            currentScreen = Screen.CHAT
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        },
        containerColor = CyberBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBlack)
        ) {
            when (currentScreen) {
                Screen.CHAT -> {
                    ChatScreen(
                        viewModel = chatViewModel,
                        onNavigateToVoice = { currentScreen = Screen.VOICE },
                        onSpeakMessage = { text ->
                            voiceViewModel.voiceManager.speak(
                                text = text,
                                speed = chatViewModel.preferences.speechSpeed.value,
                                language = chatViewModel.preferences.speechLanguage.value
                            )
                        }
                    )
                }
                Screen.VOICE -> {
                    VoiceScreen(
                        viewModel = voiceViewModel
                    )
                }
                Screen.IMAGES -> {
                    ImageStudioScreen(
                        viewModel = imageViewModel
                    )
                }
                Screen.TOOLS -> {
                    PhoneControlScreen(
                        viewModel = toolsViewModel
                    )
                }
                Screen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
