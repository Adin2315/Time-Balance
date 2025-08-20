package com.adindaapriliawahyupp_231111015.timebalance.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.adapter.ScheduleAdapter
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import com.adindaapriliawahyupp_231111015.timebalance.databinding.FragmentScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbAdapter: DBAdapter
    private lateinit var scheduleAdapter: ScheduleAdapter

    private val allSchedules = mutableListOf<Map<String, Any?>>()
    private val filteredSchedules = mutableListOf<Map<String, Any?>>()

    private var selectedDate = Calendar.getInstance()
    private var selectedCategoryId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()

        setupCalendar()
        setupCategorySpinner()
        setupRecyclerView()
        loadSchedulesFromDatabase()
    }

    private fun setupCalendar() {
        // Set initial date to today
        selectedDate = Calendar.getInstance()
        binding.calendarView.date = selectedDate.timeInMillis

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            updateSchedules()
        }
    }

    private fun setupCategorySpinner() {
        val categories = dbAdapter.getAllCategories()
        val categoryNames = mutableListOf(getString(R.string.all_categories))
        val categoryIds = mutableListOf(-1) // -1 represents "All Categories"

        categories.forEach { category ->
            category["name"]?.toString()?.let { name ->
                categoryNames.add(name)
                categoryIds.add(category["id"]?.toString()?.toIntOrNull() ?: -1)
            }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryId = if (position == 0) -1 else categoryIds[position]
                updateSchedules()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(requireContext(), filteredSchedules, dbAdapter).apply {
            setOnScheduleActionListener(object : ScheduleAdapter.OnScheduleActionListener {
                override fun onScheduleChanged() {
                    loadSchedulesFromDatabase()
                }

                override fun onScheduleUpdateFailed(error: String) {
                    TODO("Not yet implemented")
                }
            })
        }

        binding.rvScheduleItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
            setHasFixedSize(true)
        }
    }

    fun loadSchedulesFromDatabase() {
        allSchedules.clear()
        allSchedules.addAll(dbAdapter.getAllSchedules())
        updateSchedules()
    }

    private fun updateSchedules() {
        filteredSchedules.clear()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = sdf.format(selectedDate.time)

        // Filter by date
        val dateFiltered = allSchedules.filter {
            it["date"]?.toString() == selectedDateStr
        }

        // Filter by category if needed
        val categoryFiltered = if (selectedCategoryId != -1) {
            dateFiltered.filter {
                it["category_id"]?.toString()?.toIntOrNull() == selectedCategoryId
            }
        } else {
            dateFiltered
        }

        // Sort by time
        val sortedSchedules = categoryFiltered.sortedBy {
            it["time"]?.toString() ?: "23:59"
        }

        filteredSchedules.addAll(sortedSchedules)
        scheduleAdapter.notifyDataSetChanged()

        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredSchedules.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvScheduleItems.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvScheduleItems.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbAdapter.close()
        _binding = null
    }
}