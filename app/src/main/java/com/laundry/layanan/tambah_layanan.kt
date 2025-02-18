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
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_layanan)

        initViews()

        btSimpan.setOnClickListener {
            cekValidasi()
        }

        // Atur padding berdasarkan system bars
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
        etCabang = findViewById(R.id.etlayanancabang)
        btSimpan = findViewById(R.id.btlayanansimpan)
    }

    private fun cekValidasi() {
        val layanan = etlayanan.text.toString().trim()
        val harga = etHarga.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        if (layanan.isEmpty()) {
            etlayanan.error = getString(R.string.validasi_jenis_layanan)
            etlayanan.requestFocus()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = getString(R.string.validasi_harga)
            etHarga.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_layanan)
            etCabang.requestFocus()
            return
        }

        simpanData(layanan, harga, cabang)
    }

    private fun simpanData(layanan: String, harga: String, cabang: String) {
        val layananBaru = myRef.push()
        val layananId = layananBaru.key ?: ""

        val data = model_layanan(
            layananId,
            layanan,
            harga,
            cabang
        )

        layananBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.tambah_layanan_sukses),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal menyimpan data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
