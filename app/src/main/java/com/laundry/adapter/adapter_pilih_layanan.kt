package com.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.laundry.R
import com.laundry.model_data.model_layanan

class adapter_pilih_layanan(
    private val layananList: ArrayList<model_layanan>,
    private val onItemClick: (model_layanan) -> Unit
) : RecyclerView.Adapter<adapter_pilih_layanan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val nomor = position + 1
        val item = layananList[position]

        holder.tvID.text = nomor.toString()
        holder.tvNamaLayanan.text = item.namaLayanan
        holder.tvHarga.text = "${context.getString(R.string.label_price)}: ${item.harga}"


        holder.cvCARD.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = layananList.size

    fun updateList(newList: List<model_layanan>) {
        layananList.clear()
        layananList.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvCARD_PILIH_LAYANAN_ID)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvCARD_PILIH_LAYANAN_NAMA)
        val tvHarga: TextView = itemView.findViewById(R.id.tvCARD_PILIH_LAYANAN_HARGA)
        val cvCARD: View = itemView.findViewById(R.id.cvCARD_PILIH_LAYANAN)
    }
}