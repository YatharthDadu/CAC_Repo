package com.example.volunteerhub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignatureScreen(
    onSignatureVerified: (name: String, email: String) -> Unit,
    onCancel: () -> Unit
) {
    var supervisorName by remember { mutableStateOf("Marta Alvarez") }
    var supervisorEmail by remember { mutableStateOf("m.alvarez@medicalcity.org") }
    val points = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Supervisor Sign-Off",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }

            Text(
                text = "Please hand phone to site coordinator to sign with finger.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = supervisorName,
                onValueChange = { supervisorName = it },
                label = { Text("Supervisor Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = supervisorEmail,
                onValueChange = { supervisorEmail = it },
                label = { Text("Supervisor Work Email (for audit)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Sign on line below:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Signature Drawing Pad Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            points.add(change.position)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        if ((p1 - p2).getDistance() < 60f) {
                            drawLine(
                                color = Color.Black,
                                start = p1,
                                end = p2,
                                strokeWidth = 5f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                if (points.isEmpty()) {
                    Text(
                        text = "✍️ Draw signature here with finger",
                        color = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                TextButton(
                    onClick = { points.clear() },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text("Clear", color = Color.Red.copy(alpha = 0.8f))
                }
            }
        }

        Button(
            onClick = {
                onSignatureVerified(supervisorName, supervisorEmail)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))
        ) {
            Text("Verify & Stamp 4.0 Hours ✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
