package com.laundry.layanan

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.laundry.R
import com.laundry.model_data.model_layanan
import com.adapter.adapter_data_layanan

class data_layanan : AppCompatActivity() {

    // Inisialisasi database Firebase
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    private lateinit var rvDATA_LAYANAN: RecyclerView
    private lateinit var fabDATA_PENGGUNA_Tambah: FloatingActionButton
    private lateinit var layananList: ArrayList<model_layanan>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.data_layanan)

        initViews()

        // Konfigurasi RecyclerView
        val layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        rvDATA_LAYANAN.layoutManager = layoutManager
        rvDATA_LAYANAN.setHasFixedSize(true)

        // Inisialisasi list data layanan
        layananList = arrayListOf()
        getData()

        // Mengatur insets untuk layout utama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mengarahkan FAB ke activity tambah_layanan
        fabDATA_PENGGUNA_Tambah.setOnClickListener {
            val intent = Intent(this, tambah_layanan::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        rvDATA_LAYANAN = findViewById(R.id.rvDATA_LAYANAN)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.tambahData)
    }

    private fun getData() {
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    layananList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val layanan = dataSnapshot.getValue(model_layanan::class.java)
                        if (layanan != null) {
                            layananList.add(layanan)
                        }
                    }
                    val adapter = adapter_data_layanan(layananList)
                    rvDATA_LAYANAN.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@data_layanan, "Data Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
