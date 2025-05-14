package com.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adapter.adapter_pilih_pelanggan
import com.laundry.model_data.model_pelanggan
import com.google.firebase.database.*
import com.laundry.R

class pilihPelanggan : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: adapter_pilih_pelanggan

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("pelanggan")
    private val pelangganList = arrayListOf<model_pelanggan>()
    private val pelangganListFull = arrayListOf<model_pelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_pelanggan)

        recyclerView = findViewById(R.id.rvPelanggan)
        searchView = findViewById(R.id.searchView)

        setupRecyclerView()
        loadDataFirebase()
        setupSearchListener()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = adapter_pilih_pelanggan(pelangganList) { pelanggan ->
            val intent = Intent().apply {
                putExtra("nama", pelanggan.namaPelanggan ?: "Tanpa Nama")
                putExtra("hp", pelanggan.noHPPelanggan ?: "Tidak diketahui")
            }
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter
    }

    private fun loadDataFirebase() {
        myRef.orderByChild("namaPelanggan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pelangganList.clear()
                pelangganListFull.clear()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        data.getValue(model_pelanggan::class.java)?.let {
                            pelangganList.add(it)
                            pelangganListFull.add(it)
                        }
                    }
                }

                adapter.updateList(ArrayList(pelangganList))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@pilihPelanggan, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrBlank()) {
                    pelangganListFull.filter {
                        it.namaPelanggan?.contains(newText, ignoreCase = true) == true
                    }
                } else {
                    pelangganListFull
                }

                adapter.updateList(ArrayList(filteredList))
                return true
            }
        })
    }
}
