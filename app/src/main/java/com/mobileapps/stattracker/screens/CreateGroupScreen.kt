package com.mobileapps.stattracker.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileapps.stattracker.ui.theme.BackgroundColor
import com.mobileapps.stattracker.ui.theme.MainColor

@Composable
fun CreateGroupScreen(
    onSubmitClick: (name: String, location: String) -> Unit,
) {
    Log.d("Lifecycle", "Create Group composed")

    var groupName by remember { mutableStateOf("") }
    var groupLocation by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "New Group",
                color = MainColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MainColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = groupLocation,
                onValueChange = { groupLocation = it },
                label = { Text("Location", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MainColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { if (groupName.isNotBlank() && groupLocation.isNotBlank()) onSubmitClick(groupName, groupLocation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainColor)
            ) {
                Text(
                    text = "Create Group",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
