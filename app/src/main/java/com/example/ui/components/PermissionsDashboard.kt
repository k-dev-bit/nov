package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaGreen

data class PermissionStatus(
    val name: String,
    val permissionKey: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val description: String
)

@Composable
fun PermissionsDashboard(
    modifier: Modifier = Modifier,
    onPermissionsUpdated: () -> Unit = {}
) {
    val context = LocalContext.current

    fun checkPermission(perm: String): Boolean {
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    var micGranted by remember { mutableStateOf(checkPermission(Manifest.permission.RECORD_AUDIO)) }
    var cameraGranted by remember { mutableStateOf(checkPermission(Manifest.permission.CAMERA)) }
    var contactsGranted by remember { mutableStateOf(checkPermission(Manifest.permission.READ_CONTACTS)) }
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else true
        )
    }

    fun refresh() {
        micGranted = checkPermission(Manifest.permission.RECORD_AUDIO)
        cameraGranted = checkPermission(Manifest.permission.CAMERA)
        contactsGranted = checkPermission(Manifest.permission.READ_CONTACTS)
        notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else true
        onPermissionsUpdated()
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refresh()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardDark)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NovaCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEVICE ACCESS & PERMISSIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val allGranted = micGranted && cameraGranted && contactsGranted && notifGranted
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (allGranted) Color(0xFF0F381E) else Color(0xFF381519),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (allGranted) NovaGreen else Color(0xFFFF6B6B)
                )
            ) {
                Text(
                    text = if (allGranted) "All Access Granted" else "Action Required",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allGranted) NovaGreen else Color(0xFFFF6B6B),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Permission Items List
        PermissionRow(
            name = "Microphone Access",
            desc = "Required for listening to voice commands and speech conversation",
            icon = Icons.Default.Mic,
            isGranted = micGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionRow(
            name = "Camera & Flashlight",
            desc = "Required for flashlight toggle and camera capture commands",
            icon = Icons.Default.CameraAlt,
            isGranted = cameraGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionRow(
            name = "Contacts",
            desc = "Required for voice dialing and phone call commands",
            icon = Icons.Default.Contacts,
            isGranted = contactsGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionRow(
            name = "Notifications",
            desc = "Required for background voice service and system alarms",
            icon = Icons.Default.Notifications,
            isGranted = notifGranted
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionRow(
            name = "Internet & Network",
            desc = "Online AI models, cloud fallback, and web queries",
            icon = Icons.Default.Wifi,
            isGranted = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Grant All Now & Open Device App Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val permissionsToRequest = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("grant_all_permissions_button")
            ) {
                Text("Grant Permissions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_device_app_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Device Settings", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    desc: String,
    icon: ImageVector,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D121D))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) NovaCyan else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = if (isGranted) "Granted" else "Denied",
            tint = if (isGranted) NovaGreen else Color(0xFFFF6B6B),
            modifier = Modifier.size(18.dp)
        )
    }
}
