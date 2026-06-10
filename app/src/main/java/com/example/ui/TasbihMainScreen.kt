package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DhikrEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihMainScreen(viewModel: TasbihViewModel) {
    val allDhikrs by viewModel.allDhikrs.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val activeDhikr by viewModel.activeDhikr.collectAsStateWithLifecycle()
    
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val isAutoNextEnabled by viewModel.isAutoNextEnabled.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showHistorySection by remember { mutableStateOf(false) }

    val view = LocalView.current
    
    // Rich gradient background mapping reflecting Elegant Dark
    val bgGradient = Brush.verticalGradient(
        listOf(DeepDarkBg, Color(0xFF0F0E12))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("tasbih_main_scaffold"),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tasbeeh Counter",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TextLightHigh,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "DHIKR COMPANION",
                            fontWeight = FontWeight.SemiBold,
                            color = TextMutedMedium,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    // History Drawer Toggle button
                    IconButton(
                        onClick = { showHistorySection = !showHistorySection },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(44.dp)
                            .background(CardDarkBg, CircleShape)
                            .testTag("history_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (showHistorySection) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Toggle History Logs",
                            tint = AccentLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Arabic scripture view if present and active
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    activeDhikr?.let { dhikr ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (dhikr.arabic.isNotEmpty()) {
                                Text(
                                    text = dhikr.arabic,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 42.sp
                                    ),
                                    color = AccentLavender,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = dhikr.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextLightHigh,
                                textAlign = TextAlign.Center
                            )
                            if (dhikr.translation.isNotEmpty()) {
                                Text(
                                    text = dhikr.translation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMutedMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp, start = 16.dp, end = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Center Massive elegant Circular ring progress
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    activeDhikr?.let { dhikr ->
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .shadow(12.dp, CircleShape, clip = true)
                                .background(CardDarkBg)
                                .clip(CircleShape)
                                .testTag("tasbih_tap_button")
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = AccentLavender),
                                    onClick = {
                                        if (isSoundEnabled) {
                                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                        }
                                        viewModel.incrementCount()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                val strokeWidth = 5.dp.toPx()
                                val size = size
                                val radius = (size.minDimension - strokeWidth) / 2
                                val center = Offset(size.width / 2, size.height / 2)

                                // Silent base ring
                                drawCircle(
                                    color = Color(0xFF1E1C22),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )

                                // Active target loop angle progress
                                val progressRatio = if (dhikr.target > 0) {
                                    (dhikr.count.toFloat() / dhikr.target.toFloat()).coerceIn(0f, 1f)
                                } else {
                                    0f
                                }

                                if (progressRatio > 0f) {
                                    drawArc(
                                        color = AccentLavender,
                                        startAngle = -90f,
                                        sweepAngle = progressRatio * 360f,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(
                                            width = strokeWidth + 2.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }

                            // Numbers area
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dhikr.count.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Light,
                                        fontSize = 76.sp,
                                        letterSpacing = (-2).sp
                                    ),
                                    color = AccentLavender
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x1F938F99))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(AccentLavender, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (dhikr.target > 0) "Goal: ${dhikr.target}" else "Unlimited",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = AccentLavender
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-stats Grid blocks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "LAPS DONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedMedium,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = activeDhikr?.totalCompletedCycles?.toString() ?: "0",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextLightHigh,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "TOTAL PRESSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedMedium,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = activeDhikr?.totalPressed?.toString() ?: "0",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextLightHigh,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Toggles Toolbar + Reset controller
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset click button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("tasbih_reset_button").clickable { viewModel.resetCount() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(CardDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = TextLightHigh.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "RESET",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Vibrate Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("vibration_toggle").clickable { viewModel.toggleVibration() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isVibrationEnabled) MutedLavender else CardDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVibrationEnabled) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = "Vibrate",
                                tint = if (isVibrationEnabled) AccentLavender else TextLightHigh.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "VIBE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isVibrationEnabled) AccentLavender else TextMutedMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sound Toggle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("sound_toggle").clickable { viewModel.toggleSound() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isSoundEnabled) MutedLavender else CardDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.Star else Icons.Default.Info,
                                contentDescription = "Sound",
                                tint = if (isSoundEnabled) AccentLavender else TextLightHigh.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SOUND",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSoundEnabled) AccentLavender else TextMutedMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Auto Next Loop
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("autonext_toggle").clickable { viewModel.toggleAutoNext() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isAutoNextEnabled) MutedLavender else CardDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoNextEnabled) Icons.Default.PlayArrow else Icons.Default.Close,
                                contentDescription = "AutoNext",
                                tint = if (isAutoNextEnabled) AccentLavender else TextLightHigh.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "LOOP",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAutoNextEnabled) AccentLavender else TextMutedMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Presets horizontal lists area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preset Dhikrs",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextLightHigh,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .background(CardDarkBg, CircleShape)
                                .testTag("tasbih_add_dhikr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Dhikr",
                                tint = AccentLavender,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(allDhikrs) { dhikr ->
                            val isSelected = activeDhikr?.id == dhikr.id
                            Surface(
                                modifier = Modifier
                                    .testTag("preset_${dhikr.id}")
                                    .clickable { viewModel.setActiveDhikr(dhikr.id) },
                                color = if (isSelected) MutedLavender else CardDarkBg,
                                shape = RoundedCornerShape(16.dp),
                                border = if (isSelected) BorderStroke(1.dp, AccentLavender) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dhikr.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) AccentLavender else TextLightHigh
                                    )
                                    if (!dhikr.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.deleteDhikr(dhikr.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Persistent elegant Click tap bar at bottom
                Button(
                    onClick = {
                        if (isSoundEnabled) {
                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                        }
                        viewModel.incrementCount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentLavender,
                        contentColor = TextContrastDark
                    ),
                    shape = RoundedCornerShape(32.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp, top = 8.dp)
                        .testTag("tasbih_assist_tap")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tap Count Plus",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAP TO COUNT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Slide history view log drawer panel on top of layout if triggered
            if (showHistorySection) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(16.dp),
                    color = DeepDarkBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📜 History Logs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AccentLavender
                            )
                            IconButton(
                                onClick = { showHistorySection = false },
                                modifier = Modifier.background(CardDarkBg, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = AccentLavender
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = CardDarkBg
                        )

                        if (allHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No devotions recorded yet.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextMutedMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Complete loops to add logs automatically.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMutedMedium.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(allHistory) { history ->
                                    val dateStr = remember(history.date) {
                                        val format = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
                                        format.format(Date(history.date))
                                    }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = CardDarkBg
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = history.dhikrName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextLightHigh
                                                )
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMutedMedium
                                                )
                                            }
                                            Text(
                                                text = "+ ${history.count}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentLavender
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.clearHistory() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFBA1A1A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Text("Clear History Logs", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Custom Dhikr Dialog Popup
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = CardDarkBg,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ Custom Tasbeeh Add",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentLavender,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    var name by remember { mutableStateOf("") }
                    var arabic by remember { mutableStateOf("") }
                    var translation by remember { mutableStateOf("") }
                    var targetValue by remember { mutableStateOf("33") }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (e.g. Darood)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLightHigh,
                            unfocusedTextColor = TextLightHigh,
                            focusedBorderColor = AccentLavender,
                            unfocusedBorderColor = TextMutedMedium.copy(alpha = 0.4f),
                            focusedLabelColor = AccentLavender,
                            unfocusedLabelColor = TextMutedMedium
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = arabic,
                        onValueChange = { arabic = it },
                        label = { Text("Arabic Text (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLightHigh,
                            unfocusedTextColor = TextLightHigh,
                            focusedBorderColor = AccentLavender,
                            unfocusedBorderColor = TextMutedMedium.copy(alpha = 0.4f),
                            focusedLabelColor = AccentLavender,
                            unfocusedLabelColor = TextMutedMedium
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        label = { Text("Transliteration (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLightHigh,
                            unfocusedTextColor = TextLightHigh,
                            focusedBorderColor = AccentLavender,
                            unfocusedBorderColor = TextMutedMedium.copy(alpha = 0.4f),
                            focusedLabelColor = AccentLavender,
                            unfocusedLabelColor = TextMutedMedium
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = { Text("Target Count (0 for unlimited)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextLightHigh,
                            unfocusedTextColor = TextLightHigh,
                            focusedBorderColor = AccentLavender,
                            unfocusedBorderColor = TextMutedMedium.copy(alpha = 0.4f),
                            focusedLabelColor = AccentLavender,
                            unfocusedLabelColor = TextMutedMedium
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Dismiss", color = TextMutedMedium)
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val targetInt = targetValue.toIntOrNull()?.coerceAtLeast(0) ?: 33
                                    viewModel.addCustomDhikr(
                                        name = name,
                                        arabic = arabic,
                                        translation = translation,
                                        target = targetInt
                                    )
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentLavender,
                                contentColor = TextContrastDark
                            )
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
