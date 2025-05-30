package com.laundry.transaksi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.laundry.adapter.adapter_pilih_tambahan
import com.laundry.R
import com.laundry.model_data.model_tambahan

class pilihLayananTambahan : AppCompatActivity() {

    private val TAG = "pilihLayananTambahan"
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("tambahan")

    private lateinit var rvPilihLayananTambahan: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvKosong: TextView

    private var idCabang: String = ""
    private lateinit var listLayananTambahan: ArrayList<model_tambahan>
    private lateinit var filteredList: ArrayList<model_tambahan>
    private lateinit var adapter: adapter_pilih_tambahan

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.pilih_layanan_tambahan)

        Log.d(TAG, "Activity created")

        tvKosong = findViewById(R.id.tvKosong)
        rvPilihLayananTambahan = findViewById(R.id.rvPILIH_TAMBAHAN)
        searchView = findViewById(R.id.searchView)

        listLayananTambahan = ArrayList()
        filteredList = ArrayList()

        rvPilihLayananTambahan.layoutManager = LinearLayoutManager(this)

        setupSearchView()
        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        Log.d(TAG, "Filtering list with query: $query")
        filteredList.clear()

        if (query.isNullOrEmpty()) {
            filteredList.addAll(listLayananTambahan)
        } else {
            val searchText = query.lowercase().trim()
            for (tambahan in listLayananTambahan) {
                if (tambahan.namaTambahan?.lowercase()?.contains(searchText) == true ||
                    tambahan.hargaTambahan?.lowercase()?.contains(searchText) == true) {
                    filteredList.add(tambahan)
                }
            }
        }

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        Log.d(TAG, "Updating RecyclerView with ${filteredList.size} items")

        if (filteredList.isEmpty()) {
            tvKosong.visibility = View.VISIBLE
            tvKosong.text = "Tidak ada tambahan yang cocok"
            rvPilihLayananTambahan.visibility = View.GONE
        } else {
            tvKosong.visibility = View.GONE
            rvPilihLayananTambahan.visibility = View.VISIBLE
        }

        // Adapter untuk pilihLayananTambahan tanpa tombol hapus dan bisa diklik untuk seleksi
        adapter = adapter_pilih_tambahan(
            listTambahan = filteredList,
            onDeleteClick = null, // Tidak ada fungsi hapus
            showDeleteButton = false, // Sembunyikan tombol hapus
            isSelectable = true // Bisa diklik untuk seleksi
        )

        rvPilihLayananTambahan.adapter = adapter
    }

    private fun getData() {
        Log.d(TAG, "getData() called")

        val query = if (idCabang.isEmpty()) {
            myRef.limitToLast(100)
        } else {
            myRef.orderByChild("idCabang").equalTo(idCabang).limitToLast(100)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: data exists: ${snapshot.exists()}, count: ${snapshot.childrenCount}")

                if (snapshot.exists()) {
                    listLayananTambahan.clear()
                    for (snap in snapshot.children) {
                        try {
                            val tambahan = snap.getValue(model_tambahan::class.java)
                            tambahan?.let {
                                listLayananTambahan.add(it)
                                Log.d(TAG, "Added layanan tambahan: ${it.namaTambahan}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing layanan tambahan: ${e.message}")
                        }
                    }

                    filteredList.clear()
                    filteredList.addAll(listLayananTambahan)

                    updateRecyclerView()
                } else {
                    Log.d(TAG, "No data found")
                    tvKosong.visibility = View.VISIBLE
                    tvKosong.text = "Data layanan tambahan tidak ditemukan"
                    rvPilihLayananTambahan.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                tvKosong.visibility = View.VISIBLE
                tvKosong.text = "Error: ${error.message}"
                rvPilihLayananTambahan.visibility = View.GONE
                Toast.makeText(this@pilihLayananTambahan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}