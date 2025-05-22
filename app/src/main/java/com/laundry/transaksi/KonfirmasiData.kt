package com.laundry.transaksi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.adapter.adapter_konfirmasi_tambahan
import com.laundry.model_data.model_tambahan

class KonfirmasiData : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.konfirmasi_data_transaksi)

        val namaPelanggan = intent.getStringExtra("namaPelanggan")
        val noHP = intent.getStringExtra("noHP")
        val hargaLayanan = intent.getStringExtra("hargaLayanan")
        val layananTambahan = intent.getSerializableExtra("layananTambahan") as? ArrayList<model_tambahan>

        // Tampilkan nama dan nomor HP
        findViewById<TextView>(R.id.tvNamaPelanggan).text = "Nama: ${namaPelanggan ?: "-"}"
        findViewById<TextView>(R.id.tvNoHP).text = "No. HP: ${noHP ?: "-"}"

        // Setup RecyclerView (jika masih ingin tampilkan daftar tambahan)
        val rvTambahan = findViewById<RecyclerView>(R.id.rvLayananTambahan)
        rvTambahan.layoutManager = LinearLayoutManager(this)
        val adapter = adapter_konfirmasi_tambahan(layananTambahan ?: emptyList())
        rvTambahan.adapter = adapter

        // Hitung total bayar = harga layanan + tambahan
        val totalTambahan = layananTambahan?.sumOf { it.hargaTambahan.toIntOrNull() ?: 0 } ?: 0
        val hargaLayananInt = hargaLayanan?.toIntOrNull() ?: 0
        val totalBayar = hargaLayananInt + totalTambahan

        // Tampilkan total bayar saja
        findViewById<TextView>(R.id.tvTotalBayar).text = "Total Bayar: Rp$totalBayar"

        // Konfirmasi tombol
        val btnKonfirmasi = findViewById<Button>(R.id.btnKonfirmasi)
        btnKonfirmasi.setOnClickListener {
            showPaymentDialog()
        }
    }

    private fun showPaymentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_method, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_bayar_nanti).setOnClickListener {
            // TODO: aksi Bayar Nanti
            dialog.dismiss()
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_tunai).setOnClickListener {
            // TODO: aksi Bayar Tunai
            dialog.dismiss()
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_qris).setOnClickListener {
            // TODO: aksi QRIS
            dialog.dismiss()
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_dana).setOnClickListener {
            // TODO: aksi DANA
            dialog.dismiss()
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_gopay).setOnClickListener {
            // TODO: aksi GoPay
            dialog.dismiss()
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_ovo).setOnClickListener {
            // TODO: aksi OVO
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btn_batal).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
