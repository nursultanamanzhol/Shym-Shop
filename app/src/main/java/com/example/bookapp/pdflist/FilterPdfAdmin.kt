package com.example.bookapp.pdflist

import android.widget.Filter


/*used to filter data from recyclerview | search pdf from pdf list in recyclerview*/

class FilterPdfAdmin : Filter {
    //arrayList in which we want to search
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint //value search
        val results = FilterResults()
        //value to be searched should not be null and not empty
        if (constraint!= null && constraint.isNotEmpty()){
            //change to upper case, or lowercase to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            var filterModels = ArrayList<ModelPdf>()
            for (i in filterList.indices){
                //validate if match
                if (filterList[i].title.lowercase().contains(constraint)){
                    // searched value is similar to value in list, add to filtered list
                    filterModels.add(filterList[i])
                }
            }
            results.count = filterModels.size
            results.values = filterModels
        }
        else{
            //searched value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }

        return results  //  do not miss
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }
}