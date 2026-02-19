package com.mobileapps.stattracker.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.AuthState
import com.mobileapps.stattracker.AuthViewModel

@Composable
fun CheckEmailScreen(
    email: String,
    onGoToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Icon
            Text(
                text = "📧",
                fontSize = 64.sp
            )

            Text(
                text = "Check Your Email",
                color = Orange,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "We sent a verification link to:",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Email container
            Box(
                modifier = Modifier
                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = email,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Click the link in the email to verify your account, then come back and log in.",
                color = TextGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Go to login button
            Button(
                onClick = onGoToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text(
                    text = "Go to Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Resend email
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
            if (authState is AuthState.Success) {
                Text(
                    text = "Verification email resent!",
                    color = Color(0xFF4CAF50),
                    fontSize = 13.sp
                )
            }

            TextButton(onClick = { authViewModel.resendVerificationEmail() }) {
                Text("Resend verification email", color = Orange, fontSize = 14.sp)
            }
        }
    }
}