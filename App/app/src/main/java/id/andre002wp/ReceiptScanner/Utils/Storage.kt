package id.andre002wp.ReceiptScanner.Utils

import android.graphics.Bitmap
import android.os.Environment
import id.andre002wp.ReceiptScanner.MainActivity
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class Storage(mainstorage: MainActivity.Companion) {
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun getImagePath(id: Int): String {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/ReceiptScanner"
        val file = File(path, "image_$id.jpg")
        return file.absolutePath
    }

    fun saveImage(id: Int, bitmap: Bitmap): String {
        try {
            val path = Environment.getExternalStorageDirectory().absolutePath + "/ReceiptScanner"
            val file = File(path, "image_$id.jpg")
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            val out: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}