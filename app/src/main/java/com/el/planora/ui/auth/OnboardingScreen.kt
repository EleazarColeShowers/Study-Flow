package com.el.planora.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ── Palette ────────────────────────────────────────────────────────────────────
private val Green400 = Color(0xFF40916C)
private val Green600 = Color(0xFF1B4332)
private val Green200 = Color(0xFFB7E4C7)

data class OnboardingPage(
    val emoji: String,          // swap for painter = painterResource(R.drawable.your_svg) later
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        emoji = "📖",
        title = "Study Smarter",
        description = "Break down complex topics into bite-sized lessons tailored to the way your brain works best."
    ),
    OnboardingPage(
        emoji = "🤖",
        title = "Your AI Tutor",
        description = "Ask questions, get instant explanations, and never feel stuck on a topic again — day or night."
    ),
    OnboardingPage(
        emoji = "🧩",
        title = "Built for Every Mind",
        description = "Dyslexia-friendly fonts, text-to-speech, and high-contrast mode — because one size never fits all."
    ),
    OnboardingPage(
        emoji = "🏆",
        title = "Track Your Growth",
        description = "Celebrate every win. Watch your progress build week by week and keep your study streak alive."
    )
)

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val isFirst = pagerState.currentPage == 0
    val isLast = pagerState.currentPage == pages.lastIndex

    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7),
        animationSpec = tween(400),
        label = "bg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (!isLast) {
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Skip",
                        color = Green400,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            PageContent(page = pages[index], isDark = isDark)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.forEachIndexed { index, _ ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) Green400
                            else if (isDark) Color.White.copy(alpha = 0.25f)
                            else Green400.copy(alpha = 0.25f)
                        )
                        .size(if (selected) 10.dp else 7.dp)
                )
            }
        }

        if (isLast) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green400,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Get Started",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Green400),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green400)
                ) {
                    Text(
                        text = "I already have an account",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isFirst) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Green400),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Green400)
                    ) {
                        Text(
                            text = "Previous",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green400,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Next",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage, isDark: Boolean) {
    val textColor = Green400
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF3D3D3D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    if (isDark) Color(0xFF1B4332)
                    else Color(0xFFD8F3DC)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = page.emoji, fontSize = 80.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = subtitleColor,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}