package com.shym.commercial.filters


import android.widget.Filter
import com.shym.commercial.adapters.AdapterMainPage
import com.shym.commercial.adapters.AdapterMainSpecial
import com.shym.commercial.data.model.ModelPdf

class FilterMainSpecial : Filter {

    var filterList: ArrayList<ModelPdf>
    var adapterMainSpecial: AdapterMainSpecial

    constructor(filterList: ArrayList<ModelPdf>, adapterMainSpecial: AdapterMainSpecial) : super() {
        this.filterList = filterList
        this.adapterMainSpecial = adapterMainSpecial
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
        adapterMainSpecial.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterMainSpecial.notifyDataSetChanged()
    }


}

