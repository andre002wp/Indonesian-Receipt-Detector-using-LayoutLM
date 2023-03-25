package id.andre002wp.ReceiptScanner.ui.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.release.gfg1.DBHelper
import com.release.gfg1.Product
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.MainActivity
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ProductAdapter
import id.andre002wp.ReceiptScanner.Utils.Storage
import id.andre002wp.ReceiptScanner.databinding.ActivityScanPreviewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class Scan_Preview : AppCompatActivity() {

    private lateinit var binding: ActivityScanPreviewBinding
    private var editflag = false
    private var id = -1
    private lateinit var storage : Storage
    private lateinit var productAll : ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storage = MainActivity.storage

        var store_holder = binding.storeName
        var date_holder = binding.Date
        var time_holder = binding.Time
        var total_holder = binding.Total
        var products_holder = binding.rvProductsPreview
        var imageholder = binding.imageViewReceipt
        val cardSave = binding.cardSave
        val cardRemove = binding.cardRemove
        val tvRemove = binding.tvRemove
        val tvSave = binding.tvSave
        var addbtn = binding.addBtn

        var store = intent.getStringExtra("store_name")
        var date = intent.getStringExtra("date")
        var time = intent.getStringExtra("time")
        var total = intent.getIntExtra("total",-1)

        // get product and product as filter
        productAll = intent.getParcelableArrayListExtra<Parcelable>("products") as ArrayList<Product>
        val products = productAll

        if (intent.hasExtra("editflag")){
            this.editflag = intent.getBooleanExtra("editflag",false)
        }

        if (intent.hasExtra("id")){
            this.id = intent.getIntExtra("id",-1)
            Log.d("DB","Edit Receipt : $id with product size ${products?.size}")
        }

        if (editflag == true){
            tvSave.text = "Edit"
            tvRemove.text = "Delete"
        }else{
            tvSave.text = "Save"
            tvRemove.text = "Cancel"
        }

        // get image from scan activity
        if (editflag == false){
            if (MainActivity.isBitmapInitialized()){
                val image = MainActivity.result_bitmap
                imageholder.setImageBitmap(image)
            }
        }
        else{
            try {
                val imagepath = storage.getImagePath(id)
                Log.d("storage","getting image : $imagepath")
                val image = File(imagepath).readBytes()
                imageholder.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(image, 0, image.size), 1000, 1000, false))
            }
            catch (e: IOException){
                Log.d("storage","image not found")
                // set image not found
                imageholder.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.imagenotfound)
                )
            }
        }

        store_holder.setText(store)
        date_holder.setText(date)
        time_holder.setText(time)
        total_holder.setText(total.toString())

        // create product adapter and set data to adapter
        products_holder.adapter = ProductAdapter(products)
        // set adapter to recycler view
        products_holder.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        addbtn.setOnClickListener{
            var addproduct = Product("",0,0)
            products.add(addproduct)
            products_holder.adapter?.notifyDataSetChanged()
        }


//
        cardRemove.setOnClickListener {
            if(editflag == true){
                val db = DBHelper(this, null)
                db.deleteReceipt(id)
                setResult(RESULT_OK)
                finish()
            }
            else{
                Snackbar.make(binding.root, "Cancel", Snackbar.LENGTH_LONG)
                    .setAction("canceling", null).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        cardSave.setOnClickListener(){
            //add new receipt to database
            val final_store = store_holder.text.toString()
            val final_date = date_holder.text.toString()
            val final_time = time_holder.text.toString()
            val final_total = total_holder.text.toString().toInt()

            if (final_store.equals("")){Log.d("DB","Store is empty")}
            if (final_date.equals("")){Log.d("DB","Date is empty")}
            if (final_time.equals("")){Log.d("DB","Time is empty")}
            if (final_total < 1){Log.d("DB","Total is empty")}

            if (!final_store.equals("") && !final_date.equals("") && !final_time.equals("") && final_total > 0 && products.size > 0) {
                if (editflag == true){
                    if(storage.isExternalStorageReadable() && storage.isExternalStorageWritable()){
                        val filename = "img/$id.png"
                        val file = getFileStreamPath(filename)
                        file.delete()
                    }
                    val new_receipt = Receipt(id,final_store,final_date,final_time,final_total,products)
                    val db = DBHelper(this, null)
                    db.updateReceipt(new_receipt)
                    setResult(RESULT_OK)
                }
                else {
                    val new_receipt = Receipt(0, final_store, final_date, final_time, final_total, products)
                    val db = DBHelper(this, null)
                    val new_id = db.addReceipt(new_receipt)
                    setResult(RESULT_OK)
                    // then save image to internal storage
                    if (MainActivity.isBitmapInitialized()) {
                        val saveimg = MainActivity.result_bitmap
                        val filepath = storage.saveImage(new_id, saveimg)
                        Log.d("storage", "saving image to : $filepath")
                    }
                }
                Snackbar.make(binding.root, "Saving", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", null).show()
                finish()
            }
            else{
                Snackbar.make(binding.root, "Please fill all the fields", Snackbar.LENGTH_LONG)
                    .setAction("canceling", null).show()
            }
        }

    }
}