package com.laundry.laporan

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.laundry.R
import com.laundry.adapter.adapter_data_laporan
import com.laundry.model_data.StatusLaporan
import com.laundry.model_data.model_laporan

class data_laporan : AppCompatActivity(),
    adapter_data_laporan.OnStatusChangeListener,
    adapter_data_laporan.OnDeleteListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: adapter_data_laporan
    private val laporanList = ArrayList<model_laporan>()
    private lateinit var database: DatabaseReference

    companion object {
        private const val TAG = "DataLaporan"
    }

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
                    try {
                        val laporan = dataSnapshot.getValue(model_laporan::class.java)
                        laporan?.let {
                            // Pastikan data dengan field baru dimuat dengan benar
                            if (it.jumlahLayanan == 0) it.jumlahLayanan = 1
                            if (it.jumlahLayananTambahan == 0 && !it.layananTambahan.isNullOrEmpty()) {
                                it.jumlahLayananTambahan = it.layananTambahan!!.size
                            }

                            laporanList.add(it)
                            Log.d(TAG, "Loaded laporan: ${it.noTransaksi}, qty: ${it.jumlahLayanan}, tambahan: ${it.jumlahLayananTambahan}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing laporan data: ${e.message}")
                    }
                }

                // Sort by date descending (newest first)
                laporanList.sortByDescending { it.tanggal }

                adapter.notifyDataSetChanged()
                Log.d(TAG, "Data refreshed. Total items: ${laporanList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load data: ${error.message}")
                Toast.makeText(
                    this@data_laporan,
                    getString(R.string.msg_gagal_memuat_laporan),
                    Toast.LENGTH_SHORT
                ).show()
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
                    Log.d(TAG, "Status updated for ${noTransaksi}: $newStatus")
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, getString(R.string.msg_gagal_update_status), Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to update status: ${exception.message}")
                }
        }
    }

    override fun onDeleteItem(laporan: model_laporan) {
        val noTransaksi = laporan.noTransaksi
        if (!noTransaksi.isNullOrEmpty()) {
            database.child(noTransaksi).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.msg_laporan_berhasil_dihapus), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Laporan deleted: $noTransaksi")
                    // Data akan otomatis ter-update melalui ValueEventListener
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, getString(R.string.msg_gagal_hapus_laporan), Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failed to delete laporan: ${exception.message}")
                }
        } else {
            Toast.makeText(this, getString(R.string.msg_no_transaksi_tidak_valid), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "DataLaporan activity destroyed")
    }
}