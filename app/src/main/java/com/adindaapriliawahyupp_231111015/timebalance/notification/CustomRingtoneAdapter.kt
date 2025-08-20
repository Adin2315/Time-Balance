package com.adindaapriliawahyupp_231111015.timebalance.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adindaapriliawahyupp_231111015.timebalance.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomRingtoneAdapter(
    private val ringtones: MutableList<CustomRingtone>,
    private val onPlayClick: (CustomRingtone) -> Unit,
    private val onDeleteClick: (CustomRingtone) -> Unit,
    private val onSelectClick: (CustomRingtone) -> Unit
) : RecyclerView.Adapter<CustomRingtoneAdapter.RingtoneViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_custom_ringtone, parent, false)
        return RingtoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        holder.bind(ringtones[position])
    }

    override fun getItemCount(): Int = ringtones.size

    inner class RingtoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_ringtone_name)
        private val textDate: TextView = itemView.findViewById(R.id.text_ringtone_date)
        private val buttonPlay: ImageButton = itemView.findViewById(R.id.button_play)
        private val buttonSelect: ImageButton = itemView.findViewById(R.id.button_select)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.button_delete)

        fun bind(ringtone: CustomRingtone) {
            textName.text = ringtone.name
            textDate.text = dateFormat.format(Date(ringtone.dateAdded))

            buttonPlay.setOnClickListener {
                onPlayClick(ringtone)
            }

            buttonSelect.setOnClickListener {
                onSelectClick(ringtone)
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(ringtone)
            }
        }
    }
}