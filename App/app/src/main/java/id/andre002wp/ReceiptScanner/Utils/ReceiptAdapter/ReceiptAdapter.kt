package id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.release.gfg1.Product
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ProductAdapter
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview

class ReceiptAdapter(private val dataSet: ArrayList<Receipt>) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // item_products_preview
        val store_name: TextView
        val dateholder: TextView
        val timeholder: TextView
        val total: TextView
        val id: TextView
        var itemHolder : ConstraintLayout
        init {
            // Define click listener for the ViewHolder's View.
            store_name = view.findViewById(R.id.store_name)
            dateholder = view.findViewById(R.id.purchase_date)
            total = view.findViewById(R.id.total)
            timeholder = view.findViewById(R.id.purchase_time)
            id = view.findViewById(R.id.idplaceholder)
            itemHolder = view.findViewById(R.id.purchase_history_item)
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
        viewHolder.total.text = dataSet[position].getTotalPayment().toString()
        viewHolder.id.text = dataSet[position].getID().toString()
        viewHolder.itemHolder.setOnClickListener(View.OnClickListener {
            var goToHistoryDetail = Intent(viewHolder.itemView.context, Scan_Preview::class.java)
            goToHistoryDetail.putExtra("editflag",true)
            goToHistoryDetail.putExtra("id", dataSet[position].getID())

            goToHistoryDetail.putExtra("store_name", dataSet[position].getStoreName())
            goToHistoryDetail.putExtra("date", dataSet[position].getPurchaseDate())
            goToHistoryDetail.putExtra("time", dataSet[position].getPurchaseTime())
            goToHistoryDetail.putExtra("total", dataSet[position].getTotalPayment())
            goToHistoryDetail.putExtra("products", dataSet[position].products)
            viewHolder.itemView.context.startActivity(goToHistoryDetail)
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    public interface editReceiptListener {
        fun onItemClick(item: Receipt)
    }
}
