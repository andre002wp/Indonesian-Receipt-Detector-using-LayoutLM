package id.andre002wp.ReceiptScanner.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.release.gfg1.DBHelper
import com.release.gfg1.Product
import id.andre002wp.ReceiptScanner.R

class ProductAdapter (private val dataSet: ArrayList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // item_products_preview
            val name: TextView
            val qty: TextView
            val price: TextView
            val container : ConstraintLayout
            val deleteBtn : Button
            init {
                // Define click listener for the ViewHolder's View.
                name = view.findViewById(R.id.product_name)
                qty = view.findViewById(R.id.product_qty)
                price = view.findViewById(R.id.product_price)
                container = view.findViewById(R.id.container)
                deleteBtn = view.findViewById(R.id.deleteBtn)
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
            viewHolder.deleteBtn.setOnClickListener {
                dataSet.removeAt(position)
                notifyDataSetChanged()
            }
            viewHolder.container.setOnClickListener {
                lateinit var editnameholder : TextView
                lateinit var editqtyholder : TextView
                lateinit var editpriceholder : TextView

                var editDialog = MaterialAlertDialogBuilder(it.context)
                    .setTitle("Edit")
                    .setView(R.layout.dialog_edit_product)
                    .setNegativeButton("Cancel") { dialog, which ->
                        //dismiss
                        dialog.dismiss()
                    }.setPositiveButton("Edit") { dialog, which ->
                        updateProductRV(editnameholder, editqtyholder, editpriceholder, position)
                    }
                editDialog.create()
                var dialog = editDialog.show()
                editnameholder = dialog.findViewById(R.id.editstorename)!!
                editqtyholder = dialog.findViewById(R.id.editqty)!!
                editpriceholder = dialog.findViewById(R.id.editprice)!!
                editnameholder.setText(dataSet[position].getProductName())
                editqtyholder.setText(dataSet[position].getQtyString())
                editpriceholder.setText(dataSet[position].getPriceString())

            }

        }

    private fun updateProductRV(editnameholder: TextView, editqtyholder: TextView, editpriceholder: TextView, position: Int) {
        val name = editnameholder.text.toString()
        val qty = editqtyholder.text.toString().toInt()
        val price = editpriceholder.text.toString().toInt()
        var updatedProduct = Product(name, price, qty)
        dataSet[position] = updatedProduct
        notifyDataSetChanged()
    }

    // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
}

