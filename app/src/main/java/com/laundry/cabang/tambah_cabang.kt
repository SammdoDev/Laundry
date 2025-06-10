package com.laundry.cabang

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_cabang

class tambah_cabang : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var tvJudul: TextView
    private lateinit var etCabang: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoTelepon: EditText
    private lateinit var btSimpan: MaterialButton

    private var isProcessing = false
    private var isEditMode = false
    private var isEditConfirmed = false // Flag untuk konfirmasi edit
    private var idCabang: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_cabang)

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
        tvJudul = findViewById(R.id.tvcabangjudul)
        etCabang = findViewById(R.id.etnamacabang)
        etAlamat = findViewById(R.id.etalamat)
        etNoTelepon = findViewById(R.id.etNoTelepon)
        btSimpan = findViewById(R.id.btcabangsimpan)
    }

    private fun checkEditMode() {
        // Cek apakah ada data yang dikirim untuk edit
        idCabang = intent.getStringExtra("ID_CABANG")
        val namaCabang = intent.getStringExtra("NAMA_CABANG")
        val alamatCabang = intent.getStringExtra("ALAMAT_CABANG")
        val NoHPCabang = intent.getStringExtra("NOMOR_TELEPON")

        if (idCabang != null && namaCabang != null && alamatCabang != null && NoHPCabang != null) {
            // Mode Edit
            isEditMode = true
            isEditConfirmed = false
            tvJudul.text = getString(R.string.judul_edit_cabang)
            btSimpan.text = getString(R.string.tombol_sunting)

            // Isi form dengan data yang akan diedit
            etCabang.setText(namaCabang)
            etAlamat.setText(alamatCabang)
            etNoTelepon.setText(NoHPCabang)

            // Disable form fields sampai dikonfirmasi
            setFormEnabled(false)
        } else {
            // Mode Tambah
            isEditMode = false
            tvJudul.text = getString(R.string.judul_tambah_cabang)
            btSimpan.text = getString(R.string.tombol_simpan)
            setFormEnabled(true)
        }
    }

    private fun confirmEdit() {
        // Aktifkan mode edit dan ubah tampilan
        isEditConfirmed = true
        btSimpan.text = getString(R.string.tombol_simpan)
        setFormEnabled(true)

        Toast.makeText(this, getString(R.string.msg_mode_sunting), Toast.LENGTH_LONG).show()
    }

    private fun setFormEnabled(enabled: Boolean) {
        etCabang.isEnabled = enabled
        etAlamat.isEnabled = enabled
        etNoTelepon.isEnabled = enabled

        // Visual feedback
        val alpha = if (enabled) 1.0f else 0.6f
        etCabang.alpha = alpha
        etAlamat.alpha = alpha
        etNoTelepon.alpha = alpha
    }

    private fun cekValidasi() {
        val cabang = etCabang.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noTelepon = etNoTelepon.text.toString().trim()

        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_nama_cabang)
            etCabang.requestFocus()
            resetButton()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.validasi_alamat)
            etAlamat.requestFocus()
            resetButton()
            return
        }
        if (noTelepon.isEmpty()) {
            etNoTelepon.error = getString(R.string.validasi_NoHP)
            etNoTelepon.requestFocus()
            resetButton()
            return
        }

        if (isEditMode && isEditConfirmed) {
            updateData(cabang, alamat, noTelepon)
        } else if (!isEditMode) {
            simpanData(cabang, alamat, noTelepon)
        }
    }

    private fun simpanData(cabang: String, alamat: String, noTelepon: String) {
        val cabangBaru = myRef.push()
        val cabangId = cabangBaru.key ?: ""

        val data = model_cabang(cabangId, cabang, alamat, noTelepon)

        cabangBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.tambah_cabang_sukses), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    getString(R.string.msg_gagal_simpan_data, error.message),
                    Toast.LENGTH_SHORT
                ).show()
                resetButton()
            }
    }

    private fun updateData(cabang: String, alamat: String, noTelepon: String) {
        idCabang?.let { id ->
            val data = model_cabang(id, cabang, alamat, noTelepon)

            myRef.child(id).setValue(data)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        getString(R.string.msg_cabang_disunting),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(
                        this,
                        getString(R.string.msg_gagal_sunting_data, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
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
            Toast.makeText(this, getString(R.string.msg_batal_perubahan), Toast.LENGTH_SHORT).show()
        }
        super.onBackPressed()
    }

}