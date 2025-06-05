package com.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_pelanggan

class adapter_pilih_pelanggan(
    private val pelangganList: ArrayList<model_pelanggan>,
    private val onItemClick: (model_pelanggan) -> Unit
) : RecyclerView.Adapter<adapter_pilih_pelanggan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_pelanggan, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val nomor = position + 1
        val item = pelangganList[position]
        holder.tvID.text = nomor.toString()
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = "${context.getString(R.string.label_alamat)}: ${item.alamatPelanggan ?: "-"}"
        holder.tvNoHP.text = "${context.getString(R.string.label_phone)}: ${item.noHPPelanggan ?: "-"}"


        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = pelangganList.size

    fun updateList(newList: List<model_pelanggan>) {
        pelangganList.clear()
        pelangganList.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvCARD_PILIHPELANGGAN_ID)
        val tvNama: TextView = itemView.findViewById(R.id.tvCARD_PILIHPELANGGAN_nama)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvCARD_PILIHPELANGGAN_alamat)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvCARD_PILIHPELANGGAN_nohp)
        val cvCARD: View = itemView.findViewById(R.id.cvCARD_PILIHPELANGGAN)
    }
}
