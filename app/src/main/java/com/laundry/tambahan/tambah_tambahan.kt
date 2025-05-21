package com.laundry.tambahan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.laundry.R
import com.laundry.model_data.model_tambahan

class tambah_tambahan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga: EditText
    lateinit var btSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tambah_tambahan)
        init()
        btSimpan.setOnClickListener{
            cekValidasi()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    fun init(){
        tvJudul = findViewById(R.id.tvtambahanjudul)
        etNama = findViewById(R.id.ettambahannama)
        etHarga = findViewById(R.id.ettambahanharga)
        btSimpan = findViewById(R.id.bttambahansimpan)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etHarga.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasi_nama_tambahan)
            Toast.makeText(this, this.getString(R.string.validasi_nama_tambahan), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etHarga.error = this.getString(R.string.validasi_harga_tambahan)
            Toast.makeText(this, this.getString(R.string.validasi_harga_tambahan), Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }
        simpan()
    }

    fun simpan() {
        val tambahanBaru = myRef.push()
        val tambahanid = tambahanBaru.key
        val data = model_tambahan(
            tambahanid.toString(),
            etNama.text.toString(),
            etHarga.text.toString(),
        )
        tambahanBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    this.getString(R.string.tambah_tambahan_sukses),
                    Toast.LENGTH_SHORT
                )
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    this.getString(R.string.tambah_tambahan_gagal),
                    Toast.LENGTH_SHORT
                )
            }
    }
}