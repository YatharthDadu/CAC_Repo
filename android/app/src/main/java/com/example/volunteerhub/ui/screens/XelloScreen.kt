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
    isRsvpd: Boolean = false,
    onToggleRsvp: () -> Unit = {},
    onBack: () -> Unit,
    onGeneratePdf: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val supervisorEmail = remember(event) {
        val orgClean = event.organizationName.lowercase().filter { it.isLetter() }.take(12)
        "coordinator@${if (orgClean.isNotEmpty()) orgClean else "volunteer"}.org"
    }

    fun getEventStarReflection(event: VolunteerEvent, variant: Int = 0): String {
        val text = "${event.title} ${event.organizationName} ${event.category}".lowercase()
        return when {
            text.contains("stem") || text.contains("tech") || text.contains("code") || text.contains("robot") ->
                if (variant == 0) "Mentored 15+ student learners through algorithmic logic exercises, Scratch coding challenges, and hands-on robotics debugging, enhancing youth computational literacy in North Texas."
                else "Diagnosed syntax errors, guided interactive Python coding sprints, and inspired elementary students to explore STEM and software development."
            text.contains("maker") || text.contains("3d print") ->
                if (variant == 0) "Facilitated teen makerspace workshops, assisting younger participants in 3D CAD modeling, filament slicing, and rapid prototyping while ensuring lab safety protocols."
                else "Managed digital maker lab equipment, instructed youth on 3D printer calibration, and facilitated STEM design challenges."
            text.contains("ai") || text.contains("data") ->
                if (variant == 0) "Assisted community members with digital literacy, testing accessibility features and AI prompt guidance to bridge regional technology adoption gaps."
                else "Evaluated user prompts, logged feedback metrics, and helped seniors navigate modern technology tools with patience and precision."
            text.contains("park") || text.contains("trail") || text.contains("plant") ->
                "Collaborated with environmental conservation teams to mulch 1.5 miles of nature trails and plant native saplings, directly improving Collin County park ecosystems."
            text.contains("food") || text.contains("pack") || text.contains("meal") ->
                "Assembled and quality-inspected 350+ emergency nutrition packages with logistics partners, accelerating regional food distribution for Collin County families."
            else ->
                "Collaborated with hospital clinical staff to assemble 250+ pediatric care kits under strict time constraints, ensuring protocol readiness and directly supporting community wellness."
        }
    }

    var reflectionVariant by remember { mutableStateOf(0) }
    var aiReflection by remember(event, reflectionVariant) {
        mutableStateOf(getEventStarReflection(event, reflectionVariant))
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

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRsvpd) "✓ RSVP Active" else "Registration Open",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRsvpd) Color(0xFF34C759) else Color(0xFF007AFF)
                            )
                            Text(
                                text = "Counts +4.0 hrs toward NHS goal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                onToggleRsvp()
                                val msg = if (!isRsvpd) "✓ RSVP Confirmed! +4.0 hrs added to National Honor Society." else "RSVP cancelled. Hours updated."
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRsvpd) Color(0xFF34C759) else Color(0xFF007AFF)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(if (isRsvpd) "✓ RSVP'd" else "RSVP Now", fontSize = 13.sp)
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
                            value = supervisorEmail,
                            onCopy = { copyToClipboard("Email", supervisorEmail) }
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
                                    reflectionVariant = (reflectionVariant + 1) % 2
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
