package com.shym.bookapp.user

import android.widget.Filter
import com.shym.bookapp.pdflist.ModelPdf

class FilterPdfUser : Filter {

    var filterList: ArrayList<ModelPdf>
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
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

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // app filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()
    }


}