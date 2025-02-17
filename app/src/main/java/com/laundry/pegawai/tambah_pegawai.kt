package com.laundry.pegawai

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
import com.laundry.model_data.model_pegawai

class tambah_pegawai : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var tvJudul: TextView
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var etTerdaftar: EditText
    private lateinit var etCabang: EditText
    private lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_pegawai)

        initViews()

        btSimpan.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        tvJudul = findViewById(R.id.tvpegawaijudul)
        etNama = findViewById(R.id.etpegawainama)
        etAlamat = findViewById(R.id.etpegawailamat)
        etNoHP = findViewById(R.id.etpegawainohp)
        etCabang = findViewById(R.id.etpegawaicabang)
        etTerdaftar = findViewById(R.id.etterdaftar)
        btSimpan = findViewById(R.id.btpegawaisimpan)
    }

    private fun cekValidasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noHp = etNoHP.text.toString().trim()
        val terdaftar = etTerdaftar.text.toString().trim() // ✅ Perbaiki di sini
        val cabang = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = getString(R.string.validasi_nama_pelanggan)
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = getString(R.string.validasi_alamat_pelanggan)
            etAlamat.requestFocus()
            return
        }
        if (noHp.isEmpty()) {
            etNoHP.error = getString(R.string.validasi_nohp_pelanggan)
            etNoHP.requestFocus()
            return
        }
        if (terdaftar.isEmpty()) {
            etTerdaftar.error = getString(R.string.validasi_isi_terdaftar)
            etTerdaftar.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = getString(R.string.validasi_cabang_pelanggan)
            etCabang.requestFocus()
            return
        }

        simpanData(nama, alamat, noHp, terdaftar, cabang)
    }

    private fun simpanData(nama: String, alamat: String, noHp: String, terdaftar: String, cabang: String) { // ✅ Tambahkan terdaftar
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key ?: ""

        val data = model_pegawai(
            pegawaiId,
            nama,
            alamat,
            noHp,
            terdaftar,
            cabang
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.tambah_pegawai_sukses),
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
