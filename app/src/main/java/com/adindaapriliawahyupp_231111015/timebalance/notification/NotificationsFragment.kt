package com.adindaapriliawahyupp_231111015.timebalance.notification

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter // Your DBAdapter

class NotificationsFragment : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var dbAdapter: DBAdapter
    private var currentNotificationSettings: NotificationSettings = NotificationSettings()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open() // Open the database connection

        // Initialize views
        switchNotifications = view.findViewById(R.id.switch_notifications)

        loadNotificationSettings()
        setupListeners()
    }

    private fun loadNotificationSettings() {
        currentNotificationSettings = dbAdapter.getNotificationSettings()
        switchNotifications.isChecked = currentNotificationSettings.isEnabled
    }

    private fun setupListeners() {
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            currentNotificationSettings = currentNotificationSettings.copy(isEnabled = isChecked)
            saveNotificationSettings()
            NotificationHelper.updateSettings(requireContext(), currentNotificationSettings)
        }
    }

    private fun saveNotificationSettings() {
        dbAdapter.updateNotificationSettings(currentNotificationSettings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbAdapter.close() // Close the database connection
    }
}