package com.shym.commercial.ui.pdf

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.shym.commercial.R
import com.shym.commercial.databinding.ActivityPdfDetailsSalesmanBinding
import com.shym.commercial.extensions.Constants
import com.shym.commercial.extensions.MyApplication
import java.io.FileOutputStream

class PdfDetailsSalesmanActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }

    private lateinit var binding: ActivityPdfDetailsSalesmanBinding
    private var bookId = ""
    private var bookTitle = ""
    private var price = ""
    private var bookUrl = ""
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var isInMyFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsSalesmanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        MyApplication.incrementBookViewCount(bookId)
        loadBookDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            openPdfView()
        }
        binding.pdfView.setOnClickListener {
            openPdfView()
        }

        binding.downloadBookBtn.setOnClickListener {
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

        binding.favoriteBookBtn.setOnClickListener {

            if (isInMyFavorite) {
                //already in fav, remove
                removeFromFavorite()
            } else {
                addToFavorite()
            }


        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun downloadBook() {
        progressDialog.setMessage("Download Book")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                Toast.makeText(this, "Failed to download book", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book downloaded...")
                if (bytes != null) {
                    savedToDownloadFolder(bytes)
                } else {
                    progressDialog.dismiss()
                    Log.d(TAG, "downloadBook: Bytes are null")
                    Toast.makeText(this, "Failed to download book", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun savedToDownloadFolder(bytes: ByteArray) {
        Log.d(TAG, "savedToDownloadFolder: saving downloaded book")
        val nameWithExtension = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            downloadFolder?.mkdirs()
            val filePath = "${downloadFolder?.path}/$nameWithExtension"
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
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var downloadsCount = "${snapshot.child("downloadsCount").value}"
                Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount")
                if (downloadsCount == "" || downloadsCount == "null") {
                    downloadsCount = "0"
                }

                val newDownloadCount: Long = downloadsCount.toLong() + 1
                Log.d(TAG, "onDataChange: New downloads Count: $newDownloadCount")

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
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val price = "${snapshot.child("price").value}"
                val discount = "${snapshot.child("discount").value}"
                val timestamp = "${snapshot.child("timestamp").value}"
                bookTitle = "${snapshot.child("title").value}"
                val uid = "${snapshot.child("uid").value}"
                bookUrl = "${snapshot.child("url").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"

                val date = MyApplication.formatTimeStamp(timestamp.toLong())

                MyApplication.loadCategory(categoryId, binding.categoryTv)
                MyApplication.loadPdfFromUrlSinglePage(
                    "$bookUrl",
                    "$bookTitle",
                    binding.pdfView,
                    binding.progressBar,
                    binding.pagesTv
                )
                MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)
                val discountedPrice = price.toInt() - ((discount.toInt() / 100) * price.toInt())
                binding.titleTv.text = bookTitle
                binding.priceTv.text = discountedPrice.toString()
                binding.descriptionTv.text = description
                binding.viewsTv.text = viewsCount
                binding.downloadsTv.text = downloadsCount
                binding.dateTv.text = date
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book is in fav or not")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
//available in favorite
                        Log.d(TAG, "onDataChange: available in favorite: ")
// set drawable top icon
                        binding.favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_fully,
                            0,
                            0
                        )
                        binding.favoriteBookBtn.text = getString(R.string.remove_favorites)
                    } else {
//not available in favorite
                        Log.d(TAG, "onDataChange: not available in favorite")
//set drawable top icon
                        binding.favoriteBookBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite,
                            0,
                            0
                        )
                        binding.favoriteBookBtn.text = getString(R.string.add_favorites)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding to favorite")
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp
        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Added to fav")
                Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //failed to add fav.
                Log.d(TAG, "addToFavorite: Failed to add to fav ${e.message}")
                Toast.makeText(
                    this,
                    "Failed to add to fav due to ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

    private fun removeFromFavorite() {
        Log.d(TAG, "removeFromFavorite: Removing from fav")
//        db ref
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavorite: Removed from fav")
                Toast.makeText(this, "Removed from favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "removeFromFavorite: Failed to remove from fav due to ${e.message}"
                )
                Toast.makeText(
                    this,
                    "Failed to remove from fav due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }
}
