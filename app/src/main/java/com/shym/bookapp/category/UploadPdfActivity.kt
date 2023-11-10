package com.shym.bookapp.category

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
import com.shym.bookapp.users_role.admin.DashboardAdminActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.bookapp.databinding.ActivityUploadPdfBinding
import com.shym.bookapp.models.ModelCategory

@Suppress("DEPRECATION")
class UploadPdfActivity : AppCompatActivity() {


    private lateinit var binding: ActivityUploadPdfBinding

    //pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfUri: Uri? = null

    //Tag
    private val TAG = "Pdf_add_tag"

    //dialog
    private lateinit var progressDialog: ProgressDialog

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private var title = ""
    private var description = ""
    private var category = ""
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Pdf picked: ")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "Pdf Pick cancelled ")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()
        //setup pro
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        //handle click, show categorypick dialog

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, goback
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //handle click, pick pdf intent
        binding.attacheConst.setOnClickListener {
            pdfPickIntent()
        }
        //handle click, start uploading pdf/product
        binding.submitConst.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        //validate data ?Step 1
        Log.d(TAG, "validateData: validating data")
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()
        //validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show()

        } else {
            //data validated, begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        /*upload pdf to firebase storage*/
        Log.d(TAG, "uploadPdfToStorage: upload to storage...")
        //show progress dialog
        progressDialog.setMessage("Uploading Pdf...")
        progressDialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()
        // path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded now getting url...")

                //Step -3  Get url of upload pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"
                uploadPdfInfoToDo(uploadPdfUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failure to upload due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun uploadPdfInfoToDo(uploadPdfUrl: String, timestamp: Long) {
        //Step 4: Uploaded Pdf info to firebase db
        Log.d(TAG, "uploadPdfInfoToDo: uploding to db")
        progressDialog.setMessage("Uploading pdf info...")

        //uid current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        //db reference DB > BookS>BookId> (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDo: upload to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                pdfUri = null
                startActivity(Intent(this, DashboardAdminActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failure to upload due to ${e.message}", Toast.LENGTH_SHORT).show()

            }


    }


    private fun loadPdfCategories() {
        Log.d(TAG, "loadCategories: Loading pdf categories")
        //init arraylist
        categoryArrayList = ArrayList()
        /*database reference to load categories DF > Categories */
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    //add data to arrayList
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of categories from arrayList
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoriesArray.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->

                //handle click
                //get click item

                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                //set category
                binding.categoryTv.text = selectedCategoryTitle
                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

}