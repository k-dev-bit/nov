package com.example.ui.screens.images

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.NovaBlue
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple

@Composable
fun ImageStudioScreen(
    viewModel: ImageStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompt by viewModel.prompt.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val imageUrl by viewModel.generatedImageUrl.collectAsState()
    val referenceImage by viewModel.referenceImageUrl.collectAsState()
    val history by viewModel.history.collectAsState()

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.setReferenceImage(uri?.toString())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "IMAGE STUDIO",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "High resolution AI neural generation",
                    fontSize = 12.sp,
                    color = NovaCyan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131D2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaCyan.copy(alpha = 0.5f))
                ) {
                    Text("Upload", fontSize = 12.sp, color = NovaCyan)
                }

                Button(
                    onClick = { viewModel.randomizePrompt() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162032)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NovaPurple.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NovaPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspire Me", fontSize = 12.sp, color = Color.White)
                }
            }
        }

        if (referenceImage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCardDark)
                    .border(1.dp, NovaCyan, RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                AsyncImage(
                    model = referenceImage,
                    contentDescription = "Uploaded reference",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reference image loaded", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { viewModel.setReferenceImage(null) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prompt Input Field
        OutlinedTextField(
            value = prompt,
            onValueChange = { viewModel.setPrompt(it) },
            placeholder = { Text("Describe what you want to see...", color = Color.Gray, fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_prompt_input"),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaCyan,
                unfocusedBorderColor = CyberBorder,
                focusedContainerColor = CyberCardDark,
                unfocusedContainerColor = CyberCardDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Style Selector
        Text(
            text = "CHOOSE STYLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaCyan,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageStyle.entries.forEach { style ->
                val isSelected = selectedStyle == style
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) NovaCyan else Color(0xFF131D2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NovaCyan else CyberBorder),
                    modifier = Modifier
                        .testTag("style_chip_${style.name}")
                        .clickable { viewModel.setStyle(style) }
                ) {
                    Text(
                        text = style.displayName,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Aspect Ratio Selector
        Text(
            text = "ASPECT RATIO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NovaPurple,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val ratios = listOf("1:1" to "Square (1:1)", "16:9" to "Landscape (16:9)", "9:16" to "Portrait (9:16)", "4:3" to "Standard (4:3)")
            ratios.forEach { (ratioKey, ratioLabel) ->
                val isSelected = selectedAspectRatio == ratioKey
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) NovaPurple else Color(0xFF131D2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NovaPurple else CyberBorder),
                    modifier = Modifier.clickable { viewModel.setAspectRatio(ratioKey) }
                ) {
                    Text(
                        text = ratioLabel,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Button
        Button(
            onClick = { viewModel.generateImage() },
            enabled = !isGenerating && prompt.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_image_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Synthesizing Image...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Artwork", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Image Preview & Actions
        if (imageUrl != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberCardDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Generated artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .testTag("generated_image_preview")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.saveImageToGallery() },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_image_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Look at this AI artwork generated with NOVA AI:\n$imageUrl")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = NovaCyan)
                        }

                        IconButton(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Prompt", prompt))
                                Toast.makeText(context, "Prompt copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy prompt", tint = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
