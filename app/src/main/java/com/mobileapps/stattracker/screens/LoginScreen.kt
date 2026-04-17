package com.mobileapps.stattracker.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.R
import com.mobileapps.stattracker.viewmodels.AuthState
import com.mobileapps.stattracker.viewmodels.AuthViewModel
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor

val TextGray = Color(0xFFAAAAAA)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToSignUp: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    Log.d("Lifecycle", "Login composed")

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            title = { Text(stringResource(R.string.no_internet_title), color = Color.White) },
            text = { Text(stringResource(R.string.no_internet_msg), color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showNoInternetDialog = false }) {
                    Text(stringResource(R.string.ok), color = MainColor)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pickuplogo),
                contentDescription = stringResource(R.string.app_logo_desc),
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.pick_up_logo_text),
                color = MainColor,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email), color = TextGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MainColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password), color = TextGray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MainColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                val errorMessage = (authState as AuthState.Error).let { error ->
                    error.message ?: error.messageResId?.let { stringResource(it) } ?: ""
                }
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isOnline(context)) {
                        authViewModel.login(email, password)
                    } else {
                        showNoInternetDialog = true
                    }
                },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.log_in),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.no_account), color = TextGray, fontSize = 14.sp)
                TextButton(onClick = onGoToSignUp) {
                    Text(stringResource(R.string.sign_up), color = MainColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
    }
    return false
}