package com.shym.bookapp.users_role.customer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shym.bookapp.databinding.RowPdfUserBinding
import com.shym.bookapp.models.ModelPdf
import com.shym.bookapp.pdflist.MyApplication
import com.shym.bookapp.pdflist.PdfDetailsActivity
import com.shym.bookapp.users_role.customer.FilterPdfUser

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfIUser>, Filterable {
    private lateinit var context: Context
    lateinit var pdfArrayList: ArrayList<ModelPdf>
    lateinit var filterList: ArrayList<ModelPdf>
    private lateinit var binding: RowPdfUserBinding
    private var filter: FilterPdfUser? = null


    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    constructor() : super()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfIUser {
        //infalte
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfIUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //return list size/ number of records
    }

    override fun onBindViewHolder(holder: HolderPdfIUser, position: Int) {
        /*get data, set data, handle click etc...*/


        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val price = model.price
        val uid = model.uid
        val pdfUrl = model.url
        val timestamp = model.timestamp

        // convert time
        val date = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.priceTv.text = price
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        ) // no need number of pages so pass bull

        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //handle click, open pdf details page
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)

        }
        return filter as FilterPdfUser
    }

    //row xml
    inner class HolderPdfIUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Ui components
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        val priceTv = binding.priceTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }


}