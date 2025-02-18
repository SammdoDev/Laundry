package com.laundry

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.laundry.layanan.data_layanan
import com.laundry.pegawai.data_pegawai
import com.laundry.pelanggan.data_pelanggan

class Beranda_laundry : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Optional: for edge-to-edge layout
        setContentView(R.layout.beranda_laundry) // Ensure layout has correct views

        // Initialize TextView for greeting
        val headline = findViewById<TextView>(R.id.headline)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Determine greeting based on the time of day
        val greeting = when (hour) {
            in 0..11 -> "Selamat Pagi, Samuel"
            in 12..17 -> "Selamat Siang, Samuel"
            else -> "Selamat Malam, Samuel"
        }
        headline.text = greeting

        // Display current date
        val timeTextView = findViewById<TextView>(R.id.time)
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        val currentDate = sdf.format(calendar.time)
        timeTextView.text = currentDate

        // Handle click event to navigate to 'data_pelanggan' activity
        val pelanggan: CardView = findViewById(R.id.pelanggan)
        pelanggan.setOnClickListener {
            val intent = Intent(this, data_pelanggan::class.java)
            startActivity(intent)
        }

        // Handle click event to navigate to 'data_pegawai' activity
        val pegawai: CardView = findViewById(R.id.pegawai)
        pegawai.setOnClickListener {
            val intent = Intent(this, data_pegawai::class.java)
            startActivity(intent)
        }

        val layanan: CardView = findViewById(R.id.layanan)
        layanan.setOnClickListener {
            val intent = Intent(this, data_layanan::class.java)
            startActivity(intent)
        }

        // Adjust layout for system insets (status bar, navigation bar)
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Ensure the insets are applied correctly
        }
    }
}
