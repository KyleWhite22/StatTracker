package com.mobileapps.stattracker.viewmodels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobileapps.stattracker.classes.Group

class GroupViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var groups by mutableStateOf<List<Group>>(emptyList())
        private set

    fun loadGroups() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("groups")
            .whereEqualTo("ownerID", userId)
            .get()
            .addOnSuccessListener { result ->
                groups = result.documents.mapNotNull { it.toObject(Group::class.java) }
            }
    }
    fun createGroup(name: String, location: String, onSuccess: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef = db.collection("groups").document()
        val group = Group(
            id = docRef.id,
            ownerID = currentUserId,
            name = name,
            location = location
        )
        docRef.set(group)
            .addOnSuccessListener {
                loadGroups()
                onSuccess()
            }
    }
}