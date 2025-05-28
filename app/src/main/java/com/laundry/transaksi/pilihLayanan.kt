package com.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adapter.adapter_pilih_layanan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.laundry.R
import com.laundry.model_data.model_layanan

class pilihLayanan : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: adapter_pilih_layanan

    private val database = FirebaseDatabase.getInstance()
    private val myRef: DatabaseReference = database.getReference("layanan")
    private val layananList = arrayListOf<model_layanan>()
    private val layananListFull = arrayListOf<model_layanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pilih_layanan)

        recyclerView = findViewById(R.id.rvLayanan)
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
        adapter = adapter_pilih_layanan(layananList) { layanan ->
            val intent = Intent().apply {
                putExtra("namaLayanan", layanan.namaLayanan ?: "Tidak diketahui")
                putExtra("harga", layanan.harga ?: "Tidak diketahui")
            }
            setResult(RESULT_OK, intent)
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = adapter
    }

    private fun loadDataFirebase() {
        myRef.orderByChild("namaLayanan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                layananList.clear()
                layananListFull.clear()

                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        data.getValue(model_layanan::class.java)?.let {
                            layananList.add(it)
                            layananListFull.add(it)
                        }
                    }
                }

                adapter.updateList(ArrayList(layananList))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@pilihLayanan, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (!newText.isNullOrBlank()) {
                    layananListFull.filter {
                        it.namaLayanan?.contains(newText, ignoreCase = true) == true
                    }
                } else {
                    layananListFull
                }

                adapter.updateList(ArrayList(filteredList))
                return true
            }
        })
    }
}