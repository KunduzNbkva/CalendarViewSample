package com.example.calendarviewsample

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.calendarviewsample.databinding.CalendarSheetBinding
import com.example.calendarviewsample.databinding.CalendarDayLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@SuppressLint("UseCompatLoadingForDrawables")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CalendarSheet(themeI: Int) : BottomSheetDialogFragment() {
    private var binding: CalendarSheetBinding? = null
    private var selectedDate: LocalDate? = null
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val today = LocalDate.now()
    private var bottomSheetTheme = themeI


    override fun getTheme(): Int = bottomSheetTheme

    companion object {
        const val TAG = "CalendarBottomSheet"
    }


    private val startBackground: GradientDrawable by lazy {
        requireContext().getDrawable(R.drawable.continuous_selected_bg_start) as GradientDrawable
    }

    private val endBackground: GradientDrawable by lazy {
        requireContext().getDrawable(R.drawable.continuous_selected_bg_end) as GradientDrawable
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CalendarSheetBinding.inflate(inflater, container, false)
        return binding!!.root}


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        calendarSetup()
        setDaysBinder()
        setMonthBinder()
        getCustomerData()
    }


    private fun calendarSetup() {
        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val daysOfWeek = DayOfWeek.values()
        binding!!.calendarView.setup(firstMonth, lastMonth, daysOfWeek.first())
        binding!!.calendarView.scrollToMonth(currentMonth)

        // We set the radius of the continuous selection background drawable dynamically
        // since the view size is `match parent` hence we cannot determine the appropriate
        // radius value which would equal half of the view's size beforehand.
        binding!!.calendarView.post {
            val radius = ((binding!!.calendarView.width / 7) / 7).toFloat()
            startBackground.setCornerRadius(topLeft = radius, bottomLeft = radius,topRight = radius, bottomRight = radius)
            endBackground.setCornerRadius(topRight = radius, bottomRight = radius,topLeft = radius, bottomLeft = radius)
        }
    }



    private  fun clickDay(day: CalendarDay){
        val date = day.date
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == today || day.date.isAfter(today))) {
                        if (startDate != null) {
                            if (date < startDate || endDate != null) {
                                startDate = date
                                endDate = null
                            } else if (date != startDate) {
                                endDate = date
                            }
                        } else {
                            startDate = date
                        }
                        binding!!.calendarView.notifyCalendarChanged()
                    } else if (day.owner != DayOwner.THIS_MONTH) {
                        val scrollMonth = date.yearMonth
                        binding!!.calendarView.scrollToMonth(scrollMonth)
                    }
    }

    private  fun clickDay(date: LocalDate){
        startDate = date
        binding!!.calendarView.notifyCalendarChanged()
    }
    private  fun clickDay(startDate: LocalDate,endDate: LocalDate){
            this.startDate = endDate
            this.endDate = startDate
            binding!!.calendarView.notifyCalendarChanged()
    }


    private fun setDaysBinder() {
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

            // Will be set when this container is bound
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    clickDay(day)
                }
            }
        }

        binding!!.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            @RequiresApi(Build.VERSION_CODES.N)
            @SuppressLint("ResourceAsColor")
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                val roundBgView = CalendarDayLayoutBinding.bind(container.view).roundBgView

                textView.text = null
                textView.background = null
                roundBgView.makeInVisible()

                val startDate = startDate
                val endDate = endDate

                container.textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    container.textView.setTextColor(Color.WHITE)
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }

                when (day.owner) {
                    DayOwner.THIS_MONTH -> {
                        textView.text = day.day.toString()
                        when {
                            startDate == day.date && endDate == null -> {
                                roundBgView.makeVisible()
                                roundBgView.setBackgroundResource(R.drawable.single_day_background)
                                textView.setTextColor(Color.BLACK)
                            }
                            day.date == startDate -> {
                                textView.background = startBackground
                                textView.setTextColor(Color.BLACK)
                            }
                            startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
                                textView.setTextColor(Color.BLACK)
                                val dayOfWeek = day.date.dayOfWeek
                                when(dayOfWeek){
                                    DayOfWeek.MONDAY ->
                                        textView.setBackgroundResource(R.drawable.start_selected_bg)
                                    DayOfWeek.SUNDAY ->
                                        textView.setBackgroundResource(R.drawable.end_selected_bg)
                                    else ->
                                        textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                                }
                            }
                            day.date == endDate -> {
                                textView.setTextColor(Color.BLACK)
                                textView.background = endBackground
                            }
                            day.date == today -> {
                                textView.setTextColor(Color.BLACK)
                                roundBgView.makeVisible()
                                roundBgView.setBackgroundResource(R.drawable.example_4_today_bg)
                            }

                            else -> textView.setTextColorRes(R.color.example_4_grey)
                        }
                    }
                    // Make the coloured selection background continuous on the invisible in and out dates across various months.
                    DayOwner.PREVIOUS_MONTH ->
                        if (startDate != null && endDate != null && isInDateBetween(
                                day.date,
                                startDate,
                                endDate
                            )
                        ) {
                            textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle)
                        }
                    DayOwner.NEXT_MONTH ->
                        if (startDate != null && endDate != null && isOutDateBetween(day.date, startDate, endDate)) {
                            textView.setBackgroundResource(R.drawable.continuous_selected_bg_middle) }
                }
            }
        }
    }

//    private fun setEndStartBack(){
//        val range = startDate..endDate
//        for(i in iterator<CalendarDay> {startDate..}
//    }


    private fun isInDateBetween(inDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (inDate.yearMonth == startDate.yearMonth) return true
        val firstDateInThisMonth = inDate.plusMonths(1).yearMonth.atDay(1)
        return firstDateInThisMonth >= startDate && firstDateInThisMonth <= endDate && startDate != firstDateInThisMonth
    }

    private fun isOutDateBetween(outDate: LocalDate, startDate: LocalDate, endDate: LocalDate): Boolean {
        if (startDate.yearMonth == endDate.yearMonth) return false
        if (outDate.yearMonth == endDate.yearMonth) return true
        val lastDateInThisMonth = outDate.minusMonths(1).yearMonth.atEndOfMonth()
        return lastDateInThisMonth >= startDate && lastDateInThisMonth <= endDate && endDate != lastDateInThisMonth
    }



    private fun setMonthBinder() {
        class MonthViewContainer(view: View) : ViewContainer(view)
        binding!!.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    binding!!.calendarView.monthScrollListener = { month ->
                        val title =
                            "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
                        binding!!.monthYearText.text = title

                        selectedDate?.let {
                            // Clear selection if we scroll to a new month.
                            selectedDate = null
                            binding!!.calendarView.notifyDateChanged(it)
                        }
                    }

                    binding!!.nextMonthImage.setOnClickListener {
                        binding!!.calendarView.findFirstVisibleMonth()?.let {
                            binding!!.calendarView.smoothScrollToMonth(it.yearMonth.next)
                        }
                    }

                    binding!!.previousMonthImage.setOnClickListener {
                        binding!!.calendarView.findFirstVisibleMonth()?.let {
                            binding!!.calendarView.smoothScrollToMonth(it.yearMonth.previous)
                        }
                    }
                }
            }
    }

    private fun getCustomerData(){
        val clickListener = View.OnClickListener {
            when (it.id){
                R.id.day -> {
                    clickDay(today)
                }
                R.id.seven_days ->{
                    val date = today.minusDays(7)
                    clickDay(today,date)
                }
                R.id.month -> {
                    val date = today.minusDays(30)
                    clickDay(today,date)
                }
                R.id.btn_select -> {
                    Log.e("Click","Clicked   $startDate - $endDate" )
                }
            }
        }
        binding!!.day.setOnClickListener(clickListener)
        binding!!.sevenDays.setOnClickListener(clickListener)
        binding!!.month.setOnClickListener(clickListener)
        binding!!.btnSelect.setOnClickListener(clickListener)
    }
}


