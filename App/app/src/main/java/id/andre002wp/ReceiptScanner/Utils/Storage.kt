package id.andre002wp.ReceiptScanner.Utils

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat.getExternalFilesDirs
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
        val path = MainActivity.external_dir
        val file = File(path, "image_$id.jpg")
        return file.absolutePath
    }

    fun saveImage(id: Int, bitmap: Bitmap): String {
        try {
            val path = MainActivity.external_dir
            if (File(path).exists()) {
                Log.d("Storage", "Directory $path exists")
            }
            else {
                Log.d("Storage", "Directory $path does not exist making a new one")
                File(path).mkdirs()
            }
            val file = File(path, "image_$id.jpg")
            if (!file.exists()) {
                Log.d("Storage", "creating new file $file")
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