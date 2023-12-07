package com.shym.commercial.ui.profile

import android.app.Activity
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.commercial.extensions.DialogUtils
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityProfileEditBinding
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.extensions.MyApplication
import com.yariksoffice.lingver.Lingver

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

//        switchLDMode()

        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
            val progressDialog = DialogUtils.createProgressDialog(this, "Please wait...")
            progressDialog.show()
        }

        binding.lngImg.setSafeOnClickListener {
            lngPickDialog()
        }
        binding.modeTv.setSafeOnClickListener {
            modePickDialog()
        }

        binding.updateBtn.setSafeOnClickListener {
            validateData()

        }
        binding.cameraImg.setSafeOnClickListener {
            showImageMenu()

        }

    }


    private var name = ""
    private var language = ""
    private var modeDl = ""
    private fun validateData() {
        //get data
        name = binding.nameEt.text.toString().trim()
        language = binding.lngImg.text.toString().trim()
        modeDl = binding.modeTv.text.toString().trim()
        //validate data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
//        } else if (language.isEmpty()) { // Проверяем выбран ли язык
//
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
                    progressDialog.dismiss()
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

    private fun lngPickDialog() {
        val languages = arrayOf("en", "kk", "ru")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Language")
            .setSingleChoiceItems(languages, -1) { dialog, which ->
                // which - индекс выбранного элемента
                language = languages[which]
                binding.lngImg.text = language// Сохраняем выбранный язык
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun modePickDialog() {
        val modes = arrayOf("Light Mode", "System Mode", "Dark Mode")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose mode")
            .setSingleChoiceItems(modes, -1) { dialog, which ->
                // which - индекс выбранного элемента
                modeDl = modes[which]
                binding.modeTv.text = modeDl// Сохраняем выбранный язык
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }


    private fun updateProfile(uploadedImageUrl: String) {

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = name
        hashMap["language"] = language
        hashMap["mode"] = modeDl
        if (imageUri != null) {
            hashMap["profileImage"] = uploadedImageUrl
        }
        val progressDialog = DialogUtils.createProgressDialog(this, "Uploading profile...")
        progressDialog.show()

        //update
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        (if (firebaseAuth.uid != null) firebaseAuth.uid else throw NullPointerException("Expression 'firebaseAuth.uid' must not be null"))?.let {
            userRef.child(it)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    // update success
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    if (modeDl.isEmpty()) {
                        setTheme(MyApplication.Theme.SYSTEM)
                    } else if (modeDl == "Light Mode") {
                        setTheme(MyApplication.Theme.LIGHT)
                    } else if (modeDl == "System Mode") {
                        setTheme(MyApplication.Theme.SYSTEM)
                    } else if (modeDl == "Dark Mode") {
                        setTheme(MyApplication.Theme.DARK)
                    }

                    if (language.isEmpty()) {
                        Lingver.getInstance().setLocale(this@ProfileEditActivity, language)
                        this.recreate()
                    } else {
                        Lingver.getInstance().setLocale(this@ProfileEditActivity, "en")
                        this.recreate()
                    }
                    startActivity(Intent(this@ProfileEditActivity, ProfileActivity::class.java))
                    finish()
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
    private fun setTheme(theme: MyApplication.Theme) {
        AppCompatDelegate.setDefaultNightMode(theme.system)
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
                        val language = "${snapshot.child("language").value}"
                        val mode = "${snapshot.child("mode").value}"
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val timestamp = "${snapshot.child("timestamp").value}"
                        val uid = "${snapshot.child("uid").value}"
                        val quickAccessCode = "${snapshot.child("quickAccessCode").value}"


                        //set date
                        binding.nameEt.setText(name)
                        binding.lngImg.setText(language)
                        binding.modeTv.setText(mode)

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
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

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
                imageUri = data!!.data
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
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

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

                binding.profileImg.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}


