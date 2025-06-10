package com.laundry.pegawai

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_pegawai
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class tambah_pegawai : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pegawai")

    lateinit var tvjuduladdpegawai: TextView
    lateinit var etNameaddpegawai: EditText
    lateinit var etAlamataddpegawai: EditText
    lateinit var etNoHpaddpegawai: EditText
    lateinit var etCabangaddpegawai: EditText
    lateinit var buttonaddpegawai: MaterialButton

    var idPegawai: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_pegawai)

        init()
        getData()
        buttonaddpegawai.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvjuduladdpegawai = findViewById(R.id.tvpegawaijudul)
        etNameaddpegawai = findViewById(R.id.etpegawainama)
        etNoHpaddpegawai = findViewById(R.id.etpegawainohp)
        etAlamataddpegawai = findViewById(R.id.etpegawaialamat)
        etCabangaddpegawai = findViewById(R.id.etpegawaicabang)
        buttonaddpegawai = findViewById(R.id.btpegawaisimpan)
    }

    fun getData() {
        idPegawai = intent.getStringExtra("idPegawai") ?: ""
        val judul = intent.getStringExtra("judul") ?: getString(R.string.tambah_pegawai)
        val nama = intent.getStringExtra("namaPegawai") ?: ""
        val alamat = intent.getStringExtra("alamatPegawai") ?: ""
        val nohp = intent.getStringExtra("noHPPegawai") ?: ""
        val cabang = intent.getStringExtra("idCabang") ?: ""
        val fromDialog = intent.getBooleanExtra("fromDialog", false)

        tvjuduladdpegawai.text = judul
        etNameaddpegawai.setText(nama)
        etAlamataddpegawai.setText(alamat)
        etNoHpaddpegawai.setText(nohp)
        etCabangaddpegawai.setText(cabang)

        if (!tvjuduladdpegawai.text.equals(getString(R.string.tambah_pegawai))) {
            if (judul.equals("Edit Pegawai")) {
                if (fromDialog) {
                    // If coming from dialog mode, directly enable editing
                    hidup()
                    etNameaddpegawai.requestFocus()
                    buttonaddpegawai.text = getString(R.string.tombol_simpan)
                } else {
                    // Normal edit mode from card press
                    mati()
                    buttonaddpegawai.text = getString(R.string.tombol_sunting)
                }
            }
        } else {
            hidup()
            etNameaddpegawai.requestFocus()
            buttonaddpegawai.text = getString(R.string.tombol_simpan)
        }
    }

    fun mati() {
        etNameaddpegawai.isEnabled = false
        etAlamataddpegawai.isEnabled = false
        etNoHpaddpegawai.isEnabled = false
        etCabangaddpegawai.isEnabled = false

        etNameaddpegawai.alpha = 0.6f
        etAlamataddpegawai.alpha = 0.6f
        etNoHpaddpegawai.alpha = 0.6f
        etCabangaddpegawai.alpha = 0.6f
    }


    fun hidup() {
        etNameaddpegawai.isEnabled = true
        etAlamataddpegawai.isEnabled = true
        etNoHpaddpegawai.isEnabled = true
        etCabangaddpegawai.isEnabled = true

        etNameaddpegawai.alpha = 1f
        etAlamataddpegawai.alpha = 1f
        etNoHpaddpegawai.alpha = 1f
        etCabangaddpegawai.alpha = 1f
    }


    fun update() {
        val pegawaiRef = database.getReference("pegawai").child(idPegawai)
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val data = model_pegawai(
            idPegawai,
            etNameaddpegawai.text.toString(),
            etAlamataddpegawai.text.toString(),
            etNoHpaddpegawai.text.toString(),
            etCabangaddpegawai.text.toString(),
            currentTime
        )

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = data.namaPegawai ?: ""
        updateData["alamatPegawai"] = data.alamatPegawai ?: ""
        updateData["noHPPegawai"] = data.noHPPegawai ?: ""
        updateData["idCabang"] = data.idCabang ?: ""

        pegawaiRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.Data_Pegawai_Berhasil_Diperbarui), Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.Data_Pegawai_Gagal_Diperbarui), Toast.LENGTH_SHORT).show()
        }
    }

    fun simpan() {
        val pegawaiBaru = myRef.push()
        val idPegawai = pegawaiBaru.key
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val data = model_pegawai(
            idPegawai.toString(),
            etNameaddpegawai.text.toString(),
            etAlamataddpegawai.text.toString(),
            etNoHpaddpegawai.text.toString(),
            etCabangaddpegawai.text.toString(),
            currentTime
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.tambah_pegawai_sukses), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.tambah_pegawai_gagal), Toast.LENGTH_SHORT).show()
            }
    }

    fun cekValidasi() {
        val nama = etNameaddpegawai.text.toString()
        val alamat = etAlamataddpegawai.text.toString()
        val noHP = etNoHpaddpegawai.text.toString()
        val cabang = etCabangaddpegawai.text.toString()

        if (nama.isEmpty()) {
            etNameaddpegawai.error = getString(R.string.validasi_nama_pegawai)
            Toast.makeText(this, getString(R.string.validasi_nama_pegawai), Toast.LENGTH_SHORT).show()
            etNameaddpegawai.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamataddpegawai.error = getString(R.string.validasi_alamat_pegawai)
            Toast.makeText(this, getString(R.string.validasi_alamat_pegawai), Toast.LENGTH_SHORT).show()
            etAlamataddpegawai.requestFocus()
            return
        }
        if (noHP.isEmpty()) {
            etNoHpaddpegawai.error = getString(R.string.validasi_noHP_pegawai)
            Toast.makeText(this, getString(R.string.validasi_noHP_pegawai), Toast.LENGTH_SHORT).show()
            etNoHpaddpegawai.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabangaddpegawai.error = getString(R.string.validasi_cabang_pegawai)
            Toast.makeText(this, getString(R.string.validasi_cabang_pegawai), Toast.LENGTH_SHORT).show()
            etCabangaddpegawai.requestFocus()
            return
        }

        if (buttonaddpegawai.text.equals(getString(R.string.tombol_simpan))) {
            // For both new records and dialog-initiated edits
            if (idPegawai.isEmpty()) {
                simpan() // Create new record
            } else {
                update() // Update existing record
            }
        } else if (buttonaddpegawai.text.equals(getString(R.string.tombol_sunting))) {
            hidup()
            etNameaddpegawai.requestFocus()
            buttonaddpegawai.text = getString(R.string.tombol_perbarui)
        } else if (buttonaddpegawai.text.equals(getString(R.string.tombol_perbarui))) {
            update()
        }
    }
}