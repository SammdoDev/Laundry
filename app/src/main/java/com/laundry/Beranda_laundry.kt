package com.laundry

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale

class Beranda_laundry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.beranda_laundry)

        val headline: TextView = findViewById(R.id.headline)

        val calender = Calendar.getInstance()
        val hour = calender.get(Calendar.HOUR_OF_DAY)

        val greeting = when{
            hour in 0..11 -> "Selamat Pagi, Samuel"
            hour in 12..17 -> "Selamat Siang, Samuel"
            else -> "Selamat Malam, Samuel"
        }

        headline.text = greeting

        val timeTextView: TextView = findViewById(R.id.time)

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = sdf.format(calender.time)

        timeTextView.text = currentDate

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}