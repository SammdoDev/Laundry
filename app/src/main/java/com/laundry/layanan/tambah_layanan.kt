package com.laundry.layanan

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_layanan

class tambah_layanan : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    private lateinit var tvJudul: TextView
    private lateinit var etlayanan: EditText
    private lateinit var etHarga: EditText
    private lateinit var btSimpan: Button

    private var isProcessing = false
    private var isEditMode = false
    private var isEditConfirmed = false // Flag untuk konfirmasi edit
    private var idLayanan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_layanan)

        initViews()
        checkEditMode()

        btSimpan.setOnClickListener {
            if (!isProcessing) {
                if (isEditMode && !isEditConfirmed) {
                    // Mode edit pertama kali - konfirmasi dulu
                    confirmEdit()
                } else {
                    // Mode tambah atau edit sudah dikonfirmasi
                    isProcessing = true
                    btSimpan.isEnabled = false
                    cekValidasi()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tvJudul = findViewById(R.id.tvlayananjudul)
        etlayanan = findViewById(R.id.etnamalayanan)
        etHarga = findViewById(R.id.etharga)
        btSimpan = findViewById(R.id.btlayanansimpan)
    }

    private fun checkEditMode() {
        // Cek apakah ada data yang dikirim untuk edit
        idLayanan = intent.getStringExtra("ID_LAYANAN")
        val namaLayanan = intent.getStringExtra("NAMA_LAYANAN")
        val hargaLayanan = intent.getStringExtra("HARGA_LAYANAN")

        if (idLayanan != null && namaLayanan != null && hargaLayanan != null) {
            // Mode Edit
            isEditMode = true
            isEditConfirmed = false
            tvJudul.text = "Sunting Layanan"
            btSimpan.text = "Sunting"

            // Isi form dengan data yang akan diedit
            etlayanan.setText(namaLayanan)
            etHarga.setText(hargaLayanan)

            // Disable form fields sampai dikonfirmasi
            setFormEnabled(false)
        } else {
            // Mode Tambah
            isEditMode = false
            tvJudul.text = "Tambah Layanan"
            btSimpan.text = "Simpan"
            setFormEnabled(true)
        }
    }

    private fun confirmEdit() {
        // Aktifkan mode edit dan ubah tampilan
        isEditConfirmed = true
        btSimpan.text = "Simpan"
        setFormEnabled(true)

        Toast.makeText(this, "Mode sunting diaktifkan. Silakan edit data dan tekan Simpan.", Toast.LENGTH_LONG).show()
    }

    private fun setFormEnabled(enabled: Boolean) {
        etlayanan.isEnabled = enabled
        etHarga.isEnabled = enabled

        // Visual feedback
        val alpha = if (enabled) 1.0f else 0.6f
        etlayanan.alpha = alpha
        etHarga.alpha = alpha
    }

    private fun cekValidasi() {
        val layanan = etlayanan.text.toString().trim()
        val harga = etHarga.text.toString().trim()

        if (layanan.isEmpty()) {
            etlayanan.error = getString(R.string.validasi_jenis_layanan)
            etlayanan.requestFocus()
            resetButton()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = getString(R.string.validasi_harga)
            etHarga.requestFocus()
            resetButton()
            return
        }

        if (isEditMode && isEditConfirmed) {
            updateData(layanan, harga)
        } else if (!isEditMode) {
            simpanData(layanan, harga)
        }
    }

    private fun simpanData(layanan: String, harga: String) {
        val layananBaru = myRef.push()
        val layananId = layananBaru.key ?: ""

        val data = model_layanan(layananId, layanan, harga)

        layananBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.tambah_layanan_sukses), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan data: ${error.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    private fun updateData(layanan: String, harga: String) {
        idLayanan?.let { id ->
            val data = model_layanan(id, layanan, harga)

            myRef.child(id).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Layanan berhasil disunting", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Gagal menyunting data: ${error.message}", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
        }
    }

    private fun resetButton() {
        isProcessing = false
        btSimpan.isEnabled = true
    }

    override fun onBackPressed() {
        if (isEditMode && isEditConfirmed) {
            // Jika user menekan back saat sudah dalam mode edit, tanyakan konfirmasi
            Toast.makeText(this, "Perubahan akan dibatalkan", Toast.LENGTH_SHORT).show()
        }
        super.onBackPressed()
    }
}