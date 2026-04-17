package com.mobileapps.stattracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileapps.stattracker.R
import com.mobileapps.stattracker.viewmodels.AuthState
import com.mobileapps.stattracker.viewmodels.AuthViewModel
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor

@Composable
fun CheckEmailScreen(
    email: String,
    onGoToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    Log.d("Lifecycle", "Check Email composed")

    val authState by authViewModel.authState.collectAsState()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.check_your_email),
                color = MainColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.verification_link_sent),
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            // Email container
            Box(
                modifier = Modifier
                    .background(MainColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = email,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = stringResource(R.string.verify_account_msg),
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
                colors = ButtonDefaults.buttonColors(containerColor = MainColor)
            ) {
                Text(
                    text = stringResource(R.string.go_to_login),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Resend email
            if (authState is AuthState.Error) {
                val error = authState as AuthState.Error
                val errorMessage = error.message ?: error.messageResId?.let { stringResource(it) } ?: ""
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
            if (authState is AuthState.Success) {
                Text(
                    text = stringResource(R.string.verification_resent),
                    color = Color(0xFF4CAF50),
                    fontSize = 13.sp
                )
            }

            TextButton(onClick = { authViewModel.resendVerificationEmail() }) {
                Text(stringResource(R.string.resend_verification), color = MainColor, fontSize = 14.sp)
            }
        }
    }
}