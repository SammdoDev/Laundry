package com.laundry.tambahan

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
import com.laundry.model_data.model_tambahan

class tambah_tambahan : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("tambahan")

    private lateinit var tvJudul: TextView
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var btSimpan: Button

    private var isProcessing = false
    private var isEditMode = false
    private var isEditConfirmed = false // Flag untuk konfirmasi edit
    private var idTambahan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_tambahan)

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
        tvJudul = findViewById(R.id.tvtambahanjudul)
        etNama = findViewById(R.id.ettambahannama)
        etHarga = findViewById(R.id.ettambahanharga)
        btSimpan = findViewById(R.id.bttambahansimpan)
    }

    private fun checkEditMode() {
        // Cek apakah ada data yang dikirim untuk edit
        idTambahan = intent.getStringExtra("ID_TAMBAHAN")
        val namaTambahan = intent.getStringExtra("NAMA_TAMBAHAN")
        val hargaTambahan = intent.getStringExtra("HARGA_TAMBAHAN")

        if (idTambahan != null && namaTambahan != null && hargaTambahan != null) {
            // Mode Edit
            isEditMode = true
            isEditConfirmed = false
            tvJudul.text = "Sunting Tambahan"
            btSimpan.text = "Sunting"

            // Isi form dengan data yang akan diedit
            etNama.setText(namaTambahan)
            etHarga.setText(hargaTambahan)

            // Disable form fields sampai dikonfirmasi
            setFormEnabled(false)
        } else {
            // Mode Tambah
            isEditMode = false
            tvJudul.text = "Tambah Tambahan"
            btSimpan.text = "Simpan"
            setFormEnabled(true)
        }
    }

    private fun confirmEdit() {
        // Aktifkan mode edit dan ubah tampilan
        isEditConfirmed = true
        btSimpan.text = "Simpan"
        setFormEnabled(true)

        Toast.makeText(this, getString(R.string.msg_mode_sunting), Toast.LENGTH_LONG).show()
    }

    private fun setFormEnabled(enabled: Boolean) {
        etNama.isEnabled = enabled
        etHarga.isEnabled = enabled

        // Visual feedback
        val alpha = if (enabled) 1.0f else 0.6f
        etNama.alpha = alpha
        etHarga.alpha = alpha
    }

    private fun cekValidasi() {
        val nama = etNama.text.toString().trim()
        val harga = etHarga.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_tambahan)
            etNama.requestFocus()
            resetButton()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = getString(R.string.validasi_harga_tambahan)
            etHarga.requestFocus()
            resetButton()
            return
        }

        if (isEditMode && isEditConfirmed) {
            updateData(nama, harga)
        } else if (!isEditMode) {
            simpanData(nama, harga)
        }
    }

    private fun simpanData(nama: String, harga: String) {
        val tambahanBaru = myRef.push()
        val tambahanId = tambahanBaru.key ?: ""

        val data = model_tambahan(tambahanId, nama, harga)

        tambahanBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.tambah_tambahan_sukses), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, getString(R.string.msg_gagal_simpan_data, error.message), Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    private fun updateData(nama: String, harga: String) {
        idTambahan?.let { id ->
            val data = model_tambahan(id, nama, harga)

            myRef.child(id).setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.msg_tambahan_disunting), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, getString(R.string.msg_gagal_sunting_data, error.message), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.msg_batal_perubahan), Toast.LENGTH_SHORT).show()
        }
        super.onBackPressed()
    }
}