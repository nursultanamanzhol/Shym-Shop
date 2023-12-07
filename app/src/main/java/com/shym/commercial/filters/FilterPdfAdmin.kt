package com.shym.commercial.filters


import android.os.Handler
import android.os.Looper
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import com.shym.commercial.adapters.AdapterPdfAdmin
import com.shym.commercial.data.model.ModelPdf

class FilterPdfAdmin(
    private val originalList: List<ModelPdf>,
    private val adapterPdfAdmin: AdapterPdfAdmin
) : Filter() {

    private var filteredList: List<ModelPdf> = originalList

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val constraintString = constraint?.toString()?.lowercase()

        val results = FilterResults()

        if (constraintString != null && constraintString.isNotEmpty()) {
            val filterModels = originalList.filter {
                it.title.lowercase().contains(constraintString)
            }
            results.values = filterModels
        } else {
            results.values = originalList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        val newFilteredList = results.values as List<ModelPdf>

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
            diffResult.dispatchUpdatesTo(adapterPdfAdmin)
        }
    }
}
