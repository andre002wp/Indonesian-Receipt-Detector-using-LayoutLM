package id.andre002wp.ReceiptScanner.Backend

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
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
    val data: MutableList<ListRekomendasiItem>,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

@Parcelize
data class ListRekomendasiItem(
    @SerializedName("foto")
    @Expose
    val foto: String,

) : Parcelable