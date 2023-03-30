package id.andre002wp.ReceiptScanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.websitebeaver.documentscanner.DocumentScanner
import com.websitebeaver.documentscanner.constants.ResponseType
import id.andre002wp.ReceiptScanner.Utils.Storage
import id.andre002wp.ReceiptScanner.databinding.ActivityMainBinding
import id.andre002wp.ReceiptScanner.ui.dashboard.DashboardFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: androidx.navigation.NavController
    var scanSheet: View? = null
    var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    var sheetState = false
    lateinit var dashboardFragment: DashboardFragment
    companion object {
        lateinit var result_bitmap: Bitmap
        private lateinit var mycontext: MainActivity
        fun isBitmapInitialized(): Boolean = ::result_bitmap.isInitialized
        var storage: Storage = Storage(this)
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val REQUEST_CODE_PERMISSIONS = 10
        var external_dir = ""
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        external_dir = ContextCompat.getExternalFilesDirs(this, null)[0].absolutePath + "/ReceiptScanner"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isBitmapInitialized()
        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)

        val included_modal = binding.includedModal
        scanSheet = included_modal.scanSheet
        bottomSheetBehavior = BottomSheetBehavior.from(scanSheet as LinearLayout)
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN

        val btnMedia = included_modal.mediacard
        val takePhoto = included_modal.cameracard

        btnMedia.setOnClickListener{
            dashboardFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)!!.childFragmentManager.fragments[0] as DashboardFragment
            bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            startGallery()
        }

        takePhoto.setOnClickListener {
            dashboardFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)!!.childFragmentManager.fragments[0] as DashboardFragment
            bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            launcherTakePhoto.launch(documentScanner.createDocumentScanIntent())
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_dashboard) {
                sheetState = true
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                sheetState = false
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherTakePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        documentScanner.handleDocumentScanIntentResult(result)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
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
            dashboardFragment.updatecropImage(bitmap as Bitmap)
        }
    }

    private val documentScanner = DocumentScanner(
        this,
        { croppedImageResults ->
            // display the first cropped image
            dashboardFragment.updatecropImage(BitmapFactory.decodeFile(croppedImageResults.first()) as Bitmap)
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


    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.navigation_dashboard){
            if (sheetState == true){
                sheetState = false
                bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_HIDDEN
            }
            else {
                navController.navigate(R.id.navigation_home)
            }
        }
        else if (navController.currentDestination?.id == R.id.navigation_notifications){
            navController.navigate(R.id.navigation_home)
        }
        else {
            finish()
        }
    }
}