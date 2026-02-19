package com.mobileapps.stattracker

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
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
                    _authState.value = AuthState.Error("Please verify your email before logging in")
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    fun signUp(email: String, password: String, confirmPassword: String, username: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener

                // Set display name to username
                val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdate).addOnSuccessListener {
                    // Send verification email AFTER profile is updated
                    user.sendEmailVerification()
                }

                // Save to Firestore
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid).set(
                    mapOf("username" to username, "email" to email)
                )

                _authState.value = AuthState.Success
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
                    _authState.value = AuthState.Error(it.message ?: "Failed to resend email")
                }
        } else {
            _authState.value = AuthState.Error("No user found, please sign up again")
        }
    }
}