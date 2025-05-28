package com.laundry

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.laundry.layanan.data_layanan
import com.laundry.pegawai.data_pegawai
import com.laundry.pelanggan.data_pelanggan
import com.laundry.transaksi.data_transaksi
import com.laundry.tambahan.data_tambahan
import androidx.appcompat.app.AppCompatDelegate

class Beranda_laundry : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.beranda_laundry)

        // Set default night mode to follow system
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Apply theme colors based on current mode
        applyThemeColors()

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

        // Handle click events
        setupClickListeners()

        // Adjust layout for system insets
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun applyThemeColors() {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        val headline = findViewById<TextView>(R.id.headline)
        val timeTextView = findViewById<TextView>(R.id.time)
        val pelangganCard = findViewById<CardView>(R.id.pelanggan)
        val pegawaiCard = findViewById<CardView>(R.id.pegawai)
        val layananCard = findViewById<CardView>(R.id.layanan)
        val transaksiCard = findViewById<CardView>(R.id.transaksi)
        val tambahanCard = findViewById<CardView>(R.id.tambahan)

        mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background))
        headline.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        timeTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

        val cards = listOf(pelangganCard, pegawaiCard, layananCard, transaksiCard, tambahanCard)
        val cardColors = listOf(
            R.color.card_1,
            R.color.card_2,
            R.color.card_3,
            R.color.card_4,
            R.color.card_5
        )

        cards.forEachIndexed { index, card ->
            card.setCardBackgroundColor(ContextCompat.getColor(this, cardColors[index]))
            card.cardElevation = 6f
        }
    }


    private fun applyAccentColors(pelanggan: CardView, pegawai: CardView, layanan: CardView, transaksi: CardView, tambahan: CardView, isDarkMode: Boolean) {
        val cards = arrayOf(pelanggan, pegawai, layanan, transaksi, tambahan)
        val accentColors = if (isDarkMode) {
            arrayOf(
                "#FF6B6B", // Red
                "#4ECDC4", // Teal
                "#45B7D1", // Blue
                "#96CEB4", // Green
                "#FFEAA7"  // Yellow
            )
        } else {
            arrayOf(
                "#E74C3C", // Red
                "#1ABC9C", // Teal
                "#3498DB", // Blue
                "#2ECC71", // Green
                "#F39C12"  // Orange
            )
        }

        cards.forEachIndexed { index, card ->
            if (index < accentColors.size) {
                // Add a subtle accent color border or ripple effect
                card.setCardBackgroundColor(Color.parseColor(accentColors[index]))
                card.alpha = if (isDarkMode) 0.8f else 0.9f
            }
        }
    }

    private fun setupClickListeners() {
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

        val layanan: CardView = findViewById(R.id.layanan)
        layanan.setOnClickListener {
            val intent = Intent(this, data_layanan::class.java)
            startActivity(intent)
        }

        val transaksi: CardView = findViewById(R.id.transaksi)
        transaksi.setOnClickListener {
            val intent = Intent(this, data_transaksi::class.java)
            startActivity(intent)
        }

        val tambahan: CardView = findViewById(R.id.tambahan)
        tambahan.setOnClickListener {
            val intent = Intent(this, data_tambahan::class.java)
            startActivity(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Reapply theme colors when configuration changes (like switching between dark/light mode)
        applyThemeColors()
    }
}