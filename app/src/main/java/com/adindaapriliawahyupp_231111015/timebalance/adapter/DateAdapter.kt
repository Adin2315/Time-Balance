package com.adindaapriliawahyupp_231111015.timebalance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.data.DateItem
import java.text.SimpleDateFormat
import java.util.Locale

class DateAdapter(
    private val dates: List<DateItem>,
    private val onDateClick: (DateItem) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.tv_day)
        val dateText: TextView = view.findViewById(R.id.tv_date)
        val container: View = view.findViewById(R.id.date_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]

        // Format hari dan tanggal
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())

        holder.dayText.text = dayFormat.format(dateItem.date).uppercase(Locale.getDefault())
        holder.dateText.text = dateFormat.format(dateItem.date)

        // Ubah tampilan sesuai apakah tanggal terpilih atau tidak
        val context = holder.itemView.context
        if (dateItem.isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_date_selected)
            holder.dayText.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.dateText.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_date_normal)
            holder.dayText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            holder.dateText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        }

        // Klik tanggal
        holder.container.setOnClickListener {
            onDateClick(dateItem)
        }
    }

    override fun getItemCount(): Int = dates.size
}