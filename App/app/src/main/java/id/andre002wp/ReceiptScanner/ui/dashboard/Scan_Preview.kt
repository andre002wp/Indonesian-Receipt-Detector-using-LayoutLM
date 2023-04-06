package id.andre002wp.ReceiptScanner.ui.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import kotlin.math.roundToInt

class Scan_Preview : AppCompatActivity() {

    private lateinit var binding: ActivityScanPreviewBinding
    private var editflag = false
    private var id = -1
    private lateinit var storage : Storage
    private lateinit var productAll : ArrayList<Product>
    private var image: Bitmap? = null

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
        var image_guidev2 = binding.imageGuidev2
        var rvguide = binding.guideline2
        // get screensize
        val displaymetrics = resources.displayMetrics
        val height = displaymetrics.heightPixels

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
                image = MainActivity.result_bitmap
                imageholder.setImageBitmap(Bitmap.createScaledBitmap(image!!, image!!.width, image!!.height, false))
            }
        }
        else{
            try {
                val imagepath = storage.getImagePath(id)
                Log.d("storage","getting image : $imagepath")
                val imagefile = File(imagepath).readBytes()
                image = BitmapFactory.decodeByteArray(imagefile, 0, imagefile.size)
                imageholder.setImageBitmap(Bitmap.createScaledBitmap(image!!, image!!.width, image!!.height, false))
            }
            catch (e: IOException){
                Log.d("storage","image not found")
                // set image not found
                imageholder.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.imagenotfound)
                )
            }
        }

        store_holder.setText(store)
        store_holder.maxLines = 1
        date_holder.setText(date)
        date_holder.maxLines = 1
        time_holder.setText(time)
        time_holder.maxLines = 1
        total_holder.setText(thousandSeparator(total))


        // create product adapter and set data to adapter
        products_holder.adapter = ProductAdapter(products)
        // set adapter to recycler view
        products_holder.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        addbtn.setOnClickListener{
            var addproduct = Product("",0,0)
            products.add(addproduct)
            products_holder.adapter?.notifyDataSetChanged()
        }


        imageholder.setOnClickListener(){
            if (image != null){
                val imagedialog = MaterialAlertDialogBuilder(this)
                    .setView(R.layout.dialog_image)
                imagedialog.create()
                var dialog = imagedialog.show()
                var bigimageHolder:ImageView = dialog.findViewById(R.id.image_dialog)!!
                bigimageHolder.setImageBitmap(Bitmap.createScaledBitmap(image!!, image!!.width, image!!.height, false))
            }
        }

        cardRemove.setOnClickListener {
            if(editflag == true){
                try {
                    if(storage.isExternalStorageReadable() && storage.isExternalStorageWritable()){
                        val filename = storage.getImagePath(id)
                        val file = File(filename)
                        file.delete()
                    }
                }
                catch (e: IOException){
                    Log.d("storage","image not found")
                }
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
            val final_total = thousandCombiner(total_holder.text.toString())

            if (final_store.equals("")){Log.d("DB","Store is empty")}
            if (final_date.equals("")){Log.d("DB","Date is empty")}
            if (final_time.equals("")){Log.d("DB","Time is empty")}
            if (final_total < 1){Log.d("DB","Total is empty")}

            if (!final_store.equals("") && !final_date.equals("") && !final_time.equals("") && final_total > 0 && products.size > 0) {
                if (editflag == true){
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
    private fun thousandSeparator(monthspending: Int): String {
        return monthspending.toString().reversed().chunked(3).joinToString(".").reversed()
    }

    private fun thousandCombiner(monthspending: String): Int {
        return monthspending.replace(".","").toInt()
    }
}