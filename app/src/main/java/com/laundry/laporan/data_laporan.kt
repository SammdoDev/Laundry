package com.laundry.laporan

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.laundry.R
import com.laundry.adapter.adapter_data_laporan
import com.laundry.model_data.StatusLaporan
import com.laundry.model_data.model_laporan

class data_laporan : AppCompatActivity(), adapter_data_laporan.OnStatusChangeListener, adapter_data_laporan.OnDeleteListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: adapter_data_laporan
    private val laporanList = ArrayList<model_laporan>()
    private lateinit var database: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_laporan)

        database = FirebaseDatabase.getInstance().getReference("Laporan")

        recyclerView = findViewById(R.id.rvDATA_LAPORAN)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = adapter_data_laporan(laporanList, this, this)
        recyclerView.adapter = adapter

        loadData()
    }

    private fun loadData() {
        database.orderByChild("tanggal").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                laporanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val laporan = dataSnapshot.getValue(model_laporan::class.java)
                    laporan?.let {
                        laporanList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@data_laporan, "Gagal memuat data laporan", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStatusChanged(laporan: model_laporan, newStatus: StatusLaporan) {
        // Update status di Firebase
        val noTransaksi = laporan.noTransaksi
        if (!noTransaksi.isNullOrEmpty()) {
            database.child(noTransaksi).child("status").setValue(newStatus)
                .addOnSuccessListener {
                    val statusMessage = when (newStatus) {
                        StatusLaporan.SUDAH_DIBAYAR -> "Status berhasil diubah menjadi Sudah Dibayar"
                        StatusLaporan.BELUM_DIBAYAR -> "Status berhasil diubah menjadi Belum Dibayar"
                        StatusLaporan.SELESAI -> "Status berhasil diubah menjadi Selesai"
                    }
                    Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal mengupdate status", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDeleteItem(laporan: model_laporan) {
        val noTransaksi = laporan.noTransaksi
        if (!noTransaksi.isNullOrEmpty()) {
            database.child(noTransaksi).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    // Data akan otomatis ter-update melalui ValueEventListener
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus laporan", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No transaksi tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}