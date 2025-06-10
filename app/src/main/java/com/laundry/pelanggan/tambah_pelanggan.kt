package com.laundry.pelanggan

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
import com.laundry.model_data.model_pelanggan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class tambah_pelanggan : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")

    lateinit var tvjuduladdpelanggan: TextView
    lateinit var etNameaddpelanggan: EditText
    lateinit var etAlamataddpelanggan: EditText
    lateinit var etNoHpaddpelanggan: EditText
    lateinit var buttonaddpelanggan: MaterialButton

    var idPelanggan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_pelanggan)

        init()
        getData()
        buttonaddpelanggan.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvjuduladdpelanggan = findViewById(R.id.tvpelangganjudul)
        etNameaddpelanggan = findViewById(R.id.etpelanggannama)
        etNoHpaddpelanggan = findViewById(R.id.etpelanggannohp)
        etAlamataddpelanggan = findViewById(R.id.etpelangganalamat)
        buttonaddpelanggan = findViewById(R.id.btpelanggansimpan)
    }

    fun getData() {
        idPelanggan = intent.getStringExtra("idPelanggan") ?: ""
        val judul = intent.getStringExtra("judul") ?: getString(R.string.tambah_pelanggan)
        val nama = intent.getStringExtra("namaPelanggan") ?: ""
        val alamat = intent.getStringExtra("alamatPelanggan") ?: ""
        val nohp = intent.getStringExtra("noHPPelanggan") ?: ""
        val fromDialog = intent.getBooleanExtra("fromDialog", false)

        tvjuduladdpelanggan.text = judul
        etNameaddpelanggan.setText(nama)
        etAlamataddpelanggan.setText(alamat)
        etNoHpaddpelanggan.setText(nohp)

        if (!tvjuduladdpelanggan.text.equals(getString(R.string.tambah_pelanggan))) {
            if (judul == "Edit Pelanggan") {
                if (fromDialog) {
                    hidup()
                    etNameaddpelanggan.requestFocus()
                    buttonaddpelanggan.text = getString(R.string.tombol_simpan)
                } else {
                    mati()
                    buttonaddpelanggan.text = getString(R.string.tombol_sunting)
                }
            }
        } else {
            hidup()
            etNameaddpelanggan.requestFocus()
            buttonaddpelanggan.text = getString(R.string.tombol_simpan)
        }
    }

    fun mati() {
        etNameaddpelanggan.isEnabled = false
        etAlamataddpelanggan.isEnabled = false
        etNoHpaddpelanggan.isEnabled = false

        etNameaddpelanggan.alpha = 0.6f
        etAlamataddpelanggan.alpha = 0.6f
        etNoHpaddpelanggan.alpha = 0.6f
    }

    fun hidup() {
        etNameaddpelanggan.isEnabled = true
        etAlamataddpelanggan.isEnabled = true
        etNoHpaddpelanggan.isEnabled = true

        etNameaddpelanggan.alpha = 1f
        etAlamataddpelanggan.alpha = 1f
        etNoHpaddpelanggan.alpha = 1f
    }


    fun update() {
        val pelangganRef = database.getReference("pelanggan").child(idPelanggan)
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val data = model_pelanggan(
            idPelanggan,
            etNameaddpelanggan.text.toString(),
            etAlamataddpelanggan.text.toString(),
            etNoHpaddpelanggan.text.toString(),
            currentTime
        )

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPelanggan"] = data.namaPelanggan ?: ""
        updateData["alamatPelanggan"] = data.alamatPelanggan ?: ""
        updateData["noHPPelanggan"] = data.noHPPelanggan ?: ""

        pelangganRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.Data_pelanggan_Berhasil_Diperbarui), Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.Data_pelanggan_Berhasil_Diperbarui), Toast.LENGTH_SHORT).show()
        }
    }

    fun simpan() {
        val pelangganBaru = myRef.push()
        val idPelanggan = pelangganBaru.key
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val data = model_pelanggan(
            idPelanggan.toString(),
            etNameaddpelanggan.text.toString(),
            etAlamataddpelanggan.text.toString(),
            etNoHpaddpelanggan.text.toString(),
            currentTime
        )

        pelangganBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.tambah_pelanggan_sukses), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.tambah_pelanggan_gagal), Toast.LENGTH_SHORT).show()
            }
    }

    fun cekValidasi() {
        val nama = etNameaddpelanggan.text.toString()
        val alamat = etAlamataddpelanggan.text.toString()
        val noHP = etNoHpaddpelanggan.text.toString()

        if (nama.isEmpty()) {
            etNameaddpelanggan.error = getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(this, getString(R.string.validasi_nama_pelanggan), Toast.LENGTH_SHORT).show()
            etNameaddpelanggan.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamataddpelanggan.error = getString(R.string.validasi_alamat_pelanggan)
            Toast.makeText(this, getString(R.string.validasi_alamat_pelanggan), Toast.LENGTH_SHORT).show()
            etAlamataddpelanggan.requestFocus()
            return
        }
        if (noHP.isEmpty()) {
            etNoHpaddpelanggan.error = getString(R.string.validasi_noHP_pelanggan)
            Toast.makeText(this, getString(R.string.validasi_noHP_pelanggan), Toast.LENGTH_SHORT).show()
            etNoHpaddpelanggan.requestFocus()
            return
        }

        if (buttonaddpelanggan.text == getString(R.string.tombol_simpan)) {
            if (idPelanggan.isEmpty()) {
                simpan()
            } else {
                update()
            }
        } else if (buttonaddpelanggan.text == getString(R.string.tombol_sunting)) {
            hidup()
            etNameaddpelanggan.requestFocus()
            buttonaddpelanggan.text = getString(R.string.tombol_perbarui)
        } else if (buttonaddpelanggan.text == getString(R.string.tombol_perbarui)) {
            update()
        }
    }
}
