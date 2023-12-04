package com.shym.commercial.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.shym.commercial.filters.FilterPdfAdmin
import com.shym.commercial.databinding.RowPdfAdminBinding
import com.shym.commercial.models.ModelPdf
import com.shym.commercial.pdflist.MyApplication
import com.shym.commercial.ui.PdfDetailsActivity
import com.shym.commercial.ui.PdfEditActivity

class AdapterPdfAdmin :RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    //context
    private var context: Context
    //arraylist to hold pdf
    public var pdfArrayList: ArrayList<ModelPdf>
    private var filterList: ArrayList<ModelPdf>
    //viewBinding
    private lateinit var binding: RowPdfAdminBinding
    //filter object
    private var filter : FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }



    /*View holder class for row_pdf_admin.xml*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //bind/ inflate layout row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //item count
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        /*----- Get data,  set data, handle click etc---*/
        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val price = model.price
        val discount = model.discount
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp to dd/mm/yyyy format

        val formattedDate = MyApplication.formatTimeStamp(timestamp)
        holder.titleTv.text = title
        holder.priceTv.text = price

// Предполагаем, что price и discount представляют числовые значения
        val discountedPrice = price.toDouble() - ((discount.toDouble() / 100) * price.toDouble())

        holder.discountTv.text = discountedPrice.toString()

// Управление видимостью элементов в зависимости от условий
        if (price.toDouble() == discountedPrice) {
            holder.discountTv.visibility = View.GONE
            holder.imageRedLine.visibility = View.GONE
        } else {
            holder.discountTv.visibility = View.VISIBLE
            holder.imageRedLine.visibility = View.VISIBLE
        }

        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
        //load further detail like category, pdf url, pdf size

        //load category
        MyApplication.loadCategory(categoryId, holder.categoryTv)


        //we dont  need page number here, pas null for page number  || load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.imageView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        //handle item click, open PdfDetailsActivity activity, lets create it first
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId", bookId) // will be used to load book details
            context.startActivity(intent)
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        //get id, url, title of book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose options: ")
            .setItems(options){dialog, position ->
                //handle item click
                if(position == 0){
                    //Edit is clicked, lets create activity to edit
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId) //passed id in book, will be used to edit the book
                    context.startActivity(intent)

                }else if (position == 1){
                    //delete

                    //show confirmation dialog first if you need...
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }


    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        /*UI Views of row_pdf_admin.xml*/

        val imageView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val priceTv = binding.priceTv
        val imageRedLine = binding.imageRedLine
        val discountTv = binding.discountTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }
}