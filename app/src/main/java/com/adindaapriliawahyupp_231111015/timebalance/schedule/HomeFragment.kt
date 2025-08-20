package com.adindaapriliawahyupp_231111015.timebalance.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.adapter.DateAdapter
import com.adindaapriliawahyupp_231111015.timebalance.adapter.ScheduleAdapter
import com.adindaapriliawahyupp_231111015.timebalance.data.DateItem
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var dbAdapter: DBAdapter

    private lateinit var dateRecyclerView: RecyclerView
    private lateinit var todayScheduleRecyclerView: RecyclerView
    private lateinit var starredScheduleRecyclerView: RecyclerView

    private lateinit var dateAdapter: DateAdapter
    private lateinit var todayScheduleAdapter: ScheduleAdapter
    private lateinit var starredScheduleAdapter: ScheduleAdapter

    private lateinit var todayScheduleTitle: TextView
    private lateinit var viewAllStarredButton: Button

    private val dateList = ArrayList<DateItem>()
    private val allSchedules = mutableListOf<Map<String, Any?>>()
    private val todaySchedules = ArrayList<Map<String, Any?>>()
    private val starredSchedules = ArrayList<Map<String, Any?>>()

    private var selectedDate = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()

        initViews(view)
        setupRecyclerViews()
        generateDateList()
        loadSchedulesFromDatabase()
    }

    private fun initViews(view: View) {
        dateRecyclerView = view.findViewById(R.id.rv_dates)
        todayScheduleRecyclerView = view.findViewById(R.id.rv_today_schedules)
        starredScheduleRecyclerView = view.findViewById(R.id.rv_starred_schedules)
        todayScheduleTitle = view.findViewById(R.id.tv_today_schedule_title)
        viewAllStarredButton = view.findViewById(R.id.btn_view_all_starred)
    }

    private fun setupRecyclerViews() {
        dateRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dateAdapter = DateAdapter(dateList) { selectedDateItem -> onDateSelected(selectedDateItem) }
        dateRecyclerView.adapter = dateAdapter

        todayScheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        todayScheduleAdapter = ScheduleAdapter(requireContext(), todaySchedules, dbAdapter).apply {
            setOnScheduleActionListener(object : ScheduleAdapter.OnScheduleActionListener {
                override fun onScheduleChanged() {
                    loadSchedulesFromDatabase()
                }

                override fun onScheduleUpdateFailed(error: String) {
                    TODO("Not yet implemented")
                }
            })
        }
        todayScheduleRecyclerView.adapter = todayScheduleAdapter

        starredScheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        starredScheduleAdapter = ScheduleAdapter(requireContext(), starredSchedules, dbAdapter).apply {
            setOnScheduleActionListener(object : ScheduleAdapter.OnScheduleActionListener {
                override fun onScheduleChanged() {
                    loadSchedulesFromDatabase()
                }

                override fun onScheduleUpdateFailed(error: String) {
                    TODO("Not yet implemented")
                }
            })
        }
        starredScheduleRecyclerView.adapter = starredScheduleAdapter

        viewAllStarredButton.setOnClickListener { navigateToStarredPage() }
    }

    private fun generateDateList() {
        dateList.clear()
        val calendar = Calendar.getInstance()
        for (i in 0 until 7) {
            dateList.add(DateItem(date = calendar.time, isSelected = i == 0))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        dateAdapter.notifyDataSetChanged()
    }

    fun loadSchedulesFromDatabase() {
        allSchedules.clear()
        allSchedules.addAll(dbAdapter.getAllSchedules())

        updateTodaySchedules()
        updateStarredSchedules()
    }

    private fun onDateSelected(dateItem: DateItem) {
        dateList.forEach { it.isSelected = false }
        dateItem.isSelected = true
        dateAdapter.notifyDataSetChanged()

        selectedDate.time = dateItem.date
        updateTodaySchedules()
    }

    private fun updateTodaySchedules() {
        todaySchedules.clear()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = sdf.format(selectedDate.time)

        val filtered = allSchedules.filter { it["date"] == selectedDateStr }
        todaySchedules.addAll(filtered)

        val titleSdf = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        todayScheduleTitle.text = "Schedule ${titleSdf.format(selectedDate.time)}"

        todayScheduleAdapter.notifyDataSetChanged()
    }

    private fun updateStarredSchedules() {
        starredSchedules.clear()
        val starred = allSchedules.filter { (it["isStarred"] as? Boolean) == true }.take(5)
        starredSchedules.addAll(starred)

        val totalStarred = allSchedules.count { (it["isStarred"] as? Boolean) == true }
        viewAllStarredButton.visibility = if (totalStarred > 5) View.VISIBLE else View.GONE
        viewAllStarredButton.text = "View All Starred ($totalStarred)"

        starredScheduleAdapter.notifyDataSetChanged()
    }

    private fun navigateToStarredPage() {
        Toast.makeText(requireContext(), "Navigate to Starred Schedules", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbAdapter.close()
    }
}