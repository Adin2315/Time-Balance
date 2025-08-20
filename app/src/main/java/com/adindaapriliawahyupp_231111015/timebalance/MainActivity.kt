package com.adindaapriliawahyupp_231111015.timebalance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import com.adindaapriliawahyupp_231111015.timebalance.notification.NotificationHelper
import com.adindaapriliawahyupp_231111015.timebalance.notification.NotificationHelper.scheduleNotification
import com.adindaapriliawahyupp_231111015.timebalance.notification.NotificationsFragment
import com.adindaapriliawahyupp_231111015.timebalance.profile.ProfileFragment
import com.adindaapriliawahyupp_231111015.timebalance.schedule.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private lateinit var fabAdd: FloatingActionButton

    private lateinit var dbAdapter: DBAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        fabAdd = findViewById(R.id.fab_add)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        dbAdapter = DBAdapter(this)
        dbAdapter.open()

        bottomNavigation.setOnItemSelectedListener { item ->
            clearDrawerNavigationSelection()
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), R.id.nav_home)
                    true
                }
                R.id.nav_schedule -> {
                    loadFragment(ScheduleFragment(), R.id.nav_schedule)
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment(), R.id.nav_profile)
                    true
                }
                else -> false
            }
        }

        fabAdd.setOnClickListener { showAddScheduleDialog() }

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_home
            loadFragment(HomeFragment(), R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearBottomNavigationSelection()
        when (item.itemId) {
            R.id.nav_all_schedule -> loadFragment(AllSheduleFragment(), R.id.nav_all_schedule)
            R.id.nav_starred -> loadFragment(StarredFragment(), R.id.nav_starred)
            R.id.nav_notifications -> loadFragment(NotificationsFragment(), R.id.nav_notifications)
            R.id.nav_about -> loadFragment(AboutUsFragment(), R.id.nav_about)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment, fragmentId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        updateFabVisibility(fragmentId)
    }

    private fun updateFabVisibility(fragmentId: Int) {
        when (fragmentId) {
            R.id.nav_home,
            R.id.nav_schedule,-> fabAdd.show()
            else -> fabAdd.hide()
        }
    }

    private fun clearBottomNavigationSelection() {
        bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigation.menu.size()) {
            bottomNavigation.menu.getItem(i).isChecked = false
        }
        bottomNavigation.menu.setGroupCheckable(0, true, true)
    }

    private fun clearDrawerNavigationSelection() {
        navigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until navigationView.menu.size()) {
            navigationView.menu.getItem(i).isChecked = false
        }
        navigationView.menu.setGroupCheckable(0, true, true)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showAddScheduleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_title)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_description)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_date)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_time)
        val spinnerCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.spinner_category)
        val switchStarred = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_starred)
        val switchNotificationEnable = dialogView.findViewById<SwitchMaterial>(R.id.switch_notification)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

        val categories = dbAdapter.getAllCategories().map { it["name"].toString() }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(categoryAdapter)

        val calendar = Calendar.getInstance()

        etDate.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                etDate.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        etTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                etTime.setText(String.format("%02d:%02d", hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val categoryName = spinnerCategory.text.toString().trim()
            val isStarred = switchStarred.isChecked
            val isNotificationEnabled = switchNotificationEnable.isChecked

            if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty() || categoryName.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = dbAdapter.getCategoryIdByName(categoryName)
            if (categoryId == -1) {
                Toast.makeText(this, "Invalid category selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isSuccess = dbAdapter.insertSchedule(
                title = title,
                description = description,
                date = convertDateToDbFormat(date),
                time = time,
                categoryId = categoryId,
                isStarred = isStarred,
                isNotificationEnabled = isNotificationEnabled
            )

            if (isSuccess) {
                Toast.makeText(this, "Schedule added successfully", Toast.LENGTH_SHORT).show()
                refreshCurrentFragment()
                dialog.dismiss()
                if (isNotificationEnabled) {
                    scheduleNotification(title, description, date, time)
                }

            } else {
                Toast.makeText(this, "Failed to add schedule", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun scheduleNotification(title: String, description: String, date: String, time: String) {
        // You'll need to parse date and time into Calendar or Instant for accurate scheduling
        // This is a simplified example.
        // You would likely have a NotificationHelper class to handle actual AlarmManager setup.
        try {
            val calendar = Calendar.getInstance()
            // Parse date "dd/MM/yyyy"
            val dateParts = date.split("/")
            calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
            calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1) // Month is 0-indexed
            calendar.set(Calendar.YEAR, dateParts[2].toInt())

            // Parse time "HH:mm"
            val timeParts = time.split(":")
            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Ensure the time is in the future
            if (calendar.timeInMillis > System.currentTimeMillis()) {
                NotificationHelper.scheduleNotification(
                    this,
                    title,
                    description,
                    calendar.timeInMillis,
                    // Pass the schedule ID so you can cancel/update it later if needed
                    System.currentTimeMillis().toInt() // Unique ID for notification, use schedule ID from DB
                )
                Toast.makeText(this, "Notification scheduled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Schedule time is in the past. Notification not set.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error scheduling notification: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun convertDateToDbFormat(date: String): String {
        val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdfInput.parse(date)
        return sdfOutput.format(parsedDate!!)
    }

    private fun refreshCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (fragment) {
            is HomeFragment -> fragment.loadSchedulesFromDatabase()
            is ScheduleFragment -> fragment.loadSchedulesFromDatabase()
            is AllSheduleFragment -> fragment.loadSchedulesFromDatabase()
            is StarredFragment -> fragment.loadStarredSchedules()
        }
    }
}
