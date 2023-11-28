package com.shym.commercial.users_role.profile

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.play.integrity.internal.h
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.commercial.DialogUtils
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityProfileEditBinding
import com.shym.commercial.extensions.setSafeOnClickListener

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth

    //image uri
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
            val progressDialog = DialogUtils.createProgressDialog(this, "Please wait...")
            progressDialog.show()
        }
        binding.updateBtn.setSafeOnClickListener {
            validateData()

        }
        binding.cameraImg.setSafeOnClickListener {
            showImageMenu()
            val progressDialog = DialogUtils.createProgressDialog(this, "The camera opens...")
            progressDialog.show()
        }

    }

    private var name = ""
    private fun validateData() {
        //get data
        name = binding.nameEt.text.toString().trim()
        //validate data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                //update without image
                updateProfile("")

            } else {
                //update with image
                updateImage()
            }
        }


    }

    private fun updateImage() {
        val progressDialog = DialogUtils.createProgressDialog(this, "Uploading profile image")
        progressDialog.show()
        //image path and name, use uid to replace previous
        val filePathAndName = "ProfileImages/" + firebaseAuth.uid
        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        (if (imageUri != null) imageUri else throw NullPointerException("Expression 'imageUri' must not be null"))?.let {
            reference.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    //image uploaded, get url of uploaded image.
                    val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = "${uriTask.result}"

                    updateProfile(uploadedImageUrl)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed to uploaded image due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun updateProfile(uploadedImageUrl: String) {
        val progressDialog = DialogUtils.createProgressDialog(this, "Uploading profile...")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri != null) {
            hashMap["profileImage"] = uploadedImageUrl
        }

        //update
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        (if (firebaseAuth.uid != null) firebaseAuth.uid else throw NullPointerException("Expression 'firebaseAuth.uid' must not be null"))?.let {
            userRef.child(it)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    // update success
                    Toast.makeText(
                        this,
                        "Profile updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    //fail
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed to update profile due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loadUserInfo() {
        //db firebase user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        (if (firebaseAuth.uid != null) firebaseAuth.uid else throw NullPointerException("Expression 'firebaseAuth.uid' must not be null"))?.let {
            ref.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info
                        val name = "${snapshot.child("name").value}"
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val timestamp = "${snapshot.child("timestamp").value}"
                        val uid = "${snapshot.child("uid").value}"
                        val quickAccessCode = "${snapshot.child("quickAccessCode").value}"


                        //set date
                        binding.nameEt.setText(name)

                        //set image
                        try {
                            Glide.with(this@ProfileEditActivity)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileImg)
                        } catch (e: Exception) {

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    private fun showImageMenu() {
        //show photo
        //setup popup Menu
        val popupMenu = PopupMenu(this, binding.profileImg)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 0, 0, "Gallery")

        //handle
        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                //camera clicked
                pickImageCamera()

            } else if (id == 1) {
                //gallery
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageGallery() {

        // intent to pick image for gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    //used tot handle
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //used tot handle
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    imageUri = data.data
                } else {
                    Toast.makeText(this, "data id null", Toast.LENGTH_SHORT).show()
                }
                binding.profileImg.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun pickImageCamera() {
        //intent to pick
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //used tot handle
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
//                if (data != null) {
//                    imageUri = data.data
//                } else {
//                    Toast.makeText(this, "data id null", Toast.LENGTH_SHORT).show()
//                }
                binding.profileImg.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


}