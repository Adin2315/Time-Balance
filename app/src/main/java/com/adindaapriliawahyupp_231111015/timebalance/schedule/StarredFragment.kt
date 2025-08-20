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
import com.adindaapriliawahyupp_231111015.timebalance.databinding.FragmentStarredBinding

class StarredFragment : Fragment(), ScheduleAdapter.OnScheduleActionListener {

    private var _binding: FragmentStarredBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbAdapter: DBAdapter
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentFilter = "All" // Default filter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()

        setupRecyclerView()
        setupFilterSpinner()
        loadStarredSchedules()
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(requireContext(), mutableListOf(), dbAdapter).apply {
            setOnScheduleActionListener(this@StarredFragment)
        }

        binding.recyclerStarredSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf("All", "Pending", "Completed", "Missed")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerFilter.adapter = adapter
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentFilter = filterOptions[position]
                loadStarredSchedules()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun loadStarredSchedules() {
        binding.progressLoading.visibility = View.VISIBLE

        // Get all starred schedules from database
        val allSchedules = dbAdapter.getAllSchedules().filter {
            val isStarred = it["isStarred"]
            when (isStarred) {
                is Boolean -> isStarred
                is Int -> isStarred == 1
                else -> false
            }
        }.toMutableList()

        // Apply filter
        val filteredSchedules = when (currentFilter) {
            "Pending" -> allSchedules.filter { it["status"] == "pending" }
            "Completed" -> allSchedules.filter { it["status"] == "completed" }
            "Missed" -> allSchedules.filter { it["status"] == "missed" }
            else -> allSchedules // "All"
        }.toMutableList()

        // Update UI
        if (filteredSchedules.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerStarredSchedules.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerStarredSchedules.visibility = View.VISIBLE
            scheduleAdapter.updateScheduleList(filteredSchedules)
        }

        binding.progressLoading.visibility = View.GONE
    }

    override fun onScheduleChanged() {
        loadStarredSchedules()
    }

    override fun onScheduleUpdateFailed(error: String) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbAdapter.close()
        _binding = null
    }
}