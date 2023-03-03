package id.andre002wp.ReceiptScanner.ui.history

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.release.gfg1.DBHelper
import com.release.gfg1.Product
import com.release.gfg1.Receipt
import id.andre002wp.ReceiptScanner.R
import id.andre002wp.ReceiptScanner.Utils.ProductAdapter
import id.andre002wp.ReceiptScanner.Utils.ReceiptAdapter.ReceiptAdapter
import id.andre002wp.ReceiptScanner.databinding.FragmentHistoryBinding
import id.andre002wp.ReceiptScanner.ui.dashboard.Scan_Preview
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var dateLayout: ConstraintLayout = binding.root.findViewById(R.id.dateLayout)

        var startDateHolder : TextView = binding.root.findViewById(R.id.startDate)
        startDateHolder.text = ""
        var endDateHolder : TextView = binding.root.findViewById(R.id.endDate)
        endDateHolder.text = ""


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

        var products_holder = binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_history)

        // get data from database
        val dbrev = DBHelper(this.requireContext(), null)
        val receipts = dbrev.getallReceipts()
        Log.d("DB", "receipts size: ${receipts.size}")
        // create product adapter and set data to adapter
        products_holder.adapter = ReceiptAdapter(receipts)
        // set adapter to recycler view
        products_holder.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)

        dateLayout.setOnClickListener {

            if(startDateHolder.text.equals("") && endDateHolder.text.equals("") ){
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
                    Log.d("HistoryFragment", dateRangePicker.headerText.split(" – ")[1])
                    startDateHolder.text = dateRangePicker.headerText.split(" – ")[0]
                    endDateHolder.text = dateRangePicker.headerText.split(" – ")[1]
                }

                dateRangePicker.show(parentFragmentManager, "StartDate")
                Log.d("HistoryFragment", "Date already selected")
            }
            else{
                Log.d("HistoryFragment", "test")
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
