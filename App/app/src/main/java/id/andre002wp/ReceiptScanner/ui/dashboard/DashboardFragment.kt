package id.andre002wp.ReceiptScanner.ui.dashboard
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.websitebeaver.documentscanner.DocumentScanner
import com.websitebeaver.documentscanner.constants.ResponseType
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val media: Button = binding.btnMedia
        val takephoto: Button = binding.btnPicture

        media.setOnClickListener{
            val intent = Intent(context, scan_activity::class.java)
            intent.putExtra("isPhoto",0)
            startActivity(intent)
        }

        takephoto.setOnClickListener {
            val intent = Intent(context, scan_activity::class.java)
            intent.putExtra("isPhoto",1)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}