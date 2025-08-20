package com.adindaapriliawahyupp_231111015.timebalance.schedule

import android.os.Bundle
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
import com.adindaapriliawahyupp_231111015.timebalance.databinding.FragmentAllSheduleBinding

class AllSheduleFragment : Fragment(), ScheduleAdapter.OnScheduleActionListener {

    private var _binding: FragmentAllSheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbAdapter: DBAdapter
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentStatusFilter = "All" // Default status filter
    private var currentStarredFilter = "All" // Default starred filter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllSheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()

        setupRecyclerView()
        setupFilterSpinners()
        loadAllSchedules()
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(requireContext(), mutableListOf(), dbAdapter).apply {
            setOnScheduleActionListener(this@AllSheduleFragment)
        }

        binding.recyclerAllSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterSpinners() {
        // Status filter setup
        val statusOptions = arrayOf("All", "Pending", "Completed", "Missed")
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statusOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerStatusFilter.adapter = statusAdapter
        binding.spinnerStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                loadAllSchedules()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Starred filter setup
        val starredOptions = arrayOf("All", "Starred", "Not Starred")
        val starredAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            starredOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerStarredFilter.adapter = starredAdapter
        binding.spinnerStarredFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStarredFilter = starredOptions[position]
                loadAllSchedules()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadAllSchedules() {
        binding.progressLoading.visibility = View.VISIBLE

        // Get all schedules from database
        val allSchedules = dbAdapter.getAllSchedules()

        // Apply status filter
        val statusFilteredSchedules = when (currentStatusFilter) {
            "Pending" -> allSchedules.filter { it["status"] == "pending" }
            "Completed" -> allSchedules.filter { it["status"] == "completed" }
            "Missed" -> allSchedules.filter { it["status"] == "missed" }
            else -> allSchedules // "All"
        }

        // Apply starred filter
        val filteredSchedules = when (currentStarredFilter) {
            "Starred" -> statusFilteredSchedules.filter {
                when (val isStarred = it["isStarred"]) {
                    is Boolean -> isStarred
                    is Int -> isStarred == 1
                    else -> false
                }
            }
            "Not Starred" -> statusFilteredSchedules.filter {
                when (val isStarred = it["isStarred"]) {
                    is Boolean -> !isStarred
                    is Int -> isStarred != 1
                    else -> true
                }
            }
            else -> statusFilteredSchedules // "All"
        }

        // Update UI
        if (filteredSchedules.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerAllSchedules.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerAllSchedules.visibility = View.VISIBLE
            scheduleAdapter.updateScheduleList(filteredSchedules.toMutableList())
        }

        binding.progressLoading.visibility = View.GONE
    }

    override fun onScheduleChanged() {
        loadAllSchedules()
    }

    override fun onScheduleUpdateFailed(error: String) {
        TODO("Not yet implemented")
    }

    fun loadSchedulesFromDatabase() {
        loadAllSchedules()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbAdapter.close()
        _binding = null
    }
}