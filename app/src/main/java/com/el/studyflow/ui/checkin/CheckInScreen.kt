package com.el.studyflow.ui.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.studyflow.domain.model.SessionQuality

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8F3ED)
    val textColor = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)
    val accentGreen = Color(0xFF40916C)

    val checkInState by viewModel.checkInState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Check-in",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accentGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "😊", style = MaterialTheme.typography.headlineLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Question
            Text(
                text = checkInState.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = checkInState.timestamp,
                style = MaterialTheme.typography.bodyMedium,
                color = subtleText
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Quality options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // First row: Great and Okay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.submitCheckIn(SessionQuality.GREAT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (checkInState.selectedQuality == SessionQuality.GREAT) accentGreen else Color(0xFF40916C).copy(alpha = 0.2f),
                            contentColor = if (checkInState.selectedQuality == SessionQuality.GREAT) Color.White else accentGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !checkInState.isSubmitting
                    ) {
                        Text(
                            text = "Great! 🎉",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { viewModel.submitCheckIn(SessionQuality.OKAY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (checkInState.selectedQuality == SessionQuality.OKAY) accentGreen else Color(0xFF40916C).copy(alpha = 0.2f),
                            contentColor = if (checkInState.selectedQuality == SessionQuality.OKAY) Color.White else accentGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !checkInState.isSubmitting
                    ) {
                        Text(
                            text = "Okay 👍",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Second row: Struggled
                Button(
                    onClick = { viewModel.submitCheckIn(SessionQuality.STRUGGLED) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (checkInState.selectedQuality == SessionQuality.STRUGGLED) accentGreen else Color(0xFF40916C).copy(alpha = 0.2f),
                        contentColor = if (checkInState.selectedQuality == SessionQuality.STRUGGLED) Color.White else accentGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !checkInState.isSubmitting
                ) {
                    Text(
                        text = "Struggled 😓",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submission status
            if (checkInState.isSubmitting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = accentGreen,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Saving...",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleText
                    )
                }
            }

            if (checkInState.isCompleted) {
                Text(
                    text = "✓ Check-in saved!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}