package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deviceactions.ActionType
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberCardDarkVariant
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaGreen
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple

@Composable
fun PhoneControlScreen(
    viewModel: PhoneControlViewModel,
    modifier: Modifier = Modifier
) {
    val testInput by viewModel.testInput.collectAsState()
    val parsedCommand by viewModel.parsedCommand.collectAsState()
    val lastResult by viewModel.lastActionResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "PHONE CONTROL HUB",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Local device action execution & system automation",
            fontSize = 12.sp,
            color = NovaCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick System Toggles (Flashlight, Battery, Wi-Fi, Bluetooth)
        Text(
            text = "HARDWARE & SYSTEM TOGGLES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ControlTile(
                title = "Flashlight",
                subtitle = "Toggle torch",
                icon = Icons.Default.FlashlightOn,
                accentColor = NovaCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.executeAction(ActionType.TOGGLE_FLASHLIGHT, null) }
            )
            ControlTile(
                title = "Battery",
                subtitle = "Query status",
                icon = Icons.Default.BatteryFull,
                accentColor = NovaGreen,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.executeAction(ActionType.CHECK_BATTERY, null) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ControlTile(
                title = "Wi-Fi Settings",
                subtitle = "Network config",
                icon = Icons.Default.Wifi,
                accentColor = NovaBlue,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.executeAction(ActionType.OPEN_SETTINGS, "wifi") }
            )
            ControlTile(
                title = "Bluetooth",
                subtitle = "Connections",
                icon = Icons.Default.Bluetooth,
                accentColor = NovaPurple,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.executeAction(ActionType.OPEN_SETTINGS, "bluetooth") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // App Launch Shortcuts
        Text(
            text = "INSTALLED APPS LAUNCHER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaPurple,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTile("YouTube", Icons.Default.PlayArrow, Color(0xFFFF3333), Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.OPEN_APP, "youtube")
            }
            AppTile("Chrome", Icons.Default.Language, NovaCyan, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.OPEN_APP, "chrome")
            }
            AppTile("Camera", Icons.Default.CameraAlt, NovaGreen, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.OPEN_APP, "camera")
            }
            AppTile("Maps", Icons.Default.Map, NovaBlue, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.OPEN_APP, "maps")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTile("Dialer", Icons.Default.Phone, NovaCyan, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.DIAL_PHONE, "")
            }
            AppTile("SMS Draft", Icons.Default.Chat, NovaPurple, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.SEND_SMS, "Hello from NOVA!")
            }
            AppTile("Alarm", Icons.Default.Alarm, Color(0xFFFFB703), Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.SET_ALARM, "7:00 AM")
            }
            AppTile("Settings", Icons.Default.Settings, Color.LightGray, Modifier.weight(1f)) {
                viewModel.executeAction(ActionType.OPEN_SETTINGS, "main")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // NLP Command Sandbox (English, Hindi, Hinglish)
        Text(
            text = "NATURAL LANGUAGE COMMAND PARSER (HINDI / ENGLISH / HINGLISH)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaGreen,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = testInput,
            onValueChange = { viewModel.updateInput(it) },
            placeholder = { Text("Try: 'YouTube kholo', 'Torch jalao', 'Battery kitni hai'...", color = Color.Gray, fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nlp_command_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaGreen,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = CyberCardDark,
                unfocusedContainerColor = CyberCardDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Parsed Result Card
        if (parsedCommand != null) {
            val cmd = parsedCommand!!
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardDarkVariant)
                    .border(1.dp, NovaGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intent: ${cmd.confidenceDescription}",
                        fontWeight = FontWeight.Bold,
                        color = NovaGreen,
                        fontSize = 13.sp
                    )
                    if (cmd.isDeviceAction) {
                        Button(
                            onClick = { viewModel.executeParsedCommand() },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("execute_nlp_button")
                        ) {
                            Text("Execute Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Action: ${cmd.actionType?.label ?: "Cloud AI Task"} | Target: ${cmd.target ?: "N/A"}",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        // Live Execution Result
        if (lastResult != null) {
            val res = lastResult!!
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (res.success) Color(0xFF0D2818) else Color(0xFF2C0B0E),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (res.success) NovaGreen else Color(0xFFFF3366)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (res.success) "SUCCESS: Execution Verified" else "EXECUTION ISSUE",
                        fontWeight = FontWeight.Bold,
                        color = if (res.success) NovaGreen else Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = res.message,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CyberCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = modifier
            .testTag("control_tile_$title")
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AppTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CyberCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
