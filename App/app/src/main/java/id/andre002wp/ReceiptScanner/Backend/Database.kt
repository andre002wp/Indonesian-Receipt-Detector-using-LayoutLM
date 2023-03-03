package com.release.gfg1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import java.util.*

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + RECEIPT + " (" +
                id_receipt + " INTEGER PRIMARY KEY, " +
                store_name + " TEXT," +
                purchase_date + " TEXT," +
                purchase_time + " TEXT," +
                total_payment + " INTEGER" + ")")

        val query2 = ("CREATE TABLE " + PRODUCTSDETAILS + " (" +
                id_detproduct + " INTEGER PRIMARY KEY, " +
                receipt_id + " INTEGER, " +
                product_name + " TEXT," +
                product_quantity + " INTEGER," +
                product_sum_price + " INTEGER" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
        db.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + RECEIPT)
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTSDETAILS)
        onCreate(db)
    }

    // This method is for adding data in our database
    fun addReceipt(store : String,date : String, time: String, total : Int, products : ArrayList<Product>){

        // below we are creating
        // a content values variable
        val Receipt = ContentValues()

        // we are inserting our values
        Receipt.put(store_name, store)
        Receipt.put(purchase_date, date)
        Receipt.put(purchase_time, time)
        Receipt.put(total_payment, total)

        // here we are creating a writable variable of our database
        // as we want to insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(RECEIPT, null, Receipt)
        val receipt_new_id = db.rawQuery("SELECT MAX(id_receipt) FROM " + RECEIPT, null)

        for (product in products) {
            addProductsDetails(receipt_new_id.getInt(0), product.name, product.price, product.quantity)
        }

        // at last we are
        // closing our database
        db.close()
    }

    // This method is for adding data in our database
    fun addReceipt(receipt: Receipt){
        val Receipt = ContentValues()

        // we are inserting our values
        Receipt.put(store_name, receipt.getStoreName())
        Receipt.put(purchase_date, receipt.getPurchaseDate())
        Receipt.put(purchase_time, receipt.getPurchaseTime())
        Receipt.put(total_payment, receipt.getTotalPayment())

        // here we are creating a writable variable of our database
        // as we want to insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(RECEIPT, null, Receipt)
        val receipt_new_id = db.rawQuery("SELECT MAX(id_receipt) FROM " + RECEIPT, null)

        for (product in receipt.products) {
            addProductsDetails(receipt_new_id.getInt(0), product.name, product.price, product.quantity)
        }

        // at last we are
        // closing our database
        db.close()
    }

    fun addProductsDetails(r_id : Int, name : String, price : Int, qty : Int){

        // below we are creating
        // a content values variable
        val product_val = ContentValues()

        // we are inserting our values
        product_val.put(receipt_id, r_id)
        product_val.put(product_name, name)
        product_val.put(product_sum_price, price)
        product_val.put(product_quantity, qty)

        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(PRODUCTSDETAILS, null, product_val)

        db.close()
    }

    fun getallReceipts() : ArrayList<Receipt>{
        val receipts = ArrayList<Receipt>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + RECEIPT, null)
        while (cursor.moveToNext()){
            val receipt = Receipt(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4))

            // get all details for the receipt
            val cursor2 = db.rawQuery("SELECT * FROM " + PRODUCTSDETAILS + " WHERE " + receipt_id + " = " + receipt.id, null)
            while (cursor2.moveToNext()){
                val product = Product(cursor2.getString(2), cursor2.getInt(4), cursor2.getInt(3))
                receipt.products.add(product)
            }
            receipts.add(receipt)
        }
        cursor.close()
        db.close()
        return receipts
    }

    fun getReceipt(id : Int) : Receipt{
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM " + RECEIPT + " WHERE " + id_receipt + " = " + id, null)
        cursor.moveToFirst()
        val receipt = Receipt(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4))
        // get all details for the receipt
        val cursor2 = db.rawQuery("SELECT * FROM " + PRODUCTSDETAILS + " WHERE " + receipt_id + " = " + id, null)
        while (cursor2.moveToNext()){
            val product = Product(cursor2.getString(2), cursor2.getInt(4), cursor2.getInt(3))
            receipt.products.add(product)
        }

        Log.d("getReceipt", "Receipt"+receipt.getStoreName()+" has total of"+receipt.products.size+" products")
        cursor.close()
        db.close()
        return receipt
    }


    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "GEEKS_FOR_GEEKS"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // below is the variable for table name
        val RECEIPT = "receipt_table"
        val PRODUCTSDETAILS = "products_details_table"

        // below is the variable for RECEIPT table
        val id_receipt = "id_receipt"
        val store_name = "store_name"
        val purchase_date = "date"
        val purchase_time = "time"
        val total_payment = "total"

        // below is the variable for PRODUCTSDETAILS table
        val id_detproduct = "id_detproduct"
        val receipt_id = "receipt_id"
        val product_name = "product_name"
        val product_sum_price = "product_price"
        val product_quantity = "qty"
    }
}

class Receipt(var id : Int, var store : String, var date : String, var time : String, var total : Int) {
    val products = ArrayList<Product>()
    constructor(id: Int, store: String, date: String, time: String, total: Int, products: ArrayList<Product>) : this(id, store, date, time, total) {
        this.products.addAll(products)
    }

    fun getStoreName() : String{
        return store
    }

    fun getPurchaseDate() : String{
        return date
    }

    fun getPurchaseTime() : String{
        return time
    }

    fun getTotalPayment() : String{
        return total.toString()
    }

    fun setStoreName(store : String){
        this.store = store
    }

    fun setPurchaseDate(date : String){
        this.date = date
    }

    fun setPurchaseTime(time : String){
        this.time = time
    }

    fun setTotalPayment(total : Int){
        this.total = total
    }

    fun setTotalPayment(total : String){
        this.total = total.toInt()
    }

    fun setTotalPayment(total : Double){
        this.total = total.toInt()
    }

    fun addProduct(product : Product){
        products.add(product)
    }

}

@Parcelize
class Product(var name: String, var price: Int, var quantity: Int) : Parcelable {

    fun getQtyString() : String{
        return quantity.toString()
    }

    fun getPriceString() : String{
        return price.toString()
    }

    fun getProductName() : String{
        return name
    }

    fun setProductName(name : String){
        this.name = name
    }

    fun setProductPrice(price : Int){
        this.price = price
    }

    fun setProductQty(qty : Int){
        this.quantity = qty
    }

    fun setProductPrice(price : String){
        this.price = price.toInt()
    }

    fun setProductQty(qty : String){
        this.quantity = qty.toInt()
    }

    fun setProductPrice(price : Double){
        this.price = price.toInt()
    }

}
