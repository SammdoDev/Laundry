package com.laundry.cabang

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adapter.adapter_data_cabang
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.laundry.R
import com.laundry.model_data.model_cabang

class data_cabang : AppCompatActivity() {

    // Inisialisasi database Firebase
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var rvDATA_CABANG: RecyclerView
    private lateinit var fabDATA_PENGGUNA_Tambah: FloatingActionButton
    private lateinit var cabangnList: ArrayList<model_cabang>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.data_cabang)

        initViews()

        // Konfigurasi RecyclerView
        val layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        rvDATA_CABANG.layoutManager = layoutManager
        rvDATA_CABANG.setHasFixedSize(true)

        cabangnList = arrayListOf()
        getData()

        // Mengatur insets untuk layout utama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fabDATA_PENGGUNA_Tambah.setOnClickListener {
            val intent = Intent(this, tambah_cabang::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        rvDATA_CABANG = findViewById(R.id.rvDATA_CABANG)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.tambahData)
    }

    private fun getData() {
        val query = myRef.orderByChild("idCabang").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    cabangnList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(model_cabang::class.java)
                        if (cabang != null) {
                            cabangnList.add(cabang)
                        }
                    }
                    val adapter = adapter_data_cabang(cabangnList)
                    rvDATA_CABANG.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@data_cabang, getString(R.string.msg_gagal_memuat_data), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
