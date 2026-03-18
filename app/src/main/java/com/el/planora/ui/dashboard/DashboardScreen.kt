package com.el.planora.ui.dashboard

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.planora.domain.model.StudyTrack

private val Green400 = Color(0xFF40916C)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAddNewTrack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val isDark      = isSystemInDarkTheme()
    val bgColor     = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg      = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8F3ED)
    val textColor   = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText  = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)

    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    val stats       by viewModel.stats.collectAsStateWithLifecycle()
    val studyTracks by viewModel.studyTracks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Planora",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green400
                    )
                    Row {
                        // Refresh button
                        if (!uiState.isLoading) {
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = subtleText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ── Search bar ────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        placeholder = { Text("Find learning tracks...", color = subtleText) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = subtleText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = cardBg,
                            focusedContainerColor = cardBg,
                            unfocusedTextColor = textColor,
                            focusedTextColor = textColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Calendar",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Stats cards ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Day Streak",   stats.dayStreak.toString(),        "🔥", Modifier.weight(1f), cardBg, textColor, subtleText)
                    StatCard("Active Plans", stats.activePlans.toString(),       "📚", Modifier.weight(1f), cardBg, textColor, subtleText)
                    StatCard("Avg Progress", "${stats.averageProgress}%",        "📈", Modifier.weight(1f), cardBg, textColor, subtleText)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Your Learning Tracks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green400,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Loading state ──────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Green400)
                            Text(
                                text = "Loading your subjects...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtleText
                            )
                        }
                    }
                }
            }

            // ── Error state ────────────────────────────────────────────────────
            else if (uiState.error != null && studyTracks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "😕", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = "Couldn't load subjects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = Green400)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            // ── Empty state ────────────────────────────────────────────────────
            else if (studyTracks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "📚", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = "No subjects yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Tap the + button to add your first subject",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Study tracks list ──────────────────────────────────────────────
            else {
                items(studyTracks) { track ->
                    StudyTrackCard(
                        track = track,
                        cardBg = cardBg,
                        textColor = textColor,
                        subtleText = subtleText
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // ── Floating Add Button ────────────────────────────────────────────────
        Button(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green400,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
        }
        //    add the sheet:
        if (showAddSheet) {
            AddSubjectSheet(
                onDismiss = { showAddSheet = false },
                onSubjectAdded = { viewModel.refresh() }  // refreshes the list after adding
            )
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    label: String, value: String, icon: String,
    modifier: Modifier = Modifier,
    cardBg: Color, textColor: Color, subtleText: Color
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = subtleText)
        }
    }
}

// ── Study Track Card ──────────────────────────────────────────────────────────

@Composable
private fun StudyTrackCard(
    track: StudyTrack,
    cardBg: Color, textColor: Color, subtleText: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4A7C59).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎨", style = MaterialTheme.typography.headlineMedium)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${track.estimatedHours} hrs",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A0A0A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = track.exam, style = MaterialTheme.typography.bodySmall, color = subtleText)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Green400.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = track.subject, style = MaterialTheme.typography.labelSmall, color = Green400, fontWeight = FontWeight.SemiBold)
                }
                Text(text = "Active today", style = MaterialTheme.typography.labelSmall, color = subtleText)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { track.completionPercentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Green400,
                    trackColor = Green400.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${track.completionPercentage}% Complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green400,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}