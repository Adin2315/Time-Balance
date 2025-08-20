package com.adindaapriliawahyupp_231111015.timebalance.data

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EventDecorator(
    private val color: Int,
    private val dates: Collection<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(color))
        view.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}