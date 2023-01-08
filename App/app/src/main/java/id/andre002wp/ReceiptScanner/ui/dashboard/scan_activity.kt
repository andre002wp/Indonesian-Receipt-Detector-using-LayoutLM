package id.andre002wp.ReceiptScanner.ui.dashboard

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.andre002wp.ReceiptScanner.R

class scan_activity : AppCompatActivity() {
    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
    }
}