package com.laundry.pegawai

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
import com.adapter.adapter_data_pegawai
import com.laundry.R
import com.laundry.model_data.model_pegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


// Disarankan menggunakan penamaan kelas dengan huruf kapital.
class data_pegawai : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pegawai")

    lateinit var rvDataPegawai : RecyclerView
    lateinit var fabTambahPegawai : FloatingActionButton
    lateinit var listpegawai: ArrayList<model_pegawai>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.data_pegawai)

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataPegawai.layoutManager = layoutManager
        rvDataPegawai.setHasFixedSize(true)
        listpegawai = arrayListOf<model_pegawai>()

        getData()

        val fabTambahPegawai : FloatingActionButton = findViewById(R.id.tambahData)
        fabTambahPegawai.setOnClickListener {
            val intent = Intent(this, tambah_pegawai::class.java)
            intent.putExtra("judul",  (this.getString(R.string.tambah_pegawai)))
            intent.putExtra("idPegawai", "")
            intent.putExtra("namaPegawai", "")
            intent.putExtra("noHPPegawai", "")
            intent.putExtra("alamatPegawai", "")
            intent.putExtra("cabangPegawai", "")
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init(){
        rvDataPegawai = findViewById(R.id.rvDATA_PEGAWAI)
        fabTambahPegawai = findViewById(R.id.tambahData)
    }
    fun getData(){
        val query = myRef.orderByChild("idPegawai").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    listpegawai.clear()
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(model_pegawai::class.java)
                        listpegawai.add(pegawai!!)
                    }
                    val adapter = adapter_data_pegawai(listpegawai)
                    rvDataPegawai.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@data_pegawai, error.message, Toast.LENGTH_LONG)
            }
        })
    }
}
