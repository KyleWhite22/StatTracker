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

    fun loadGroupById(groupId: String, onResult: (Group?) -> Unit) {
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(Group::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addMemberToGroup(groupId: String, memberName: String, onSuccess: () -> Unit) {
        val docRef = db.collection("groups").document(groupId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentMembers = snapshot.get("members") as? List<String> ?: emptyList()
            if (!currentMembers.contains(memberName)) {
                val newMembers = currentMembers + memberName
                transaction.update(docRef, "members", newMembers)
            }
        }.addOnSuccessListener {
            onSuccess()
        }
    }

    fun deleteGroup(groupId: String, onSuccess: () -> Unit) {
        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener {
                loadGroups()
                onSuccess()
            }
    }

    fun createGroup(name: String, location: String, onSuccess: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val docRef = db.collection("groups").document()
        val group = Group(
            id = docRef.id,
            ownerID = currentUserId,
            name = name,
            location = location,
            members = emptyList()
        )
        docRef.set(group)
            .addOnSuccessListener {
                loadGroups()
                onSuccess()
            }
    }
}