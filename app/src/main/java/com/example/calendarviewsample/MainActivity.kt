package com.example.calendarviewsample

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.calendarviewsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.showBottomSheet.setOnClickListener {
                CalendarSheet(R.style.AppBottomSheetDialogTheme).apply {
                    show(supportFragmentManager,CalendarSheet.TAG)
                }
            }
        }
    }
