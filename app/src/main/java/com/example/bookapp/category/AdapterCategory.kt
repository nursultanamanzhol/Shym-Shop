package com.example.bookapp.category

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {
    //ViewHolder class to hold/init UI Views for recycleView in row_category.xml
    private lateinit var binding: RowCategoryBinding
    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>
    private var filter: FilterCategory? = null

    //constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate /binding row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size //nums in items
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
//    get data, set, handle click etc.
        //get
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val timestamp = model.timestamp
        val uid = model.uid
        //set
        holder.categoryTv.text = category
        //handle click, delete cate.
        holder.deleteBtn.setOnClickListener {
            val build = AlertDialog.Builder(context)
            build.setTitle("Delete")
                .setMessage("Are sure you want to delete this category?")
                .setPositiveButton("Confirm") { a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()
        }

    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
//get id in category to delete
        val id = model.id
        //Firebase data > Category > id category
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }


    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // init views ui
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}