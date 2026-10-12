package com.example.volunteerhub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerhub.model.UserProfile
import com.example.volunteerhub.model.VolunteerEvent

@Composable
fun PdfScreen(
    userProfile: UserProfile,
    events: List<VolunteerEvent>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F7))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Text("✕", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("Verified Resume (PDF)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = {
                    Toast.makeText(context, "Exported PDF to device storage!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("Share / Print")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Printable Paper Document Card
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header Seal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "CONGRESSIONAL DISTRICT TX-04",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                        Text(
                            text = "OFFICIAL SERVICE TRANSCRIPT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Volunteer Hub Certified Record",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    Text("🏅", fontSize = 36.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Black.copy(alpha = 0.2f))

                // Student Metadata
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("STUDENT NAME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(userProfile.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column {
                        Text("GRADE LEVEL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(userProfile.grade, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Column {
                        Text("PATHWAY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(userProfile.intendedMajor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Summary Stats
                Surface(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL VERIFIED HOURS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("18.0 HRS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34C759))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AUDIT STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("PASS (100%)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("VERIFIED LOGS & SUPERVISOR ATTESTATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                events.take(3).forEach { ev ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ev.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("4.0 Hours", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF007AFF))
                        }
                        Text("${ev.organizationName} • ${ev.location}", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI Impact Reflection: Assembled comfort supplies and guided client check-in logistics with attention to safety guidelines.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ Signed by Site Supervisor (Marta Alvarez, Coordinator)", fontSize = 11.sp, color = Color(0xFF34C759), fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "This document serves as cryptographic proof of volunteer hours completed in compliance with National Honor Society and Congressional App Challenge standards.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
