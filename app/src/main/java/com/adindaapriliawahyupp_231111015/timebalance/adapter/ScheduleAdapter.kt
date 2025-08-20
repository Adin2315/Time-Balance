package com.adindaapriliawahyupp_231111015.timebalance.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ScheduleAdapter(
    private val context: Context,
    private var scheduleList: MutableList<Map<String, Any?>>,
    private val dbAdapter: DBAdapter
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private var listener: OnScheduleActionListener? = null

    interface OnScheduleActionListener {
        fun onScheduleChanged()
        fun onScheduleUpdateFailed(error: String)
    }

    fun setOnScheduleActionListener(listener: OnScheduleActionListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int = scheduleList.size

    fun updateScheduleList(newList: MutableList<Map<String, Any?>>) {
        this.scheduleList = newList
        notifyDataSetChanged()
    }

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.schedule_container)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_schedule_time)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_schedule_duration)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_schedule_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_schedule_description)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_schedule_icon)
        private val btnStar: ImageButton = itemView.findViewById(R.id.btn_star)

        fun bind(schedule: Map<String, Any?>) {
            try {
                val time = schedule["time"] as? String ?: ""
                val date = schedule["date"] as? String ?: ""
                val title = schedule["title"] as? String ?: ""
                val description = schedule["description"] as? String ?: ""
                val iconName = schedule["categoryIcon"] as? String ?: "ic_schedule"
                val isStarred = schedule["isStarred"] as? Boolean ?: false
                val status = schedule["status"] as? String ?: "pending"

                tvTime.text = time
                tvTitle.text = title
                tvDescription.text = description

                // Set status color on CardView
                when (status) {
                    "completed" -> {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.completed_bg))
                    }
                    "missed" -> {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.missed_bg))
                    }
                    else -> {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                    }
                }

                val iconRes = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                ivIcon.setImageResource(if (iconRes != 0) iconRes else R.drawable.ic_schedule)

                calculateAndDisplayDuration(date, time)
                updateStarIcon(isStarred)

                btnStar.setOnClickListener {
                    toggleStar(adapterPosition)
                }

                cardView.setOnLongClickListener {
                    showActionPopup(adapterPosition)
                    true
                }

                cardView.setOnClickListener {
                    showEditDialog(adapterPosition)
                }
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error binding schedule: ${e.message}")
            }
        }

        private fun calculateAndDisplayDuration(dateString: String, timeString: String) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val scheduleDateTimeString = "$dateString $timeString"
                val scheduleDateTime = dateFormat.parse(scheduleDateTimeString)
                val currentDateTime = Calendar.getInstance().time

                if (scheduleDateTime != null) {
                    val diffMillis = scheduleDateTime.time - currentDateTime.time
                    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)

                    val durationText = when {
                        diffMinutes < 0 -> {
                            val hoursPassed = abs(diffMinutes) / 60
                            val minutesPassed = abs(diffMinutes) % 60
                            if (hoursPassed > 0) {
                                "Terlewat ${hoursPassed} jam ${minutesPassed} menit"
                            } else {
                                "Terlewat ${minutesPassed} menit"
                            }
                        }
                        diffMinutes < 60 -> {
                            "Dimulai dalam ${diffMinutes} menit"
                        }
                        else -> {
                            val hours = diffMinutes / 60
                            val minutes = diffMinutes % 60
                            if (minutes > 0) {
                                "Dimulai dalam ${hours} jam ${minutes} menit"
                            } else {
                                "Dimulai dalam ${hours} jam"
                            }
                        }
                    }
                    tvDuration.text = durationText
                }
            } catch (e: Exception) {
                tvDuration.text = "Durasi: -"
                Log.e("ScheduleAdapter", "Error calculating duration", e)
            }
        }

        private fun updateStarIcon(isStarred: Boolean) {
            btnStar.setImageResource(if (isStarred) R.drawable.ic_starred else R.drawable.ic_star)
        }

        private fun toggleStar(position: Int) {
            if (position == RecyclerView.NO_POSITION) return

            try {
                val schedule = scheduleList[position]
                val id = schedule["id"] as? Int ?: return
                val newStar = !(schedule["isStarred"] as? Boolean ?: false)

                Thread {
                    try {
                        val success = dbAdapter.updateScheduleStar(id, newStar)

                        (context as? android.app.Activity)?.runOnUiThread {
                            if (success) {
                                scheduleList[position] = schedule.toMutableMap().apply {
                                    put("isStarred", newStar)
                                }
                                notifyItemChanged(position)
                                listener?.onScheduleChanged()
                            } else {
                                listener?.onScheduleUpdateFailed("Gagal mengubah status bintang")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ScheduleAdapter", "Error toggling star", e)
                        (context as? android.app.Activity)?.runOnUiThread {
                            listener?.onScheduleUpdateFailed("Error: ${e.message}")
                        }
                    }
                }.start()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error in toggleStar", e)
            }
        }

        private fun showActionPopup(position: Int) {
            if (position == RecyclerView.NO_POSITION) return

            val popup = PopupMenu(context, cardView)
            popup.menuInflater.inflate(R.menu.schedule_item_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        showEditDialog(position)
                        true
                    }
                    R.id.menu_delete -> {
                        showDeleteDialog(position)
                        true
                    }
                    else -> false
                }
            }

            try {
                popup.show()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error showing popup menu", e)
            }
        }

        private fun showEditDialog(position: Int) {
            if (position == RecyclerView.NO_POSITION) return

            val schedule = scheduleList[position]
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_schedule, null)

            try {
                builder.setView(dialogView)
                val dialog = builder.create()

                // Initialize views
                val etTitle = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_title)
                val etDescription = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_description)
                val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_date)
                val etTime = dialogView.findViewById<TextInputEditText>(R.id.et_schedule_time)
                val spinnerCategory = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinner_category)
                val switchStarred = dialogView.findViewById<SwitchMaterial>(R.id.switch_starred)
                val switchNotification = dialogView.findViewById<SwitchMaterial>(R.id.switch_notification)
                val btnSave = dialogView.findViewById<Button>(R.id.btn_save)
                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

                // Load categories
                val categories = dbAdapter.getAllCategories()
                val categoryNames = categories.map { it["name"].toString() }
                val categoryAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, categoryNames)
                spinnerCategory.setAdapter(categoryAdapter)

                // Set initial values
                etTitle.setText(schedule["title"]?.toString() ?: "")
                etDescription.setText(schedule["description"]?.toString() ?: "")
                etDate.setText(schedule["date"]?.toString() ?: "")
                etTime.setText(schedule["time"]?.toString() ?: "")
                switchStarred.isChecked = schedule["isStarred"] as? Boolean ?: false
                switchNotification.isChecked = schedule["isNotificationEnabled"] as? Boolean ?: false

                // Set selected category
                val selectedCategoryId = schedule["category_id"]?.toString()?.toIntOrNull()
                categories.firstOrNull { it["id"].toString().toIntOrNull() == selectedCategoryId }?.let {
                    spinnerCategory.setText(it["name"].toString(), false)
                }

                // Date picker
                etDate.setOnClickListener {
                    showDatePicker(etDate)
                }

                // Time picker
                etTime.setOnClickListener {
                    showTimePicker(etTime)
                }

                // Cancel button
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                // Save button
                btnSave.setOnClickListener {
                    try {
                        val title = etTitle.text.toString().trim()
                        val description = etDescription.text.toString().trim()
                        val date = etDate.text.toString().trim()
                        val time = etTime.text.toString().trim()
                        val categoryName = spinnerCategory.text.toString().trim()

                        // Validate inputs
                        if (title.isEmpty()) {
                            etTitle.error = "Judul tidak boleh kosong"
                            return@setOnClickListener
                        }
                        if (date.isEmpty()) {
                            etDate.error = "Tanggal tidak boleh kosong"
                            return@setOnClickListener
                        }
                        if (time.isEmpty()) {
                            etTime.error = "Waktu tidak boleh kosong"
                            return@setOnClickListener
                        }
                        if (categoryName.isEmpty()) {
                            Toast.makeText(context, "Pilih kategori", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val selectedCategory = categories.firstOrNull { it["name"] == categoryName }
                        val categoryId = selectedCategory?.get("id")?.toString()?.toIntOrNull() ?: -1

                        if (categoryId == -1) {
                            Toast.makeText(context, "Kategori tidak valid", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // Update in background thread
                        Thread {
                            try {
                                val success = dbAdapter.updateScheduleData(
                                    schedule["id"]?.toString()?.toIntOrNull() ?: -1,
                                    title,
                                    description,
                                    date,
                                    time,
                                    categoryId,
                                    switchStarred.isChecked,
                                    switchNotification.isChecked
                                )

                                (context as? android.app.Activity)?.runOnUiThread {
                                    if (success) {
                                        // Update local data
                                        scheduleList[position] = schedule.toMutableMap().apply {
                                            put("title", title)
                                            put("description", description)
                                            put("date", date)
                                            put("time", time)
                                            put("category_id", categoryId)
                                            put("isStarred", switchStarred.isChecked)
                                            put("isNotificationEnabled", switchNotification.isChecked)
                                            put("categoryName", categoryName)
                                        }

                                        notifyItemChanged(position)
                                        listener?.onScheduleChanged()
                                        dialog.dismiss()
                                        Toast.makeText(context, "Jadwal berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    } else {
                                        listener?.onScheduleUpdateFailed("Gagal menyimpan perubahan")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ScheduleAdapter", "Error updating schedule", e)
                                (context as? android.app.Activity)?.runOnUiThread {
                                    listener?.onScheduleUpdateFailed("Error: ${e.message}")
                                }
                            }
                        }.start()

                    } catch (e: Exception) {
                        Log.e("ScheduleAdapter", "Error in save click", e)
                        Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error showing edit dialog", e)
                Toast.makeText(context, "Gagal memuat dialog edit", Toast.LENGTH_SHORT).show()
            }
        }

        private fun showDeleteDialog(position: Int) {
            if (position == RecyclerView.NO_POSITION) return

            val schedule = scheduleList[position]
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_schedule, null)

            try {
                builder.setView(dialogView)
                val dialog = builder.create()

                val tvTitle = dialogView.findViewById<TextView>(R.id.tv_delete_schedule_title)
                val tvDateTime = dialogView.findViewById<TextView>(R.id.tv_delete_schedule_time)
                val tvCategory = dialogView.findViewById<TextView>(R.id.tv_delete_schedule_location)
                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_delete)
                val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm_delete)

                tvTitle.text = schedule["title"]?.toString() ?: "Tidak ada judul"
                tvDateTime.text = "${schedule["date"]} ${schedule["time"]}"
                tvCategory.text = schedule["categoryName"]?.toString() ?: "Tidak ada kategori"

                btnCancel.setOnClickListener { dialog.dismiss() }

                btnConfirm.setOnClickListener {
                    try {
                        val id = schedule["id"]?.toString()?.toIntOrNull() ?: -1
                        if (id == -1) {
                            Toast.makeText(context, "ID jadwal tidak valid", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        Thread {
                            try {
                                val success = dbAdapter.deleteSchedule(id)

                                (context as? android.app.Activity)?.runOnUiThread {
                                    if (success) {
                                        scheduleList.removeAt(position)
                                        notifyItemRemoved(position)
                                        listener?.onScheduleChanged()
                                        Toast.makeText(context, "Jadwal berhasil dihapus", Toast.LENGTH_SHORT).show()
                                    } else {
                                        listener?.onScheduleUpdateFailed("Gagal menghapus jadwal")
                                    }
                                    dialog.dismiss()
                                }
                            } catch (e: Exception) {
                                Log.e("ScheduleAdapter", "Error deleting schedule", e)
                                (context as? android.app.Activity)?.runOnUiThread {
                                    listener?.onScheduleUpdateFailed("Error: ${e.message}")
                                }
                            }
                        }.start()
                    } catch (e: Exception) {
                        Log.e("ScheduleAdapter", "Error in delete click", e)
                        Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.show()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error showing delete dialog", e)
                Toast.makeText(context, "Gagal memuat dialog hapus", Toast.LENGTH_SHORT).show()
            }
        }

        private fun showDatePicker(etDate: TextInputEditText) {
            try {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error showing date picker", e)
            }
        }

        private fun showTimePicker(etTime: TextInputEditText) {
            try {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            } catch (e: Exception) {
                Log.e("ScheduleAdapter", "Error showing time picker", e)
            }
        }
    }
}