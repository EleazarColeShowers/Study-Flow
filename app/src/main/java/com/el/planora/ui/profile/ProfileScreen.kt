package com.el.planora.ui.profile

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Green400 = Color(0xFF40916C)
private val Green100 = Color(0xFFD8F3DC)

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val state  by viewModel.state.collectAsStateWithLifecycle()

    val bgColor    = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg     = if (isDark) Color(0xFF1A1A1A) else Color.White
    val textColor  = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            // Placeholder to balance the row
            Spacer(Modifier.width(48.dp))
        }

        // ── Loading ────────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green400)
            }
            return@Column
        }

        Spacer(Modifier.height(16.dp))

        // ── Avatar + name ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1B4332) else Green100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.username
                        .split(" ")
                        .take(2)
                        .joinToString("") { it.take(1).uppercase() }
                        .ifEmpty { "?" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Green400
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = state.username.ifEmpty { "—" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = subtleText
            )

            if (state.userCategory.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Green400.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.userCategory,
                        style = MaterialTheme.typography.labelMedium,
                        color = Green400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Active Subjects (from Planora API) ─────────────────────────────────
        if (state.activeSubjects.isNotEmpty()) {
            Text(
                text = "Active Subjects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Green400,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.activeSubjects.forEach { subject ->
                    ProfileInfoRow(
                        icon = Icons.Default.School,
                        label = "Subject",
                        value = subject,
                        cardBg = cardBg,
                        textColor = textColor,
                        subtleText = subtleText
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }

        // ── Study Profile (from Firestore) ─────────────────────────────────────
        Text(
            text = "Study Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green400,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.studySubject.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.Book,
                    label = "Studying",
                    value = state.studySubject,
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
            if (state.studyGoal.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.EmojiEvents,
                    label = "Goal",
                    value = state.studyGoal,
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
            if (state.dailyDedication.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Daily dedication",
                    value = state.dailyDedication,
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
            if (state.studyTime.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.LightMode,
                    label = "Best study time",
                    value = state.studyTime,
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
            if (state.attentionSpan.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.WbTwilight,
                    label = "Attention span",
                    value = state.attentionSpan,
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
            if (state.learningDifferences.isNotEmpty()) {
                ProfileInfoRow(
                    icon = Icons.Default.Person,
                    label = "Learning differences",
                    value = state.learningDifferences.joinToString(", "),
                    cardBg = cardBg, textColor = textColor, subtleText = subtleText
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        // ── Sign out ───────────────────────────────────────────────────────────
        Button(
            onClick = { viewModel.logout(onLoggedOut) },
            enabled = !state.isLoggingOut,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            )
        ) {
            if (state.isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Profile info row ───────────────────────────────────────────────────────────
@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    cardBg: Color,
    textColor: Color,
    subtleText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Green400.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Green400,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = subtleText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}