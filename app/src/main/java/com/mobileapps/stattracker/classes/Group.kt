package com.mobileapps.stattracker.classes

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId val id: String = "",
    val ownerID: String = "",
    val name: String = "",
    val location: String = "",
)