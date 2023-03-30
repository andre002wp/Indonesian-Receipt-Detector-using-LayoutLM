package id.andre002wp.ReceiptScanner.ui.history

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.release.gfg1.DBHelper
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter.ReceiptAdapter
import id.andre002wp.ReceiptScanner.databinding.FragmentHistoryBinding
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment(), ReceiptAdapter.EditReceiptListener {

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var receipts : ArrayList<Receipt>
    lateinit var startDateHolder : TextView
    lateinit var endDateHolder : TextView
    private lateinit var products_holder : RecyclerView
    private lateinit var dbrev: DBHelper
    private lateinit var editResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dbrev = DBHelper(this.requireContext(), null)
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var dateLayout: ConstraintLayout = binding.root.findViewById(R.id.dateLayout)

        startDateHolder = binding.root.findViewById(R.id.startDate)
        startDateHolder.text = ""
        endDateHolder = binding.root.findViewById(R.id.endDate)
        endDateHolder.text = ""

        var textTotal : TextView = binding.root.findViewById(R.id.textTotal)

        // value for calendar
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val janThisYear = calendar.timeInMillis

        calendar.timeInMillis = today
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val decThisYear = calendar.timeInMillis

        // Build constraints.
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(janThisYear)
                .setEnd(decThisYear)

        products_holder = binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_history)

        checkDateSetting()

        Log.d("DB", "receipts size: ${receipts.size}")

        editResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                checkDateSetting()
                updateAdapter(products_holder, receipts)
            }
        }

        // create product adapter and set data to adapter
        products_holder.adapter = ReceiptAdapter(receipts, this)
        // set adapter to recycler view
        products_holder.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)

        dateLayout.setOnClickListener {

            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(androidx.core.util.Pair(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds())
                    )
                    .build()
            dateRangePicker.addOnPositiveButtonClickListener {
                // Respond to positive button click.
                val dateformat = SimpleDateFormat("yyyy-MM-dd")

                val startDate = dateformat.format(Date(it.first)).toString()
                val endDate = dateformat.format(Date(it.second)).toString()
                startDateHolder.text = startDate
                endDateHolder.text = endDate
                Log.d("DB", "startDate: $startDate")
                Log.d("DB", "endDate: $endDate")
                getReceiptsByDate(startDate, endDate, products_holder, textTotal)
            }

            dateRangePicker.show(parentFragmentManager, "StartDate")
            Log.d("HistoryFragment", "Date already selected")
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        if (startDateHolder.text.toString().equals("")){
            // get data from database
            receipts = dbrev.getallReceipts()
        }
        else{
            receipts = dbrev.getReceiptbyDate(startDateHolder.text.toString(), endDateHolder.text.toString())
        }
    }

    fun getReceiptsByDate(startDate: String, endDate: String,products_holder: RecyclerView, total_text:TextView): MutableList<Receipt> {
        val dbrev = DBHelper(this.requireContext(), null)
        val receipts = dbrev.getReceiptbyDate(startDate, endDate)
        updateTotal(total_text, receipts)
        updateAdapter(products_holder, receipts)
        return receipts
    }

    fun checkDateSetting(){
        val dbrev = DBHelper(this.requireContext(), null)
        if (startDateHolder.text.toString().equals("")){
            // get data from database
            receipts = dbrev.getallReceipts()
        }
        else{
            receipts = dbrev.getReceiptbyDate(startDateHolder.text.toString(), endDateHolder.text.toString())
        }
    }

    fun updateTotal(textTotal: TextView, receipts: MutableList<Receipt>){
        var total = 0
        for(receipt in receipts){
            total += receipt.total
        }
        textTotal.text = "Total Belanja : Rp. ${thousandSeparator(total)}"
    }

    fun updateAdapter(products_holder: androidx.recyclerview.widget.RecyclerView, receipts: MutableList<Receipt>){
        products_holder.adapter = ReceiptAdapter(receipts as ArrayList<Receipt>, this)
        products_holder.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)
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

    private fun thousandSeparator(monthspending: Int): String {
        return monthspending.toString().reversed().chunked(3).joinToString(".").reversed()
    }
}
