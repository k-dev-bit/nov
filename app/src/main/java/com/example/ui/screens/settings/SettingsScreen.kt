package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.models.AiProvider
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = viewModel.preferences

    val assistantName by prefs.assistantName.collectAsState()
    val selectedProvider by prefs.selectedProvider.collectAsState()
    val isOfflineOnly by prefs.isOfflineOnly.collectAsState()
    val autoFallback by prefs.autoFallbackEnabled.collectAsState()

    val geminiKey by prefs.geminiApiKey.collectAsState()
    val groqKey by prefs.groqApiKey.collectAsState()
    val customUrl by prefs.customApiUrl.collectAsState()
    val customKey by prefs.customApiKey.collectAsState()
    val customModel by prefs.customModel.collectAsState()
    val localUrl by prefs.localServerUrl.collectAsState()
    val localModel by prefs.localModel.collectAsState()

    val testStatus by viewModel.testStatus.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()

    var showClearChatsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "SYSTEM SETTINGS",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "AI provider manager, coordination engine & security",
            fontSize = 12.sp,
            color = NovaCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Operating Mode Toggles
        SectionHeader("COORDINATION & MODES")

        SettingCard {
            // Auto Fallback Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto Fallback", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Automatically failover to secondary provider if primary model fails", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = autoFallback,
                    onCheckedChange = { prefs.setAutoFallback(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = NovaCyan, checkedTrackColor = NovaCyan.copy(alpha = 0.4f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Offline Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Offline-Only Mode", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Block all internet requests. Run only local phone actions and local server.", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = isOfflineOnly,
                    onCheckedChange = { prefs.setOfflineOnly(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = NovaPurple, checkedTrackColor = NovaPurple.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AI Provider Manager
        SectionHeader("AI PROVIDER MANAGER")

        // 1. Google Gemini
        ProviderCard(
            title = "1. Google Gemini",
            subtitle = "gemini-3.5-flash / gemini-3.1-pro-preview",
            isConfigured = prefs.getEffectiveGeminiKey().isNotBlank(),
            isTesting = isTesting == AiProvider.GEMINI.id,
            testResult = testStatus[AiProvider.GEMINI.id],
            onTest = { viewModel.testProvider(AiProvider.GEMINI) }
        ) {
            OutlinedTextField(
                value = geminiKey,
                onValueChange = { prefs.setGeminiApiKey(it) },
                label = { Text("Gemini API Key (optional override)") },
                placeholder = { Text("Auto-read from Secrets panel if empty") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Groq AI
        val groqModel by prefs.groqModel.collectAsState()
        ProviderCard(
            title = "2. Groq Cloud",
            subtitle = "Models: openai/gpt-oss-120b, Llama 3.3, DeepSeek",
            isConfigured = groqKey.isNotBlank(),
            isTesting = isTesting == AiProvider.GROQ.id,
            testResult = testStatus[AiProvider.GROQ.id],
            onTest = { viewModel.testProvider(AiProvider.GROQ) }
        ) {
            OutlinedTextField(
                value = groqKey,
                onValueChange = { prefs.setGroqApiKey(it) },
                label = { Text("Groq API Key (gsk_...)") },
                placeholder = { Text("Enter your Groq API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("SELECT GROQ MODEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaCyan)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val groqPresets = listOf(
                    "openai/gpt-oss-120b",
                    "llama-3.3-70b-versatile",
                    "llama-3.1-8b-instant",
                    "deepseek-r1-distill-llama-70b"
                )
                groqPresets.forEach { m ->
                    val isSelected = groqModel == m
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NovaCyan else Color(0xFF131D2E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NovaCyan else CyberBorder),
                        modifier = Modifier.clickable { prefs.setGroqModel(m) }
                    ) {
                        Text(
                            text = m,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = groqModel,
                onValueChange = { prefs.setGroqModel(it) },
                label = { Text("Active Model Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Pollinations AI
        ProviderCard(
            title = "3. Pollinations AI",
            subtitle = "Zero-setup free community AI models (Text & Image)",
            isConfigured = true,
            isTesting = isTesting == AiProvider.POLLINATIONS.id,
            testResult = testStatus[AiProvider.POLLINATIONS.id],
            onTest = { viewModel.testProvider(AiProvider.POLLINATIONS) }
        ) {
            Text(
                text = "✓ Always enabled. No API key required. Used automatically as a reliable fallback or default free provider.",
                color = NovaGreen,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Custom OpenAI API
        ProviderCard(
            title = "4. Custom OpenAI Compatible API",
            subtitle = "Any standard /v1/chat/completions endpoint",
            isConfigured = customUrl.isNotBlank(),
            isTesting = isTesting == AiProvider.CUSTOM.id,
            testResult = testStatus[AiProvider.CUSTOM.id],
            onTest = { viewModel.testProvider(AiProvider.CUSTOM) }
        ) {
            OutlinedTextField(
                value = customUrl,
                onValueChange = { prefs.setCustomApiConfig(it, customKey, customModel) },
                label = { Text("Base URL (e.g. https://api.openai.com/v1)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customKey,
                onValueChange = { prefs.setCustomApiConfig(customUrl, it, customModel) },
                label = { Text("API Key (optional)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customModel,
                onValueChange = { prefs.setCustomApiConfig(customUrl, customKey, it) },
                label = { Text("Model Name (e.g. gpt-4o-mini)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Local AI API (Ollama / LM Studio)
        ProviderCard(
            title = "5. Local Inference (Ollama / LM Studio)",
            subtitle = "Run models on your PC or home Wi-Fi server",
            isConfigured = localUrl.isNotBlank(),
            isTesting = isTesting == AiProvider.LOCAL.id,
            testResult = testStatus[AiProvider.LOCAL.id],
            onTest = { viewModel.testProvider(AiProvider.LOCAL) }
        ) {
            OutlinedTextField(
                value = localUrl,
                onValueChange = { prefs.setLocalServerConfig(it, localModel) },
                label = { Text("Server Address (e.g. http://192.168.1.50:11434)") },
                placeholder = { Text("http://<your-pc-ip>:11434") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = localModel,
                onValueChange = { prefs.setLocalServerConfig(localUrl, it) },
                label = { Text("Model Name (e.g. llama3.2, mistral, qwen)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Note: 'localhost' on Android refers to the phone itself. Use your PC's Wi-Fi IP address (e.g. 192.168.x.x) and ensure firewall allows inbound connections.",
                color = Color.LightGray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy & Data Management
        SectionHeader("DATA & PRIVACY CONTROLS")
        SettingCard {
            Button(
                onClick = { showClearChatsDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C0B0E), contentColor = Color(0xFFFF6B6B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete All Chat History")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    viewModel.clearSecrets()
                    Toast.makeText(context, "Stored API credentials cleared", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Remove All Stored API Keys", color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Access & Permissions Dashboard
        SectionHeader("DEVICE ACCESS & PERMISSIONS")
        com.example.ui.components.PermissionsDashboard()

        Spacer(modifier = Modifier.height(20.dp))

        // About NOVA AI
        SettingCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = NovaCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("NOVA AI Assistant v1.0", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hybrid Android AI Assistant with real-time multi-provider intelligence, local speech processing, and native phone control intents.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showClearChatsDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatsDialog = false },
            title = { Text("Delete All History?", color = Color(0xFFFF6B6B)) },
            text = { Text("This will permanently remove all saved conversations and local messages.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllChats()
                        showClearChatsDialog = false
                        Toast.makeText(context, "All chat history deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatsDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = CyberCardDark
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NovaCyan,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardDark)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun ProviderCard(
    title: String,
    subtitle: String,
    isConfigured: Boolean,
    isTesting: Boolean,
    testResult: Pair<Boolean, String>?,
    onTest: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardDark)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }

            Button(
                onClick = onTest,
                enabled = !isTesting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B273A), contentColor = NovaCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("test_provider_$title")
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = NovaCyan)
                } else {
                    Text("Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        content()

        if (testResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val (success, msg) = testResult
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (success) NovaGreen else Color(0xFFFF6B6B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = msg,
                    color = if (success) NovaGreen else Color(0xFFFF6B6B),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovaCyan,
    unfocusedBorderColor = CyberBorder,
    focusedContainerColor = Color(0xFF0F1522),
    unfocusedContainerColor = Color(0xFF0F1522),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
