package com.shym.commercial.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shym.commercial.data.model.ModelPdf
import com.shym.commercial.databinding.RowMainPageBinding
import com.shym.commercial.databinding.RowPdfUserBinding
import com.shym.commercial.extensions.MyApplication
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.filters.FilterMainPage
import com.shym.commercial.ui.pdf.PdfDetailsActivity

class AdapterMainPage(
    private val context: Context,
    var pdfArrayList: ArrayList<ModelPdf>
) : RecyclerView.Adapter<AdapterMainPage.HolderMainPage>(), Filterable {

    private var filterList: ArrayList<ModelPdf> = pdfArrayList
    private var filter: FilterMainPage? = null
    private lateinit var binding: RowMainPageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMainPage {
        binding = RowMainPageBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderMainPage(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderMainPage, position: Int) {
        val model = pdfArrayList[position]
        //get data
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val price = model.price
        val discount = model.discount.toIntOrNull() ?: 0
        val uid = model.uid
        val pdfUrl = model.url
        val timestamp = model.timestamp
        // ... Остальной код без изменений
        holder.priceTv.text = price
        if ( discount == 0) {
            holder.discount.visibility = View.GONE
        } else {
            holder.discount.visibility = View.VISIBLE
            holder.discount.text = discount.toString()
        }
        holder.titleTv.text = title

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        holder.itemView.setSafeOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterMainPage(filterList, this)
        }
        return filter as FilterMainPage
    }

    inner class HolderMainPage(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var priceTv = binding.priceTv
        var discount = binding.discount
//        var descriptionTv = binding.descriptionTv
//        val priceTv = binding.priceTv
//        val imageRedLine = binding.imageRedLine
//        var categoryTv = binding.categoryTv
//        var sizeTv = binding.sizeTv
//        var dateTv = binding.dateTv
    }
}
