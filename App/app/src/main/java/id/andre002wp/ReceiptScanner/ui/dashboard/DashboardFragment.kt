package id.andre002wp.ReceiptScanner.ui.dashboard
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.release.gfg1.Product
import id.andre002wp.ReceiptScanner.Backend.ApiConfig
import id.andre002wp.ReceiptScanner.Backend.FileUploadResponse
import id.andre002wp.ReceiptScanner.MainActivity
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.databinding.FragmentDashboardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var croppedImageView: ImageView
    private lateinit var btn_scan: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        croppedImageView = binding.scanImage
        btn_scan = binding.scanBtn
        val mainActivity = activity as MainActivity

        btn_scan.setOnClickListener {
            if (croppedImageView.drawable == null){
                mainActivity.bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                mainActivity.sheetState = true
            }
            else{
                uploadImage(croppedImageView.drawable.toBitmap())
            }

        }



        return root
    }

    fun updatecropImage(bitmap: Bitmap) {
        croppedImageView.setImageBitmap(bitmap)
    }



    private fun uploadImage(bitimg: Bitmap) {
        if (bitimg != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitimg.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val byteimg = byteArrayOutputStream.toByteArray()
            val encodedimg = Base64.encodeToString(byteimg, Base64.DEFAULT)

            val service = ApiConfig.getApiService().uploadImage(encodedimg)

            service.enqueue(object : Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Success",
                            Toast.LENGTH_SHORT
                        ).show()
                        val scan_data = response.body()?.data
//                        Log.d("API", scan_data!!?.store_name)
//                        Log.d("API", scan_data!!?.date)
//                        Log.d("API", scan_data!!?.time)
//                        Log.d("API", scan_data!!?.total.toString())
                        val products = ArrayList<Product>()
                        for (i in scan_data!!?.products){
                            val new_product = Product(i.name, i.price, i.quantity)
                            products.add(new_product)
//                            Log.d("API", i.name)
//                            Log.d("API", i.price.toString())
//                            Log.d("API", i.quantity.toString())
                        }
                        Log.d("API", response.body()?.image!!)
//                        var b64decoded = Base64.decode(response.body()?.image, Base64.DEFAULT)
//                        result_bitmap = BitmapFactory.decodeByteArray(b64decoded, 0, b64decoded.size)
                        Intent(context, Scan_Preview::class.java).also {
                            it.putExtra("editflag", false)
                            it.putExtra("store_name", scan_data!!?.store_name)
                            it.putExtra("date", scan_data!!?.date)
                            it.putExtra("time", scan_data!!?.time)
                            it.putExtra("total", scan_data!!?.total)
                            it.putExtra("products", products)
                            it.putExtra("image", response.body()?.image!!)
                            startActivity(it)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            response.message().toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Log.e("cek", "thidaa")
                    Toast.makeText(
                        context,
                        "Cannot instance Retrofit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(
                context,
                "File Not Found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}