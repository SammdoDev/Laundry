package com.laundry.pelanggan

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
import com.adapter.adapter_data_pelanggan
import com.laundry.R
import com.laundry.model_data.model_pelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


// Disarankan menggunakan penamaan kelas dengan huruf kapital.
class data_pelanggan : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")

    lateinit var rvDatapelanggan : RecyclerView
    lateinit var fabTambahpelanggan : FloatingActionButton
    lateinit var listpelanggan: ArrayList<model_pelanggan>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.data_pelanggan)

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDatapelanggan.layoutManager = layoutManager
        rvDatapelanggan.setHasFixedSize(true)
        listpelanggan = arrayListOf<model_pelanggan>()

        getData()

        val fabTambahpelanggan : FloatingActionButton = findViewById(R.id.tambahData)
        fabTambahpelanggan.setOnClickListener {
            val intent = Intent(this, tambah_pelanggan::class.java)
            intent.putExtra("judul",  (this.getString(R.string.tambah_pelanggan)))
            intent.putExtra("idPelanggan", "")
            intent.putExtra("namaPelanggan", "")
            intent.putExtra("noHPPelanggan", "")
            intent.putExtra("alamatPelanggan", "")
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init(){
        rvDatapelanggan = findViewById(R.id.rvDATA_PELANGGAN)
        fabTambahpelanggan = findViewById(R.id.tambahData)
    }
    fun getData(){
        val query = myRef.orderByChild("idpelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    listpelanggan.clear()
                    for (dataSnapshot in snapshot.children) {
                        val pelanggan = dataSnapshot.getValue(model_pelanggan::class.java)
                        listpelanggan.add(pelanggan!!)
                    }
                    val adapter = adapter_data_pelanggan(listpelanggan)
                    rvDatapelanggan.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@data_pelanggan, getString(R.string.msg_gagal_memuat_data), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
