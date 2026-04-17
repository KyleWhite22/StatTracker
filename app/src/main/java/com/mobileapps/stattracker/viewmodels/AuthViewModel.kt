package com.mobileapps.stattracker.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileapps.stattracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String? = null, @StringRes val messageResId: Int? = null) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error(messageResId = R.string.error_fill_fields)
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user?.isEmailVerified == true) {
                    _authState.value = AuthState.Success
                } else {
                    auth.signOut()
                    _authState.value = AuthState.Error(messageResId = R.string.error_verify_email)
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(message = it.message ?: "Login failed")
            }
    }

    fun signUp(email: String, password: String, confirmPassword: String, username: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error(messageResId = R.string.error_fill_fields)
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error(messageResId = R.string.error_passwords_mismatch)
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error(messageResId = R.string.error_password_too_short)
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener

                // Set display name to username
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdate).addOnSuccessListener {
                    // Send verification email AFTER profile is updated
                    user.sendEmailVerification()
                }

                // Save to Firestore
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid).set(
                    mapOf("username" to username, "email" to email)
                )

                _authState.value = AuthState.Success
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(message = it.message ?: "Signup failed")
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Check if user is already logged in
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Loading
            user.sendEmailVerification()
                .addOnSuccessListener {
                    _authState.value = AuthState.Success
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Error(message = it.message ?: "Failed to resend email")
                }
        } else {
            _authState.value = AuthState.Error(messageResId = R.string.error_no_user)
        }
    }
}