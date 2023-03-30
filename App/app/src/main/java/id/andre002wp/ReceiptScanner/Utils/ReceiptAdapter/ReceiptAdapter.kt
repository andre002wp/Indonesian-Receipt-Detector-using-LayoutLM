package id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.release.gfg1.Product
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.MainActivity
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ProductAdapter
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview
import id.andre002wp.ReceiptScanner.ui.history.HistoryFragment
import java.io.File

class ReceiptAdapter(private val dataSet: ArrayList<Receipt>, private var onEditReceipt: EditReceiptListener) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>(),
    Filterable {
    private var receiptListFull: ArrayList<Receipt> = ArrayList(dataSet)
    private var storage = MainActivity.storage

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // item_products_preview
        val store_name: TextView
        val dateholder: TextView
        val timeholder: TextView
        val total: TextView
        val id: TextView
        val receiptImage : ImageView
        var itemHolder : ConstraintLayout
        init {
            // Define click listener for the ViewHolder's View.
            store_name = view.findViewById(R.id.store_name)
            dateholder = view.findViewById(R.id.purchase_date)
            total = view.findViewById(R.id.total)
            timeholder = view.findViewById(R.id.purchase_time)
            id = view.findViewById(R.id.idplaceholder)
            itemHolder = view.findViewById(R.id.purchase_history_item)
            receiptImage = view.findViewById(R.id.receipt_image)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ReceiptAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_purchase_history, viewGroup, false)

        return ReceiptAdapter.ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ReceiptAdapter.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.store_name.text = dataSet[position].getStoreName()
        viewHolder.dateholder.text = dataSet[position].getPurchaseDate()
        viewHolder.timeholder.text = dataSet[position].getPurchaseTime()
        viewHolder.total.text = thousandSeparator(dataSet[position].getTotalPayment())
        if(storage.isExternalStorageReadable()){
            try {
                val imagepath = storage.getImagePath(dataSet[position].getID())
                val bitmap = File(imagepath).readBytes()
                viewHolder.receiptImage.setImageBitmap(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size))
            }
            catch (e: Exception){
                viewHolder.receiptImage.setImageResource(R.drawable.imagenotfound)
            }
        }
        viewHolder.id.text = dataSet[position].getID().toString()
        viewHolder.itemHolder.setOnClickListener(View.OnClickListener {
            onEditReceipt.onEditReceipt(dataSet[position])
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    override fun getFilter(): Filter {
        return receiptFilter
    }

    private val receiptFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<Receipt> = ArrayList()
            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(receiptListFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim { it <= ' ' }
                for (item in receiptListFull) {
                    if (item.getStoreName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            dataSet.clear()
            dataSet.addAll(results?.values as ArrayList<Receipt>)
            notifyDataSetChanged()
        }
    }

    public interface EditReceiptListener {
        fun onEditReceipt(item: Receipt)
    }

    private fun thousandSeparator(monthspending: Int): String {
        return monthspending.toString().reversed().chunked(3).joinToString(".").reversed()
    }
}

