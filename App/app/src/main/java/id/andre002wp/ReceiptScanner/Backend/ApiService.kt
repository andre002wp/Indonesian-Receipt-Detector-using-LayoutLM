package id.andre002wp.ReceiptScanner.Backend

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.release.gfg1.Product
import kotlinx.parcelize.Parcelize
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

//    @Multipart
    @POST("detect")
    fun uploadImage(
        @Body base64img: String
    ): Call<FileUploadResponse>
}

class FileUploadResponse(
    @field:SerializedName("data")
    @Expose
    val data: ReceiptScanResponse,

    @field:SerializedName("image")
    @Expose
    val image: String,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

@Parcelize
data class ReceiptScanResponse(
    @SerializedName("store_name")
    @Expose
    val store_name: String,

    @SerializedName("date")
    @Expose
    val date: String,

    @SerializedName("time")
    @Expose
    val time: String,

    @SerializedName("total")
    @Expose
    val total: Int,

    @SerializedName("products")
    @Expose
    val products: MutableList<Product>,

    ) : Parcelable