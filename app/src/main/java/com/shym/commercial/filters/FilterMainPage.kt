package com.shym.commercial.filters


import android.annotation.SuppressLint
import android.widget.Filter
import com.shym.commercial.adapters.AdapterMainPage
import com.shym.commercial.data.model.ModelPdf

class FilterMainPage : Filter {

    var filterList: ArrayList<ModelPdf>
    var adapterMainPage: AdapterMainPage

    constructor(filterList: ArrayList<ModelPdf>, adapterMainPage: AdapterMainPage) : super() {
        this.filterList = filterList
        this.adapterMainPage = adapterMainPage
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint

        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()){
            // not null nor empty

            //change
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                if (filterList[i].title.uppercase().contains(constraint))
                //searched value matched with title, add to list
                    filteredModels.add(filterList[i])

            } // return filtered
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            // either it is null or is empty
            // return original list and size
            results.count = filterList.size
            results.values = filterList
        }
        return results

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // app filter changes
        adapterMainPage.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterMainPage.notifyDataSetChanged()
    }


}

