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
import com.laundry.pelanggan.data_pelanggan
import com.laundry.pegawai.data_pegawai

class Beranda_laundry : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Menambahkan efek Edge-to-Edge (optional)
        setContentView(R.layout.beranda_laundry) // Memuat layout activity beranda_laundry

        // Inisialisasi TextView untuk headline
        val headline = findViewById<TextView>(R.id.headline)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Menentukan salam berdasarkan waktu
        val greeting = when (hour) {
            in 0..11 -> "Selamat Pagi, Samuel"
            in 12..17 -> "Selamat Siang, Samuel"
            else -> "Selamat Malam, Samuel"
        }
        headline.text = greeting

        val timeTextView = findViewById<TextView>(R.id.time)
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        val currentDate = sdf.format(calendar.time)
        timeTextView.text = currentDate

        val pelanggan: CardView = findViewById(R.id.pelanggan)
        pelanggan.setOnClickListener {
            val intent = Intent(this, data_pelanggan::class.java)
            startActivity(intent)
        }

        val pegawai: CardView = findViewById(R.id.pegawai)
        pegawai.setOnClickListener {
            val intent = Intent(this, data_pegawai::class.java)
            startActivity(intent)
        }

        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
