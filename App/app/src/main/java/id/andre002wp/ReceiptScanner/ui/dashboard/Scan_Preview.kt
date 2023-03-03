package id.andre002wp.ReceiptScanner.ui.dashboard

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.release.gfg1.DBHelper
import com.release.gfg1.Product
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ProductAdapter
import id.andre002wp.ReceiptScanner.databinding.ActivityScanPreviewBinding

class Scan_Preview : AppCompatActivity() {

    private lateinit var binding: ActivityScanPreviewBinding
    private var editflag = false
    private var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var store_holder = binding.storeName
        var date_holder = binding.Date
        var time_holder = binding.Time
        var total_holder = binding.Total
        var products_holder = binding.rvProductsPreview

        var savebtn = binding.saveBtn
        var cancelbtn = binding.cancelBtn
        var addbtn = binding.addBtn

        var store = intent.getStringExtra("store_name")
        var date = intent.getStringExtra("date")
        var time = intent.getStringExtra("time")
        var total = intent.getIntExtra("total",-1)
        val products = intent.getParcelableArrayListExtra<Parcelable>("products")

        if (intent.hasExtra("editflag")){
            this.editflag = intent.getBooleanExtra("editflag",false)
            this.id = intent.getIntExtra("id",-1)
            Log.d("DB","Edit Receipt : $id with product size ${products?.size}")
        }

        store_holder.setText(store)
        date_holder.setText(date)
        time_holder.setText(time)
        total_holder.setText(total.toString())

        // create product adapter and set data to adapter
        products_holder.adapter = ProductAdapter(products as ArrayList<Product>)
        // set adapter to recycler view
        products_holder.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        cancelbtn.setOnClickListener {
            if(editflag == true){
                val db = DBHelper(this, null)
                db.deleteReceipt(id)
                finish()
            }
            else{
                Snackbar.make(binding.root, "Cancel", Snackbar.LENGTH_LONG)
                    .setAction("canceling", null).show()
                finish()
            }
        }

        savebtn.setOnClickListener {
            //add new receipt to database
            if (store != null && date != null && time != null && total != -1 && products.size > 0) {
                if (editflag == true){
                    val new_receipt = Receipt(id,store, date, time, total, products)
                    val db = DBHelper(this, null)
                    db.updateReceipt(new_receipt)
                }
                else {
                    val new_receipt = Receipt(0, store, date, time, total, products)
                    val db = DBHelper(this, null)
                    db.addReceipt(new_receipt)
                }
            }
        }
    }
}