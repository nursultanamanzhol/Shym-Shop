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
import com.shym.commercial.databinding.RowMainDiscountBinding
import com.shym.commercial.extensions.MyApplication
import com.shym.commercial.extensions.setSafeOnClickListener
import com.shym.commercial.filters.FilterMainPage
import com.shym.commercial.filters.FilterMainSpecial
import com.shym.commercial.ui.pdf.PdfDetailsActivity

class AdapterMainSpecial(
    private val context: Context,
    var pdfArrayList: ArrayList<ModelPdf>
) : RecyclerView.Adapter<AdapterMainSpecial.HolderMainSpecial>(), Filterable {

    private var filterList: ArrayList<ModelPdf> = pdfArrayList
    private var filter: FilterMainSpecial? = null
    private lateinit var binding: RowMainDiscountBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMainSpecial {
        binding = RowMainDiscountBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderMainSpecial(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderMainSpecial, position: Int) {
        val model = pdfArrayList[position]
        //get data
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val price = model.price
        val discount = model.discount
        val uid = model.uid
        val pdfUrl = model.url
        val timestamp = model.timestamp
        // ... Остальной код без изменений
        holder.titleTv.text = title
        holder.priceTv.text = price.toString()
        holder.discount.text = discount.toString()

        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, PdfDetailsActivity::class.java)
//            intent.putExtra("bookId", model.id)
//            context.startActivity(intent)
//        }
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterMainSpecial(filterList, this)
        }
        return filter as FilterMainSpecial
    }

    inner class HolderMainSpecial(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var discount = binding.discount
        val priceTv = binding.priceTv

        init {
            // Устанавливаем обработчик клика на itemView, который включает в себя pdfView
            itemView.setSafeOnClickListener {
                val intent = Intent(context, PdfDetailsActivity::class.java)
                intent.putExtra("bookId", pdfArrayList[adapterPosition].id)
                context.startActivity(intent)
            }

            // Если вы хотите также обрабатывать клики на pdfView, раскомментируйте следующую строку:
            // pdfView.setSafeOnClickListener { /* ваш код обработки клика на pdfView */ }
        }

    }
}
