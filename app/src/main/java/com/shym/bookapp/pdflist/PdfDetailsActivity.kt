package com.shym.bookapp.pdflist

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.EnvironmentalReverb
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.bookapp.databinding.ActivityPdfDetailsBinding
import java.io.FileOutputStream

class PdfDetailsActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private lateinit var binding: ActivityPdfDetailsBinding
    private var bookId = ""
    private var bookTitle = ""  //get from firebase
    private var bookUrl = ""
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //progress
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        MyApplication.incrementBookViewCount(bookId)
        loadBookDetails()


        //handle click back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdf view activity, lets create an activity for view

        binding.readBookBtn.setOnClickListener {
            openPdfView()
        }
        binding.pdfView.setOnClickListener {
            openPdfView()
        }

        //handle cick download book/ pdf
        binding.downloadBookBtn.setOnClickListener {
            // first check storage
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            //lets check if grannted or not
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Download Book")
        progressDialog.setMessage("Download Book")
        progressDialog.show()
        //lets download book from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book downloaded...")
                savedToDownloadFolder(bytes)
            }
    }

    private fun savedToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG, "savedToDownloadFolder: saving downloaded book")
        val nameWithExtension = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder.mkdirs()
            val filePath = downloadFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "savedToDownloadFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementDownloadCount()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "savedToDownloadFolder: Failed to save due to ${e.message}")
            Toast.makeText(this, "Failed to save due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
//        count downloads
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {   // data count download
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")
                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New downloads Count: $newDownloadCount")
                    //setup data to update to db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads count incremented")

                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: FAILED to increment due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun openPdfView() {
        val intent = Intent(this, PdfViewActivity::class.java)
        intent.putExtra("bookId", bookId)
        startActivity(intent)
    }

    private fun loadBookDetails() {
        //Book > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)  //load pdf category
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)
                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}