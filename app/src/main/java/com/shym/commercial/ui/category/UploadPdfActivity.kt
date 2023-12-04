package com.shym.commercial.ui.category

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shym.commercial.ui.admin.DashboardAdminActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityUploadPdfBinding
import com.shym.commercial.models.ModelCategory

@Suppress("DEPRECATION")
class UploadPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPdfBinding
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private var fileUris: ArrayList<Uri> = ArrayList()
    private val TAG = "File_upload_tag"
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var title = ""
    private var description = ""
    private var price = ""
    private var uid = ""
    private var category = ""
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    val fileActivityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK) {
                    Log.d(TAG, "Files picked: ")
                    val data = result.data
                    if (data != null) {
                        if (data.clipData != null) {
                            for (i in 0 until data.clipData!!.itemCount) {
                                val clipItem = data.clipData!!.getItemAt(i)
                                fileUris.add(clipItem.uri)
                                if (fileUris.size >= 7) {
                                    break
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "File Pick cancelled ")
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadFileCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.attacheConst.setOnClickListener {
            filePickIntent()
        }

        binding.submitConst.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        price = binding.costEt.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (price.isEmpty()) {
            Toast.makeText(this, "Write a price product...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (fileUris.isEmpty()) {
            Toast.makeText(this, "Pick Files...", Toast.LENGTH_SHORT).show()
        } else {
            binding.attacheConst.background =
                ContextCompat.getDrawable(this, R.drawable.add_new_product_salesman)
            uploadFilesToStorage()
        }
    }

    private fun uploadFilesToStorage() {
        Log.d(TAG, "uploadFilesToStorage: upload to storage...")

        progressDialog.setMessage("Uploading Files...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val filesInfoHashMap: HashMap<String, Any> = HashMap()

        for ((index, uri) in fileUris.withIndex()) {
            val filePathAndName = "Files/$timestamp/$uid/$title/$category/$price/$index"
            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

            storageReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "uploadFilesToStorage: File uploaded now getting url...")

                    val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadFileUrl = "${uriTask.result}"

                    // Add file info to the HashMap
                    val fileInfoHashMap: HashMap<String, Any> = HashMap()
                    fileInfoHashMap["url"] = uploadFileUrl
                    fileInfoHashMap["timestamp"] = System.currentTimeMillis()

                    // Use the index as a key to distinguish different files
                    filesInfoHashMap["$index"] = fileInfoHashMap

                    // Check if this is the last file
                    if (index == fileUris.size - 1) {
                        uploadFilesInfoToDb(filesInfoHashMap, timestamp)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "uploadFilesToStorage: failed to upload due to ${e.message}")
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failure to upload due to ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun uploadFilesInfoToDb(filesInfoHashMap: HashMap<String, Any>, timestamp: Long) {
        Log.d(TAG, "uploadFilesInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading files info...")

        uid = firebaseAuth.uid.toString()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = uid
        hashMap["id"] = timestamp
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["price"] = price
        hashMap["categoryId"] = selectedCategoryId
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0
        hashMap["files"] = filesInfoHashMap

        val ref = FirebaseDatabase.getInstance().getReference("Files")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadFilesInfoToDb: upload to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                fileUris.clear()
                startActivity(Intent(this, DashboardAdminActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadFilesInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failure to upload due to ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }


    private fun loadFileCategories() {
        Log.d(TAG, "loadCategories: Loading file categories")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing file category pick dialog")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoriesArray.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                binding.categoryTv.text = selectedCategoryTitle
                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun filePickIntent() {
        Log.d(TAG, "filePickIntent: starting file pick intent")

        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Разрешить выбор нескольких файлов

        fileActivityResultLauncher.launch(intent)
    }
}
