package com.laundry.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.StatusLaporan
import com.laundry.model_data.model_laporan
import java.text.NumberFormat
import java.util.*

class adapter_data_laporan(
    private val laporanList: List<model_laporan>,
    private val statusChangeListener: OnStatusChangeListener,
    private val deleteListener: OnDeleteListener
) : RecyclerView.Adapter<adapter_data_laporan.LaporanViewHolder>() {

    interface OnStatusChangeListener {
        fun onStatusChanged(laporan: model_laporan, newStatus: StatusLaporan)
    }

    interface OnDeleteListener {
        fun onDeleteItem(laporan: model_laporan)
    }

    class LaporanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        val tvLayananQuantity: TextView = itemView.findViewById(R.id.tvLayananQuantity)
        val tvAdditionalServices: TextView = itemView.findViewById(R.id.tvAdditionalServices)
        val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        val btnPickup: Button = itemView.findViewById(R.id.btnPickup)

        // Optional: untuk breakdown harga
        val tvBreakdownHarga: TextView? = itemView.findViewById(R.id.tvBreakdownHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_transaksi_belumdibayar, parent, false)
        return LaporanViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val laporan = laporanList[position]

        // Set basic info
        holder.tvNamaPelanggan.text = laporan.namaPelanggan ?: "Pelanggan"
        holder.tvDateTime.text = laporan.tanggal ?: ""
        holder.tvLayanan.text = laporan.namaLayanan ?: "Layanan"
        holder.tvTotalAmount.text = formatCurrency(laporan.totalHarga)

        // Set quantity layanan utama
        val quantity = laporan.jumlahLayanan
        val context = holder.itemView.context
        if (quantity > 1) {
            holder.tvLayananQuantity.text = context.getString(R.string.qty_multiple, quantity)
        } else {
            holder.tvLayananQuantity.text = context.getString(R.string.qty_single)
        }
        holder.tvLayananQuantity.visibility = View.VISIBLE


        // Set layanan tambahan
        val jumlahTambahan = laporan.jumlahLayananTambahan

        if (jumlahTambahan > 0) {
            holder.tvAdditionalServices.text = context.getString(R.string.additional_services_format, jumlahTambahan)
        } else {
            holder.tvAdditionalServices.text = context.getString(R.string.no_additional_services)
        }

        holder.tvAdditionalServices.visibility = View.VISIBLE

        // Set breakdown harga (optional)
        holder.tvBreakdownHarga?.let { breakdownView ->
            if (laporan.totalHargaLayanan > 0 && laporan.subtotalTambahan > 0) {
                breakdownView.text = "Layanan Utama: ${formatCurrency(laporan.totalHargaLayanan)} + Tambahan: ${formatCurrency(laporan.subtotalTambahan)}"
                breakdownView.visibility = View.VISIBLE
            } else {
                breakdownView.visibility = View.GONE
            }
        }

        // Set status
        when (laporan.status) {
            StatusLaporan.BELUM_DIBAYAR -> {
                holder.tvPaymentStatus.text = context.getString(R.string.Belum_dibayar)
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.red))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_payment_status_belumbayar)
                holder.btnPickup.text = context.getString(R.string.Ambil_sekarang)
                holder.btnPickup.backgroundTintList = context.getColorStateList(R.color.red)
            }
            StatusLaporan.SUDAH_DIBAYAR -> {
                holder.tvPaymentStatus.text = context.getString(R.string.Sudah_dibayar)
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.green))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_payment_status_sudahbayar)
                holder.btnPickup.text = context.getString(R.string.selesai)
                holder.btnPickup.backgroundTintList = context.getColorStateList(R.color.green)
            }
            StatusLaporan.SELESAI -> {
                holder.tvPaymentStatus.text = context.getString(R.string.selesai)
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.blue))
                holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_payment_status_selesai)
                holder.btnPickup.text = context.getString(R.string.selesai)
                holder.btnPickup.backgroundTintList = context.getColorStateList(R.color.blue)
                holder.btnPickup.isEnabled = false
            }
        }


        // Button click listener
        holder.btnPickup.setOnClickListener {
            when (laporan.status) {
                StatusLaporan.BELUM_DIBAYAR -> {
                    statusChangeListener.onStatusChanged(laporan, StatusLaporan.SUDAH_DIBAYAR)
                }
                StatusLaporan.SUDAH_DIBAYAR -> {
                    statusChangeListener.onStatusChanged(laporan, StatusLaporan.SELESAI)
                }
                StatusLaporan.SELESAI -> {
                    // Do nothing or show completion message
                }
            }
        }

        // Long click for delete option
        holder.itemView.setOnLongClickListener {
            showDeleteDialog(holder.itemView.context, laporan)
            true
        }
    }

    override fun getItemCount(): Int = laporanList.size

    private fun formatCurrency(amount: Int): String {
        return "Rp${String.format("%,d", amount).replace(',', '.')}"
    }

    private fun showDeleteDialog(context: android.content.Context, laporan: model_laporan) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Hapus Laporan")
        builder.setMessage("Apakah Anda yakin ingin menghapus laporan transaksi ${laporan.noTransaksi}?")
        builder.setPositiveButton("Hapus") { _, _ ->
            deleteListener.onDeleteItem(laporan)
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }
}