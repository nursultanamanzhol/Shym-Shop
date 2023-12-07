package com.shym.commercial.viewmodel

import android.net.Uri
import android.service.autofill.UserData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class UserData(
    val name: String = "",
    val language: String = "",
    val mode: String = "",
    val profileImage: String = ""
)
class ProfileEditViewModel : ViewModel() {

    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> get() = _userData

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userRef = FirebaseDatabase.getInstance().getReference("Users")

    fun loadUserInfo() {
        firebaseAuth.uid?.let { uid ->
            userRef.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)
                    _userData.value = userData
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
        }
    }

    fun validateData(name: String, language: String, modeDl: String, imageUri: Uri?) {
        // Validate data and update profile
        if (name.isEmpty()) {
            // Handle empty name
        } else {
            if (imageUri == null) {
                // Update profile without image
                updateProfile(name, language, modeDl, "")
            } else {
                // Update profile with image
                updateImage(name, language, modeDl, imageUri)
            }
        }
    }

    private fun updateImage(name: String, language: String, modeDl: String, imageUri: Uri) {
        // Implement image upload logic and call updateProfile method with the image URL
    }

    private fun updateProfile(name: String, language: String, modeDl: String, uploadedImageUrl: String) {
        // Implement profile update logic using the provided data
    }

    fun showImageMenu() {
        // Implement image menu logic
    }
}
