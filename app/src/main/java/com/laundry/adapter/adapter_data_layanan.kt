package com.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_layanan

class adapter_data_layanan(
    private val listLayanan: ArrayList<model_layanan>
) : RecyclerView.Adapter<adapter_data_layanan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayanan[position]
        holder.tvCARD_LAYANAN_ID.text = item.idLayanan
        holder.tvCARD_LAYANAN_NAMA.text = item.namaLayanan ?: "N/A"
        holder.tvCARD_LAYANAN_HARGA.text = item.harga ?: "N/A"
        holder.tvCARD_LAYANAN_CABANG.text = item.namaCabang ?: "N/A"

        holder.cvCARD_LAYANAN.setOnClickListener {
            // Tambahkan aksi saat item diklik jika diperlukan
        }
    }

    override fun getItemCount(): Int = listLayanan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCARD_LAYANAN = itemView.findViewById<View>(R.id.cvCARD_LAYANAN)
        val tvCARD_LAYANAN_ID = itemView.findViewById<TextView>(R.id.tvCARD_LAYANAN_ID)
        val tvCARD_LAYANAN_NAMA = itemView.findViewById<TextView>(R.id.tvCARD_LAYANAN_NAMA)
        val tvCARD_LAYANAN_HARGA = itemView.findViewById<TextView>(R.id.tvCARD_LAYANAN_HARGA)
        val tvCARD_LAYANAN_CABANG = itemView.findViewById<TextView>(R.id.tvCARD_LAYANAN_CABANG)
    }
}
