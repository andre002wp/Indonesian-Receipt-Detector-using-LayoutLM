package id.andre002wp.ReceiptScanner.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.release.gfg1.DBHelper
import id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter.ReceiptAdapter
import id.andre002wp.ReceiptScanner.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val rv_receipt = binding.rvReceipt
        val tvwelcome = binding.tvWelcome
        val tvMonth = binding.tvMonth
        val tv_total = binding.tvTotal

        val dbrev = DBHelper(this.requireContext(), null)
        val date = "16/03/2023"
        var convertedDate: LocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val startDate = convertedDate.withDayOfMonth(1)
        val endDate = convertedDate.withDayOfMonth(convertedDate.getMonth().length(convertedDate.isLeapYear())).plusDays(1)


        val month_receipts = dbrev.getReceiptbyDate(startDate.toString(), endDate.toString())
        rv_receipt.adapter = ReceiptAdapter(month_receipts)
        rv_receipt.layoutManager = LinearLayoutManager(this.requireContext())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}