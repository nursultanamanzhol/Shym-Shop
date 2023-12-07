package com.shym.commercial.ui.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shym.commercial.databinding.FragmentBooksUserBinding
import com.shym.commercial.adapters.AdapterPdfUser
import com.shym.commercial.data.model.ModelPdf

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    companion object {
        private const val TAG = "BOOKS_USER_TAG"

        fun newInstance(
            categoryId: String,
            category: String,
            uid: String
        ): BooksUserFragment {
            val fragment = BooksUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""
    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId") ?: ""
            category = args.getString("category") ?: ""
            uid = args.getString("uid") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBooksUserBinding.inflate(inflater, container, false)

        Log.d(TAG, "onCreateView: Category: $category")
        when {
            category == "All" -> loadAllBooks()
            category == "Most Viewed" || category == "Special discounts" ->
                loadMostViewedDownloadedBooks(category.toLowerCase())
            else -> loadCategorizedBooks()
        }
//                || category == "Most Downloaded"

        binding.searchEt.addTextChangedListener {
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        adapterPdfUser.filter.filter(s)
                    } catch (e: Exception) {
                        Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            }
        }

        return binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    model?.let { pdfArrayList.add(it) }
                }
                adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "loadAllBooks onCancelled: ${error.message}")
            }
        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy)
            .limitToLast(10)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)
                        model?.let { pdfArrayList.add(it) }
                    }

                    // Обновление пользовательского интерфейса в основном потоке
                    requireActivity().runOnUiThread {
                        adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList)
                        binding.booksRv.adapter = adapterPdfUser
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "loadMostViewedDownloadedBooks onCancelled: ${error.message}")
                }
            })
    }


    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelPdf::class.java)
                        model?.let { pdfArrayList.add(it) }
                    }
                    adapterPdfUser = AdapterPdfUser(requireContext(), pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "loadCategorizedBooks onCancelled: ${error.message}")
                }
            })
    }
}
