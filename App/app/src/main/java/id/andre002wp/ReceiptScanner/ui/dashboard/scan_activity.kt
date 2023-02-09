package id.andre002wp.ReceiptScanner.ui.dashboard

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.websitebeaver.documentscanner.DocumentScanner
import com.websitebeaver.documentscanner.constants.ResponseType
import id.andre002wp.ReceiptScanner.Backend.ApiConfig.Companion.getApiService
import id.andre002wp.ReceiptScanner.Backend.FileUploadResponse
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.uriToFile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class scan_activity : AppCompatActivity() {
    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var croppedImageView: ImageView
    private lateinit var btn_scan: Button
    private lateinit var result_bitmap:Bitmap

    private val documentScanner = DocumentScanner(
        this,
        { croppedImageResults ->
            // display the first cropped image
            croppedImageView.setImageBitmap(
                BitmapFactory.decodeFile(croppedImageResults.first())
            )
        },
        {
            // an error happened
                errorMessage -> Log.v("documentscannerlogs", errorMessage)
        },
        {
            // user canceled document scan
            Log.v("documentscannerlogs", "User canceled document scan")
        },
        ResponseType.IMAGE_FILE_PATH,
        true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // cropped image
        croppedImageView = findViewById(R.id.cropped_image_view)
        btn_scan = findViewById(R.id.btnScan)

        val isPhoto = intent.getIntExtra("isPhoto",-1)

        if (isPhoto != -1){
            if (isPhoto == 1){
                // start document scan
                documentScanner.startScan()
            }
            else{
                startGallery()
            }
        }
        else{
            Log.e("Scan Invalid","Picture not defined")
        }

        btn_scan.setOnClickListener{
            uploadImage(croppedImageView.drawable.toBitmap())
        }
    }


    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            var bitmap: Bitmap? = null
            if(Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    selectedImg
                )
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, selectedImg)
                bitmap = ImageDecoder.decodeBitmap(source)
            }
            croppedImageView.setImageBitmap(bitmap)
        }
    }

    private fun uploadImage(bitimg: Bitmap) {
        if (bitimg != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitimg.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteimg = byteArrayOutputStream.toByteArray()
            val encodedimg = Base64.encodeToString(byteimg, Base64.DEFAULT)

            val service = getApiService().uploadImage(encodedimg)

            service.enqueue(object : Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Log.e("cek", "thidaa")
                    Toast.makeText(
                        applicationContext,
                        "Cannot instance Retrofit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(
                applicationContext,
                "File Not Found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}