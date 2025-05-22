package com.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_tambahan

class adapter_konfirmasi_tambahan(
    private val listTambahan: List<model_tambahan>
) : RecyclerView.Adapter<adapter_konfirmasi_tambahan.TambahanViewHolder>() {

    class TambahanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId = itemView.findViewById<TextView>(R.id.tvCARD_PILIH_TAMBAHAN_ID)
        val tvNama = itemView.findViewById<TextView>(R.id.tvCARD_PILIH_TAMBAHAN_NAMA)
        val tvHarga = itemView.findViewById<TextView>(R.id.tvCARD_PILIH_TAMBAHAN_HARGA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TambahanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_pilih_tambahan, parent, false)
        return TambahanViewHolder(view)
    }

    override fun onBindViewHolder(holder: TambahanViewHolder, position: Int) {
        val tambahan = listTambahan[position]
        holder.tvId.text = tambahan.idTambahan
        holder.tvNama.text = tambahan.namaTambahan
        holder.tvHarga.text = "Rp${tambahan.hargaTambahan}"
    }

    override fun getItemCount(): Int = listTambahan.size

    // Fungsi untuk menghitung total harga tambahan
    fun getTotalHargaTambahan(): Int {
        return listTambahan.sumOf { it.hargaTambahan.toIntOrNull() ?: 0 }
    }
}
