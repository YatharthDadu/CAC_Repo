package com.example.volunteerhub.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.model.VolunteerEvent

@Composable
fun XelloScreen(
    event: VolunteerEvent,
    onBack: () -> Unit,
    onGeneratePdf: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var aiReflection by remember {
        mutableStateOf(
            "Collaborated with hospital clinical staff to assemble 250+ pediatric care kits under strict time constraints, ensuring protocol readiness and directly supporting community wellness."
        )
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied: $label", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Activity Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Activity Details Card (Mockup Image 3)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = event.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.organizationName,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${event.eventDate} • 4.0 hours",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF34C759)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VERIFIED ✓",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier
                                .width(90.dp)
                                .height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✍️ Signed", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }

                        Column {
                            Text(
                                text = "Marta Alvarez",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "(Site Coordinator)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Xello Bridge Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Xello Bridge (Fast Copy)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF007AFF).copy(alpha = 0.12f))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "📋 Xello & Portals Copy Drawer",
                                color = Color(0xFF007AFF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        FastCopyRow(
                            title = "Organization",
                            value = event.organizationName,
                            onCopy = { copyToClipboard("Organization", event.organizationName) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FastCopyRow(
                            title = "Hours & Date",
                            value = "4.0 hours • ${event.eventDate}",
                            onCopy = { copyToClipboard("Hours", "4.0") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        FastCopyRow(
                            title = "Supervisor Email",
                            value = "m.alvarez@medicalcity.org",
                            onCopy = { copyToClipboard("Email", "m.alvarez@medicalcity.org") }
                        )
                    }
                }
            }

            // AI Impact Reflection Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AI Impact Reflection (STAR Method)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Reflection Generator",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007AFF),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = aiReflection,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(14.dp),
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    aiReflection = "Coordinated volunteer intake logistics and comfort kit preparation for 150+ inpatient admissions, enhancing clinic response efficiency."
                                },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("✨ Regenerate")
                            }

                            Button(
                                onClick = { copyToClipboard("AI Reflection", aiReflection) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                            ) {
                                Text("Copy Paragraph")
                            }
                        }
                    }
                }
            }

            // Big Blue PDF Generator Button (Mockup Image 3 Bottom)
            Button(
                onClick = onGeneratePdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📄", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Verified Resume (PDF)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FastCopyRow(
    title: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        IconButton(
            onClick = onCopy,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF007AFF).copy(alpha = 0.12f), CircleShape)
        ) {
            Text("📋", fontSize = 14.sp)
        }
    }
}
