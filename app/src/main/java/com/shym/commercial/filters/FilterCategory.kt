package com.shym.commercial.filters

import android.os.Handler
import android.os.Looper
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import com.shym.commercial.adapters.AdapterCategory
import com.shym.commercial.data.model.ModelCategory

class FilterCategory(
    private val originalList: List<ModelCategory>,
    private val adapterCategory: AdapterCategory
) : Filter() {

    private var filteredList: List<ModelCategory> = originalList

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraintString = constraint
        val results = FilterResults()

        if (constraintString != null && constraintString.isNotEmpty()) {
            //change to upper case, or lower case to avoid case sensitivity
            constraintString = constraintString.toString().uppercase()
            val filterModels = originalList.filter {
                it.category.uppercase().contains(constraintString)
            }
            results.values = filterModels
        } else {
            results.values = originalList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        val newFilteredList = results.values as List<ModelCategory>

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = filteredList.size
            override fun getNewListSize(): Int = newFilteredList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return filteredList[oldItemPosition] == newFilteredList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return filteredList[oldItemPosition] == newFilteredList[newItemPosition]
            }
        })

        filteredList = newFilteredList

        // Оповещение адаптера асинхронно с использованием Handler и Looper
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            diffResult.dispatchUpdatesTo(adapterCategory)
        }
    }
}
