package com.el.studyflow.ui.component


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val Green400 = Color(0xFF40916C)

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCCCCCC)
    val textColor   = if (isDark) Color.White else Color(0xFF0A0A0A)
    val labelColor  = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = Green400,
                unfocusedBorderColor = borderColor,
                errorBorderColor = Color(0xFFE53935),
                focusedLabelColor = Green400,
                unfocusedLabelColor = labelColor,
                cursorColor = Green400,
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFE53935),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
            )
        }
    }
}