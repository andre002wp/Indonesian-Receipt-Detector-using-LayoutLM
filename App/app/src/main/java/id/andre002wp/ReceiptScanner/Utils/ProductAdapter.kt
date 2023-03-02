package id.andre002wp.ReceiptScanner.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.release.gfg1.Product
import id.andre002wp.ReceiptScanner.R

class ProductAdapter(private val dataSet: ArrayList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>(){
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // item_products_preview
            val name: TextView
            val qty: TextView
            val price: TextView
            init {
                // Define click listener for the ViewHolder's View.
                name = view.findViewById(R.id.product_name)
                qty = view.findViewById(R.id.product_qty)
                price = view.findViewById(R.id.product_price)
            }
        }

    // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_products_preview, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.name.text = dataSet[position].getProductName()
            viewHolder.qty.text = dataSet[position].getQtyString()
            viewHolder.price.text = dataSet[position].getPriceString()
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
}

