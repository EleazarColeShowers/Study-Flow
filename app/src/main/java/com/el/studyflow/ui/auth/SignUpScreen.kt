package com.el.studyflow.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.studyflow.ui.component.AuthTextField

private val Green400 = Color(0xFF40916C)
private val Green600 = Color(0xFF1B4332)

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val formState by viewModel.signUpForm.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val textColor = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onSignUpSuccess()
            viewModel.resetState()
        }
    }

    val isLoading = authState is AuthUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Join StudyFlow and start learning your way",
            style = MaterialTheme.typography.bodyLarge,
            color = subtleText
        )

        Spacer(Modifier.height(36.dp))

        // Username
        AuthTextField(
            value = formState.username,
            onValueChange = viewModel::onSignUpUsernameChange,
            label = "Username",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Green400) },
            isError = formState.usernameError != null,
            errorMessage = formState.usernameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            isDark = isDark
        )

        Spacer(Modifier.height(16.dp))

        // Email
        AuthTextField(
            value = formState.email,
            onValueChange = viewModel::onSignUpEmailChange,
            label = "Email address",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Green400) },
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isDark = isDark
        )

        Spacer(Modifier.height(16.dp))

        // Password
        AuthTextField(
            value = formState.password,
            onValueChange = viewModel::onSignUpPasswordChange,
            label = "Password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Green400) },
            trailingIcon = {
                IconButton(onClick = viewModel::onSignUpTogglePassword) {
                    Icon(
                        imageVector = if (formState.isPasswordVisible) Icons.Default.Lock
                        else Icons.Default.Face,
                        contentDescription = "Toggle password",
                        tint = subtleText
                    )
                }
            },
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isDark = isDark
        )

        // Error banner
        if (authState is AuthUiState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = (authState as AuthUiState.Error).message,
                color = Color(0xFFE53935),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = viewModel::signUp,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green400,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                color = subtleText,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Sign In",
                    color = Green400,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}