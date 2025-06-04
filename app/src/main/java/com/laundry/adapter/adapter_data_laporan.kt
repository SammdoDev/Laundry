package com.laundry.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.StatusLaporan
import com.laundry.model_data.model_laporan
import java.text.NumberFormat
import java.util.*

class adapter_data_laporan(
    private val laporanList: MutableList<model_laporan>,
    private val onStatusChangeListener: OnStatusChangeListener? = null,
    private val onDeleteListener: OnDeleteListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnStatusChangeListener {
        fun onStatusChanged(laporan: model_laporan, newStatus: StatusLaporan)
    }

    interface OnDeleteListener {
        fun onDeleteItem(laporan: model_laporan)
    }

    companion object {
        const val TYPE_SUDAH_DIBAYAR = 0
        const val TYPE_BELUM_DIBAYAR = 1
        const val TYPE_SELESAI = 2

        fun formatCurrency(amount: Int): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(amount).replace("IDR", "Rp")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (laporanList[position].status) {
            StatusLaporan.SUDAH_DIBAYAR -> TYPE_SUDAH_DIBAYAR
            StatusLaporan.BELUM_DIBAYAR -> TYPE_BELUM_DIBAYAR
            StatusLaporan.SELESAI -> TYPE_SELESAI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SUDAH_DIBAYAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_sudahdibayar, parent, false)
                SudahDibayarViewHolder(view)
            }
            TYPE_BELUM_DIBAYAR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_belumdibayar, parent, false)
                BelumDibayarViewHolder(view)
            }
            TYPE_SELESAI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_data_transaksi_selesai, parent, false)
                SelesaiViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position >= 0 && position < laporanList.size) {
            val laporan = laporanList[position]
            when (holder) {
                is SudahDibayarViewHolder -> holder.bind(laporan)
                is BelumDibayarViewHolder -> holder.bind(laporan)
                is SelesaiViewHolder -> holder.bind(laporan)
            }
        }
    }

    override fun getItemCount(): Int = laporanList.size

    private fun setupLongClickDelete(itemView: View, laporan: model_laporan) {
        itemView.setOnLongClickListener {
            val context = itemView.context

            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_hapus_transaksi))
                .setMessage(
                    context.getString(
                        R.string.msg_hapus_transaksi,
                        laporan.noTransaksi,
                        laporan.namaPelanggan,
                        laporan.tanggal
                    )
                )
                .setPositiveButton(context.getString(R.string.btn_hapus)) { dialog, _ ->
                    onDeleteListener?.onDeleteItem(laporan)
                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.btn_batal)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
            true
        }
    }

    // ViewHolder untuk Sudah Dibayar
    inner class SudahDibayarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val btnSelesai: Button? = try {
            itemView.findViewById(R.id.btnPickup)
        } catch (e: Exception) {
            null
        }

        fun bind(laporan: model_laporan) {
            tvNamaPelanggan.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime.text = laporan.tanggal ?: ""
            tvLayanan.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount.text = formatCurrency(laporan.totalHarga ?: 0)

            setupLongClickDelete(itemView, laporan)

            btnSelesai?.setOnClickListener {
                onStatusChangeListener?.onStatusChanged(laporan, StatusLaporan.SELESAI)
            }
        }
    }

    // ViewHolder untuk Belum Dibayar
    inner class BelumDibayarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val btnBayar: Button? = try {
            itemView.findViewById(R.id.btnPickup)
        } catch (e: Exception) {
            null
        }

        fun bind(laporan: model_laporan) {
            tvNamaPelanggan.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime.text = laporan.tanggal ?: ""
            tvLayanan.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount.text = formatCurrency(laporan.totalHarga ?: 0)

            setupLongClickDelete(itemView, laporan)

            btnBayar?.setOnClickListener {
                onStatusChangeListener?.onStatusChanged(laporan, StatusLaporan.SUDAH_DIBAYAR)
            }
        }
    }

    // ViewHolder untuk Selesai
    inner class SelesaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val tvDateTimeAmbil: TextView = itemView.findViewById(R.id.tvDateTimeAmbil)

        fun bind(laporan: model_laporan) {
            tvNamaPelanggan.text = laporan.namaPelanggan ?: "Unknown"
            tvDateTime.text = laporan.tanggal ?: ""
            tvLayanan.text = laporan.namaLayanan ?: "Unknown Service"
            tvTotalAmount.text = formatCurrency(laporan.totalHarga ?: 0)
            tvDateTimeAmbil.text = getCurrentDateTime()

            setupLongClickDelete(itemView, laporan)
        }

        private fun getCurrentDateTime(): String {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }
}