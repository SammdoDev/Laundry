package com.laundry.transaksi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.adapter.adapter_konfirmasi_tambahan
import com.laundry.invoice.Invoice
import com.laundry.model_data.model_tambahan
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class KonfirmasiData : AppCompatActivity() {

    private var namaPelanggan: String? = null
    private var noHP: String? = null
    private var namaLayanan: String? = null
    private var hargaLayanan: String? = null
    private var jumlahLayanan: Int = 1
    private var hargaLayananInt: Int = 0
    private var totalHargaLayanan: Int = 0
    private var layananTambahan: ArrayList<model_tambahan>? = null
    private var totalBayar: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.konfirmasi_data_transaksi)

        // Ambil data dari Intent
        namaPelanggan = intent.getStringExtra("namaPelanggan")
        noHP = intent.getStringExtra("noHP")
        namaLayanan = intent.getStringExtra("namaLayanan")
        hargaLayanan = intent.getStringExtra("hargaLayanan")
        jumlahLayanan = intent.getIntExtra("jumlahLayanan", 1)
        hargaLayananInt = intent.getIntExtra("hargaLayananInt", 0)
        totalHargaLayanan = intent.getIntExtra("totalHargaLayanan", 0)
        layananTambahan = intent.getSerializableExtra("layananTambahan") as? ArrayList<model_tambahan>

        setupUI()
        calculateTotal()
        setupButtonListener()
    }

    private fun setupUI() {
        // Tampilkan data layanan dan pelanggan
        findViewById<TextView>(R.id.tvNamaLayanan).text = namaLayanan ?: "-"
        findViewById<TextView>(R.id.tvHargaLayanan).text = "${hargaLayanan ?: "-"} x ${jumlahLayanan}"
        findViewById<TextView>(R.id.tvTotalHargaLayanan).text = "Rp${formatRupiah(totalHargaLayanan)}"
        findViewById<TextView>(R.id.tvNamaPelanggan).text = namaPelanggan ?: "-"
        findViewById<TextView>(R.id.tvNoHP).text = noHP ?: "-"

        // Setup RecyclerView untuk layanan tambahan
        val rvTambahan = findViewById<RecyclerView>(R.id.rvLayananTambahan)
        rvTambahan.layoutManager = LinearLayoutManager(this)
        val adapter = adapter_konfirmasi_tambahan(layananTambahan ?: emptyList())
        rvTambahan.adapter = adapter
    }

    private fun calculateTotal() {
        // Hitung total bayar
        val totalTambahan = layananTambahan?.sumOf {
            it.hargaTambahan.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
        } ?: 0

        totalBayar = totalHargaLayanan + totalTambahan

        // Tampilkan total bayar
        findViewById<TextView>(R.id.tvTotalBayar).text = "Rp${formatRupiah(totalBayar)}"
    }

    private fun setupButtonListener() {
        val btnKonfirmasi = findViewById<Button>(R.id.btnKonfirmasi)
        btnKonfirmasi.setOnClickListener {
            showPaymentDialog()
        }

        val btnBatal = findViewById<Button>(R.id.btnBatal)
        btnBatal.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }
    }

    private fun showPaymentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_method, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Setup click listeners untuk setiap metode pembayaran
        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_bayar_nanti).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("Bayar Nanti")
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_tunai).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("Tunai")
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_qris).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("QRIS")
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_dana).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("DANA")
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_gopay).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("GoPay")
        }

        dialogView.findViewById<androidx.cardview.widget.CardView>(R.id.btn_ovo).setOnClickListener {
            dialog.dismiss()
            navigateToInvoice("OVO")
        }

        dialogView.findViewById<TextView>(R.id.btn_batal).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun navigateToInvoice(metodePembayaran: String) {
        val intent = Intent(this, Invoice::class.java).apply {
            // Data transaksi utama
            putExtra("idTransaksi", generateTransactionId())
            putExtra("tanggal", getCurrentDate())
            putExtra("pelanggan", namaPelanggan)
            putExtra("noHP", noHP)
            putExtra("karyawan", getCurrentUser())

            // Data layanan lengkap
            putExtra("layanan", namaLayanan)
            putExtra("hargaLayanan", hargaLayananInt)
            putExtra("hargaLayananString", hargaLayanan) // Tambahan untuk string format
            putExtra("jumlahLayanan", jumlahLayanan) // Quantity layanan utama
            putExtra("totalHargaLayanan", totalHargaLayanan)

            // Data pembayaran
            putExtra("metodePembayaran", metodePembayaran)
            putExtra("totalBayar", totalBayar)

            // Data layanan tambahan
            putExtra("tambahan", layananTambahan)

            // Hitung subtotal tambahan untuk kemudahan
            val subtotalTambahan = layananTambahan?.sumOf {
                it.hargaTambahan.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
            } ?: 0
            putExtra("subtotalTambahan", subtotalTambahan)
        }

        startActivity(intent)
        finish()
    }

    private fun generateTransactionId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextInt(1000, 9999)
        return "TRX$timestamp$random"
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentUser(): String {
        // TODO: Implementasi untuk mendapatkan nama karyawan yang sedang login
        // Bisa dari SharedPreferences, database, atau session management
        return "Admin" // Default sementara
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }
}