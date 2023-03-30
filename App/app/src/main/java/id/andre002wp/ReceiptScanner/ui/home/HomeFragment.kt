package id.andre002wp.ReceiptScanner.ui.home

import android.app.Activity
import android.content.Intent
import android.icu.number.NumberFormatter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.release.gfg1.DBHelper
import com.release.gfg1.Receipt
import com.release.gfg1.User
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter.ReceiptAdapter
import id.andre002wp.ReceiptScanner.databinding.FragmentHomeBinding
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment(), ReceiptAdapter.EditReceiptListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var user1: User? = null
    var name_edit = true
    private var tvuser: EditText? = null
    private lateinit var editResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var convertedDate: LocalDate
    private lateinit var month_receipts: ArrayList<Receipt>
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
        tvuser = binding.tvuser
        val tvMonth = binding.tvMonth
        val tv_total = binding.tvTotal
        val btnchangeName = binding.changeName

        val dbrev = DBHelper(this.requireContext(), null)
        updateuserdb()

        editResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getMonthReceipts(dbrev)
                rv_receipt.adapter = ReceiptAdapter(month_receipts,this)
                rv_receipt.layoutManager = LinearLayoutManager(this.requireContext())
            }
        }

        month_receipts = getMonthReceipts(dbrev)
        tvMonth.text = "History transaksi bulan ${indonesianMonth(convertedDate.month.toString())} ${convertedDate.year}"
        var monthspending = 0
        for (receipt in month_receipts){
            monthspending += receipt.getTotalPayment()
        }
        tv_total.text = "Rp. ${thousandSeparator(monthspending)}"
        rv_receipt.adapter = ReceiptAdapter(month_receipts,this)
        rv_receipt.layoutManager = LinearLayoutManager(this.requireContext())

        btnchangeName.setOnClickListener {
            if (name_edit){
                name_edit = false
                tvuser!!.focusable = View.FOCUSABLE
                tvuser!!.isFocusableInTouchMode = true
                tvuser!!.requestFocus()

            } else {
                name_edit = true
                tvuser!!.focusable = View.NOT_FOCUSABLE
                tvuser!!.isFocusableInTouchMode = false
                    if (!tvuser!!.text.toString().equals("")){
                    val new_user = tvuser!!.text.toString()
                    dbrev.updateUser(1, new_user)
                    updateuserdb()
                }
            }
        }

        return root
    }

    private fun thousandSeparator(monthspending: Int): String {
        return monthspending.toString().reversed().chunked(3).joinToString(".").reversed()
    }

    private fun indonesianMonth(toString: String): String {
        if(toString.lowercase().equals("january")){
            return "Januari"
        }
        if(toString.lowercase().equals("february")){
            return "Februari"
        }
        if(toString.lowercase().equals("march")){
            return "Maret"
        }
        if(toString.lowercase().equals("april")){
            return "April"
        }
        if(toString.lowercase().equals("may")){
            return "Mei"
        }
        if(toString.lowercase().equals("june")){
            return "Juni"
        }
        if(toString.lowercase().equals("july")){
            return "Juli"
        }
        if(toString.lowercase().equals("august")){
            return "Agustus"
        }
        if(toString.lowercase().equals("september")){
            return "September"
        }
        if(toString.lowercase().equals("october")){
            return "Oktober"
        }
        if(toString.lowercase().equals("november")){
            return "November"
        }
        if(toString.lowercase().equals("december")){
            return "Desember"
        }
        return toString
    }

    private fun getMonthReceipts(dbrev:DBHelper): ArrayList<Receipt> {
//        val date = "19-03-2023"
//        var convertedDate: LocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        convertedDate = LocalDate.now()
        val startDate = convertedDate.withDayOfMonth(1)
        val endDate = convertedDate.withDayOfMonth(convertedDate.getMonth().length(convertedDate.isLeapYear())).plusDays(1)


        month_receipts = dbrev.getReceiptbyDate(startDate.toString(), endDate.toString())
        return month_receipts
    }

    fun updateuserdb() {
        val dbrev = DBHelper(this.requireContext(), null)
        //get username
        var getuser = dbrev.getUser(1)
        if (getuser == null) {
            val new_user = User(1, "User")
            dbrev.addUser(new_user)
            getuser = dbrev.getUser(1)
        }
        tvuser!!.setText(getuser?.getName() ?: "User")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditReceipt(item: Receipt) {
        var goToHistoryDetail = Intent(context, Scan_Preview::class.java)
        goToHistoryDetail.putExtra("editflag",true)
        goToHistoryDetail.putExtra("id", item.getID())
        goToHistoryDetail.putExtra("store_name", item.getStoreName())
        goToHistoryDetail.putExtra("date", item.getPurchaseDate())
        goToHistoryDetail.putExtra("time", item.getPurchaseTime())
        goToHistoryDetail.putExtra("total", item.getTotalPayment())
        goToHistoryDetail.putExtra("products", item.products)
        editResultLauncher.launch(goToHistoryDetail)
    }
}