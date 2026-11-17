package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalHisobimColors

@Composable
fun LockScreen(
    savedPassword: String,
    onUnlock: () -> Unit,
    onCreatePassword: (String) -> Unit
) {
    val colors = LocalHisobimColors.current
    var isCreateMode by remember(savedPassword) { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val submitAction = {
        when {
            isCreateMode || savedPassword.isEmpty() -> {
                if (password.length < 4) {
                    errorMessage = "Parol kamida 4 ta belgidan iborat bo'lsin"
                } else if (isCreateMode && confirmPassword != password) {
                    errorMessage = "Parollar mos kelmadi"
                } else {
                    onCreatePassword(password)
                }
            }
            password == savedPassword -> {
                onUnlock()
            }
            else -> {
                errorMessage = "Parol noto'g'ri, qaytadan urinib ko'ring"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.loginBg)
            .imePadding()
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon box matching Page 1 & Page 13
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(colors.loginIconBox),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (colors.isDark) {
                        Icons.Rounded.AutoAwesomeMosaic
                    } else {
                        Icons.Outlined.AccountBalanceWallet
                    },
                    contentDescription = "Hisobim",
                    tint = colors.loginIconTint,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Hisobim",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isCreateMode -> "Yangi parol yarating"
                    colors.isDark -> "Parolni kiriting va hisobingizga kiring"
                    else -> "Ilovaga kirish uchun parolni kiriting"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(34.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isCreateMode) "Yangi parol" else "Parol",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val fieldBg = if (colors.isDark) colors.card else Color.White
                val fieldText = if (colors.isDark) colors.textPrimary else Color(0xFF141917)

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lock_password_input"),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = fieldText.copy(alpha = 0.45f)
                        )
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = "Parolni ko'rsatish",
                                tint = fieldText.copy(alpha = 0.65f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isCreateMode) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submitAction() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = fieldBg,
                        unfocusedContainerColor = fieldBg,
                        focusedTextColor = fieldText,
                        unfocusedTextColor = fieldText,
                        focusedBorderColor = if (colors.isDark) colors.primaryAccent else Color.White,
                        unfocusedBorderColor = if (colors.isDark) colors.border else Color.White
                    )
                )

                if (isCreateMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Parolni tasdiqlang",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_confirm_password_input"),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "••••••••",
                                color = fieldText.copy(alpha = 0.45f)
                            )
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submitAction() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = fieldBg,
                            unfocusedContainerColor = fieldBg,
                            focusedTextColor = fieldText,
                            unfocusedTextColor = fieldText,
                            focusedBorderColor = if (colors.isDark) colors.primaryAccent else Color.White,
                            unfocusedBorderColor = if (colors.isDark) colors.border else Color.White
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD2CC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = submitAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("lock_submit_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.loginButtonBg,
                    contentColor = colors.loginButtonText
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (isCreateMode) "Parolni saqlash" else "Kirish",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = {
                    isCreateMode = !isCreateMode
                    errorMessage = null
                },
                modifier = Modifier.testTag("lock_toggle_mode_button")
            ) {
                Text(
                    text = if (isCreateMode) {
                        "Ortga qaytish"
                    } else {
                        "Birinchi marta? Parol yaratish"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.loginLinkText
                )
            }

            if (savedPassword.isEmpty() && !isCreateMode) {
                TextButton(
                    onClick = onUnlock,
                    modifier = Modifier.testTag("lock_skip_button")
                ) {
                    Text(
                        text = "Parolsiz kirish",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}
