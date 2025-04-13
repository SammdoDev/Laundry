package com.laundry.pegawai

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_pegawai
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class tambah_pegawai : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pegawai")

    lateinit var tvjuduladdpegawai : TextView
    lateinit var tvnamaaddpegawai : TextView
    lateinit var etNameaddpegawai : EditText
    lateinit var tvalamataddpegawai : TextView
    lateinit var etAlamataddpegawai : EditText
    lateinit var tvNoHpaddpegawai : TextView
    lateinit var etNoHpaddpegawai : EditText
    lateinit var tvCabangaddpegawai : TextView
    lateinit var etCabangaddpegawai : EditText
    lateinit var buttonaddpegawai : Button

    var idPegawai : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_pegawai)

        init()
        getData()
        buttonaddpegawai.setOnClickListener{
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
        tvnamaaddpegawai = findViewById(R.id.tvnamaaddpegawai)
        etNameaddpegawai = findViewById(R.id.etpegawainama)
        tvNoHpaddpegawai = findViewById(R.id.tvNoHpaddpegawai)
        etNoHpaddpegawai = findViewById(R.id.etpegawainohp)
        tvalamataddpegawai = findViewById(R.id.tvalamataddpegawai)
        etAlamataddpegawai = findViewById(R.id.etpegawailamat)
        tvCabangaddpegawai = findViewById(R.id.tvCabangaddpegawai)
        etCabangaddpegawai = findViewById(R.id.etpegawaicabang)
        buttonaddpegawai = findViewById(R.id.btpegawaisimpan)
    }

    fun getData() {
        idPegawai = intent.getStringExtra("idPegawai").toString()
        val judul = intent.getStringExtra("judul")
        val nama = intent.getStringExtra("namaPegawai")
        val alamat = intent.getStringExtra("alamatPegawai")
        val nohp = intent.getStringExtra("noHPPegawai")
        val cabang = intent.getStringExtra("cabangPegawai")
        tvjuduladdpegawai.text = judul
        etNameaddpegawai.setText(nama)
        etAlamataddpegawai.setText(alamat)
        etNoHpaddpegawai.setText(nohp)
        etCabangaddpegawai.setText(cabang)
        if (!tvjuduladdpegawai.text.equals(this.getString(R.string.tambah_pegawai))) {
            if (judul.equals("Edit Pegawai")) {
                mati()
                buttonaddpegawai.text = "Sunting"
            }
        } else {
            hidup()
            etNameaddpegawai.requestFocus()
            buttonaddpegawai.text = "Simpan"
        }
    }

    fun mati() {
        etNameaddpegawai.isEnabled = false
        etAlamataddpegawai.isEnabled = false
        etNoHpaddpegawai.isEnabled = false
        etCabangaddpegawai.isEnabled = false
    }

    fun hidup() {
        etNameaddpegawai.isEnabled = true
        etAlamataddpegawai.isEnabled = true
        etNoHpaddpegawai.isEnabled = true
        etCabangaddpegawai.isEnabled = true
    }

    fun update() {
        val pegawaiRef = database.getReference("pegawai").child(idPegawai)
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(
            Date()
        )
        val data = model_pegawai(
            idPegawai,
            etNameaddpegawai.text.toString(),
            etAlamataddpegawai.text.toString(),
            etNoHpaddpegawai.text.toString(),
            etCabangaddpegawai.text.toString(),
            currentTime)
        // map untuk update data
        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = data.namaPegawai.toString()
        updateData["alamatPegawai"] = data.alamatPegawai.toString()
        updateData["noHPPegawai"] = data.noHPPegawai.toString()
        updateData["cabangPegawai"] = data.idCabang.toString()
        pegawaiRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this, this.getString(R.string.Data_Pegawai_Berhasil_Diperbarui),Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, this.getString(R.string.Data_Pegawai_Gagal_Diperbarui),Toast.LENGTH_SHORT).show()
        }
    }

    fun simpan(){
        val pegawaiBaru = myRef.push()
        val idPegawai = pegawaiBaru.key
        val currentTime = SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(
            Date()
        )
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
                Toast.makeText(this, this.getString(R.string.tambah_pegawai_sukses), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener() {
                Toast.makeText(this, this.getString(R.string.tambah_pegawai_gagal), Toast.LENGTH_SHORT).show()
            }
    }

    fun cekValidasi() {
        val nama = etNameaddpegawai.text.toString()
        val alamat = etAlamataddpegawai.text.toString()
        val noHP = etNoHpaddpegawai.text.toString()
        val cabang = etCabangaddpegawai.text.toString()
        // validasi data
        if (nama.isEmpty()) {
            etNameaddpegawai.error = this.getString(R.string.validasi_nama_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_nama_pegawai), Toast.LENGTH_SHORT).show()
            etNameaddpegawai.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamataddpegawai.error = this.getString(R.string.validasi_alamat_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_alamat_pegawai), Toast.LENGTH_SHORT).show()
            etAlamataddpegawai.requestFocus()
            return
        }
        if (noHP.isEmpty()) {
            etNoHpaddpegawai.error = this.getString(R.string.validasi_noHP_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_noHP_pegawai), Toast.LENGTH_SHORT).show()
            etNoHpaddpegawai.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabangaddpegawai.error = this.getString(R.string.validasi_cabang_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_cabang_pegawai), Toast.LENGTH_SHORT).show()
            etCabangaddpegawai.requestFocus()
            return
        }
        if (buttonaddpegawai.text.equals("Simpan")) {
            simpan()
        } else if (buttonaddpegawai.text.equals("Sunting")) {
            hidup()
            etNameaddpegawai.requestFocus()
            buttonaddpegawai.text = "Perbarui"
        } else if (buttonaddpegawai.text.equals("Perbarui")) {
            update()

        }
    }
}