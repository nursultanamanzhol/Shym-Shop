package com.shym.commercial.ui.category

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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityUploadPdfSalesmanBinding
import com.shym.commercial.data.model.ModelCategory
import com.shym.commercial.ui.users.DashboardSalesmanPageActivity

@Suppress("DEPRECATION")
class UploadPdfActivitySalesman : AppCompatActivity() {
    private var categoryId = ""
    private lateinit var binding: ActivityUploadPdfSalesmanBinding

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
    private var price = 0
    private var discount =0
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
        binding = ActivityUploadPdfSalesmanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()
        //setup pro
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        //handle click, show categorypick dialog

//        binding.categoryTv.setOnClickListener {
//            categoryPickDialog()
//        }

        //handle click, goback
        binding.backBtn.setOnClickListener {
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
        // Проверка данных - Шаг 1
        Log.d(TAG, "validateData: проверка данных")
        // Получение данных
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        // Преобразование строк в Int
        price = binding.costEt.text.toString().toIntOrNull() ?: 0
        discount = binding.discountEt.text.toString().toIntOrNull() ?: 0

//        category = binding.categoryTv.text.toString().trim()

        // Проверка данных
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите заголовок...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Введите описание...", Toast.LENGTH_SHORT).show()
        } else if (price == 0) {
            Toast.makeText(this, "Введите цену продукта...", Toast.LENGTH_SHORT).show()
        }
//        else if (discount.isEmpty()) {
//            Toast.makeText(this, "Введите скидку...", Toast.LENGTH_SHORT).show()
//        }
//        else if (category.isEmpty()) {
//            Toast.makeText(this, "Введите категорию...", Toast.LENGTH_SHORT).show()
//        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Выберите PDF...", Toast.LENGTH_SHORT).show()
        } else {
            // Данные проверены, начнем загрузку
            binding.attacheConst.background =
                ContextCompat.getDrawable(this, R.drawable.add_new_product_salesman)
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
        (if (pdfUri != null) pdfUri else throw NullPointerException("Expression 'pdfUri' must not be null"))?.let {
            storageReference.putFile(it)
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
                    Toast.makeText(
                        this,
                        "Failure to upload due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun uploadPdfInfoToDo(uploadPdfUrl: String, timestamp: Long) {
        //Step 4: Uploaded Pdf info to firebase db
        Log.d(TAG, "uploadPdfInfoToDo: uploding to db")
        progressDialog.setMessage("Uploading pdf info...")

        //uid current user
        val uid = firebaseAuth.uid

        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    categoryId = "${snapshot.child("categories").value}"
                    //setup data to upload
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["uid"] = "$uid"
                    hashMap["id"] = "$timestamp"
                    hashMap["title"] = "$title"
                    hashMap["description"] = "$description"
                    hashMap["price"] = price
                    hashMap["discount"] = discount
                    hashMap["categoryId"] = "$categoryId"
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
                            Toast.makeText(
                                this@UploadPdfActivitySalesman,
                                "Uploaded...",
                                Toast.LENGTH_SHORT
                            ).show()
                            pdfUri = null
//                fileUris.clear()
                            startActivity(
                                Intent(
                                    this@UploadPdfActivitySalesman,
                                    DashboardSalesmanPageActivity::class.java
                                )
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                            progressDialog.dismiss()
                            Toast.makeText(
                                this@UploadPdfActivitySalesman,
                                "Failure to upload due to ${e.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


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

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

}